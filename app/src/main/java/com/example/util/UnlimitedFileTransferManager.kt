package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.data.FirestoreChatRepository
import com.example.data.formatByteSize
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class PreparedAttachment(
    val fileId: String,
    val localFile: File,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String,
    val detectedType: String, // IMAGE, VIDEO, DOCUMENT, APK, FILE
    val transferMode: String, // CLOUD_CHUNKS, P2P_WEBRTC, LAN_HTTP
    val urlOrUri: String,
    val chunkCount: Int
)

/**
 * Handles high-capacity file, video, image, and document transfers (>= 1.5 GB up to 4 GB):
 * - <= 100 MB (signed-in): Async Firestore encrypted chunks (CLOUD_CHUNKS, 600 KB raw/doc)
 * - > 100 MB (1.5 GB - 4 GB):
 *     * Same Wi-Fi/Hotspot: Auto-started LocalMeshServer HTTP 206 Range stream (LAN_HTTP / P2P fallback URL)
 *     * Cross-network Internet: WebRTC DataChannel P2P binary stream (P2P_WEBRTC) signaled via Firestore
 * Zero broad storage permissions required (SAF / PhotoPicker only), zero OOM via buffered streaming,
 * and pre-flight StatFs storage check.
 */
object UnlimitedFileTransferManager {
    const val CHUNK_RAW_BYTES = 600 * 1024 // ~800 KB Base64 per Firestore chunk doc (under 1 MiB doc limit)
    const val CLOUD_CHUNK_CAP_BYTES = 100L * 1024L * 1024L // 100 MB async cloud relay
    const val TARGET_LARGE_FILE_MIN_BYTES = 1_610_612_736L // 1.5 GiB
    const val MAX_TRANSFER_FILE_BYTES = 4L * 1024L * 1024L * 1024L // 4 GiB max supported envelope

    private const val STREAM_BUFFER_BYTES = 256 * 1024
    private const val PREFS_NAME = "general_chat_shared_files"
    private const val KEY_PREFIX = "file_path_"

    private val _transferProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val transferProgress = _transferProgress.asStateFlow()

    fun chooseTransferMode(
        sizeBytes: Long,
        isSignedIn: Boolean
    ): String {
        return when {
            isSignedIn && sizeBytes in 1L..CLOUD_CHUNK_CAP_BYTES -> "CLOUD_CHUNKS"
            isSignedIn -> "P2P_WEBRTC"
            else -> "LAN_HTTP"
        }
    }

    fun restorePersistedFiles(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.all.forEach { (key, value) ->
                if (key.startsWith(KEY_PREFIX) && value is String) {
                    val fileId = key.removePrefix(KEY_PREFIX)
                    val file = File(value)
                    if (file.exists() && file.length() > 0L) {
                        val mime = guessMimeType(file.name)
                        LocalMeshServer.registerSharedFile(fileId, file, file.name, mime)
                    } else {
                        prefs.edit().remove(key).apply()
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun resolveRegisteredFile(context: Context, fileId: String): File? {
        if (fileId.isBlank()) return null
        LocalMeshServer.getRegisteredFile(fileId)?.file?.takeIf { it.exists() && it.length() > 0L }?.let {
            return it
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString("$KEY_PREFIX$fileId", null)
        if (!path.isNullOrBlank()) {
            val f = File(path)
            if (f.exists() && f.length() > 0L) {
                LocalMeshServer.registerSharedFile(fileId, f, f.name, guessMimeType(f.name))
                return f
            }
        }
        // Fallback scan shared_files directory by fileId prefix
        val dir = File(context.filesDir, "shared_files")
        if (dir.exists()) {
            dir.listFiles()?.firstOrNull { it.name.startsWith("${fileId}_") && it.length() > 0L }?.let { found ->
                persistFileMapping(context, fileId, found)
                LocalMeshServer.registerSharedFile(fileId, found, found.name.removePrefix("${fileId}_"), guessMimeType(found.name))
                return found
            }
        }
        return null
    }

    private fun persistFileMapping(context: Context, fileId: String, file: File) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$KEY_PREFIX$fileId", file.absolutePath)
            .apply()
    }

    private fun ensureDiskSpaceOrEvict(targetDir: File, requiredBytes: Long): Boolean {
        if (requiredBytes <= 0L) return true
        val minRequiredWithMargin = requiredBytes + (16L * 1024L * 1024L)
        if (availableBytes(targetDir) >= minRequiredWithMargin) return true

        // Evict oldest downloaded cache files (dl_*) before rejecting
        val cachedDownloads = targetDir.listFiles()
            ?.filter { it.name.startsWith("dl_") || it.name.endsWith(".part") }
            ?.sortedBy { it.lastModified() }
            .orEmpty()
        for (oldFile in cachedDownloads) {
            oldFile.delete()
            if (availableBytes(targetDir) >= minRequiredWithMargin) return true
        }
        return availableBytes(targetDir) >= requiredBytes
    }

    private fun availableBytes(dir: File): Long {
        return try {
            dir.mkdirs()
            val stat = StatFs(dir.absolutePath)
            stat.availableBytes
        } catch (_: Exception) {
            Long.MAX_VALUE
        }
    }

    suspend fun prepareAndRegisterAttachment(
        context: Context,
        sourceUri: Uri
    ): Result<PreparedAttachment> = withContext(Dispatchers.IO) {
        var destFile: File? = null
        try {
            val cr = context.contentResolver
            var displayName = sourceUri.lastPathSegment?.substringAfterLast('/') ?: "dosya_${System.currentTimeMillis()}"
            var reportedSize = 0L

            cr.query(sourceUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }?.let { displayName = it }
                    }
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIdx != -1 && !cursor.isNull(sizeIdx)) {
                        reportedSize = cursor.getLong(sizeIdx).coerceAtLeast(0L)
                    }
                }
            }

            if (reportedSize > MAX_TRANSFER_FILE_BYTES) {
                return@withContext Result.failure(
                    IllegalArgumentException(
                        "Dosya boyutu (${formatByteSize(reportedSize)}) tek aktarımda desteklenen üst sınır olan ${formatByteSize(MAX_TRANSFER_FILE_BYTES)} değerini aşıyor."
                    )
                )
            }

            val targetDir = File(context.filesDir, "shared_files").apply { mkdirs() }
            if (reportedSize > 0L && !ensureDiskSpaceOrEvict(targetDir, reportedSize)) {
                val free = availableBytes(targetDir)
                return@withContext Result.failure(
                    IllegalStateException(
                        "Cihaz depolama alanı yetersiz: ${formatByteSize(reportedSize)} gerekli, boş alan ${formatByteSize(free)}."
                    )
                )
            }

            val mimeType = cr.getType(sourceUri) ?: guessMimeType(displayName)
            val fileId = "att_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val safeFileName = displayName.replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
            val outFile = File(targetDir, "${fileId}_$safeFileName")
            destFile = outFile

            var actualBytes = 0L
            cr.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    val buffer = ByteArray(STREAM_BUFFER_BYTES)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        if (actualBytes + read > MAX_TRANSFER_FILE_BYTES) {
                            throw IllegalArgumentException(
                                "Dosya boyutu maksimum ${formatByteSize(MAX_TRANSFER_FILE_BYTES)} sınırını aşıyor."
                            )
                        }
                        output.write(buffer, 0, read)
                        actualBytes += read
                    }
                }
            } ?: return@withContext Result.failure(IllegalStateException("Seçilen dosya okunamadı."))

            val finalSize = if (actualBytes > 0L) actualBytes else reportedSize
            val detectedType = detectAttachmentType(displayName, mimeType)

            persistFileMapping(context, fileId, outFile)

            // Auto-start LocalMeshServer for LAN HTTP 206 Range transfers and large P2P files
            val signedIn = FirebaseAuth.getInstance().currentUser != null
            val mode = chooseTransferMode(finalSize, signedIn)
            if (mode != "CLOUD_CHUNKS") {
                LocalMeshServer.ensureStartedSync()
            }
            val lanUrl = LocalMeshServer.registerSharedFile(fileId, outFile, displayName, mimeType)

            val chunkCount = if (mode == "CLOUD_CHUNKS") {
                ((finalSize + CHUNK_RAW_BYTES - 1) / CHUNK_RAW_BYTES).toInt().coerceAtLeast(1)
            } else {
                0
            }

            // For P2P_WEBRTC and LAN_HTTP we store lanUrl (`http://<ip>:<port>/api/files/$fileId`).
            // The trailing `/$fileId` doubles as the cross-network WebRTC fileId if LAN is unreachable.
            val urlOrUri = when (mode) {
                "P2P_WEBRTC", "LAN_HTTP" -> lanUrl
                else -> outFile.absolutePath
            }

            Result.success(
                PreparedAttachment(
                    fileId = fileId,
                    localFile = outFile,
                    displayName = displayName,
                    sizeBytes = finalSize,
                    mimeType = mimeType,
                    detectedType = detectedType,
                    transferMode = mode,
                    urlOrUri = urlOrUri,
                    chunkCount = chunkCount
                )
            )
        } catch (e: Exception) {
            destFile?.let { if (it.exists()) it.delete() }
            Result.failure(e)
        }
    }

    suspend fun uploadCloudChunksIfNeeded(
        context: Context,
        conversationId: String,
        messageId: String,
        participantIds: List<String>,
        prepared: PreparedAttachment
    ) = withContext(Dispatchers.IO) {
        if (prepared.transferMode != "CLOUD_CHUNKS" || prepared.chunkCount <= 0) return@withContext
        if (FirebaseAuth.getInstance().currentUser == null) return@withContext

        try {
            val repo = FirestoreChatRepository.create(context)
            val total = prepared.chunkCount
            updateProgress(messageId, 0.02f)

            FileInputStream(prepared.localFile).use { fis ->
                val buffer = ByteArray(CHUNK_RAW_BYTES)
                var chunkIndex = 0
                while (chunkIndex < total) {
                    val read = fis.read(buffer)
                    if (read <= 0) break
                    val slice = if (read == buffer.size) buffer else buffer.copyOf(read)
                    val b64 = Base64.encodeToString(slice, Base64.NO_WRAP)
                    repo.uploadMessageFileChunk(
                        conversationId = conversationId,
                        messageId = messageId,
                        participantIds = participantIds,
                        chunkIndex = chunkIndex,
                        totalChunks = total,
                        dataBase64 = b64
                    ).getOrThrow()
                    chunkIndex++
                    updateProgress(messageId, chunkIndex.toFloat() / total.toFloat())
                }
            }
            updateProgress(messageId, 1f)
        } catch (_: Exception) {
            updateProgress(messageId, -1f)
        }
    }

    suspend fun ensureLocalFileAvailable(
        context: Context,
        conversationId: String,
        messageId: String,
        attachmentName: String,
        attachmentUrl: String?,
        transferMode: String?,
        senderUid: String? = null,
        expectedSizeBytes: Long = 0L
    ): Result<File> = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "shared_files").apply { mkdirs() }
        val safeName = attachmentName.replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
        val cachedDest = File(dir, "dl_${messageId}_$safeName")
        val extractedFileId = attachmentUrl
            ?.substringAfterLast('/')
            ?.substringBefore('?')
            ?.takeIf { it.startsWith("att_") }

        try {
            // 1. Check direct local path or registered host fileId
            if (!attachmentUrl.isNullOrBlank() && !attachmentUrl.startsWith("http")) {
                val direct = File(attachmentUrl)
                if (direct.exists() && direct.length() > 0L) {
                    return@withContext Result.success(direct)
                }
            }
            if (extractedFileId != null) {
                resolveRegisteredFile(context, extractedFileId)?.let { hostFile ->
                    return@withContext Result.success(hostFile)
                }
            }

            if (cachedDest.exists() && cachedDest.length() > 0L &&
                (expectedSizeBytes <= 0L || cachedDest.length() >= expectedSizeBytes)
            ) {
                return@withContext Result.success(cachedDest)
            }

            if (expectedSizeBytes > 0L && !ensureDiskSpaceOrEvict(dir, expectedSizeBytes)) {
                val free = availableBytes(dir)
                return@withContext Result.failure(
                    IllegalStateException(
                        "Dosyayı indirmek için cihazda yeterli boş alan yok (${formatByteSize(expectedSizeBytes)} gerekli, ${formatByteSize(free)} boş)."
                    )
                )
            }

            // 2. Try fast LAN HTTP stream if sender is on same Wi-Fi/Hotspot (short connectTimeout so P2P kicks in fast)
            if (!attachmentUrl.isNullOrBlank() && attachmentUrl.startsWith("http")) {
                try {
                    updateProgress(messageId, 0.03f)
                    val conn = (URL(attachmentUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 2500
                        readTimeout = 0 // Unlimited duration once connected
                    }
                    if (conn.responseCode in 200..299) {
                        val totalLen = conn.contentLengthLong.takeIf { it > 0L } ?: expectedSizeBytes
                        conn.inputStream.use { input ->
                            FileOutputStream(cachedDest).use { output ->
                                val buf = ByteArray(STREAM_BUFFER_BYTES)
                                var copied = 0L
                                while (true) {
                                    val r = input.read(buf)
                                    if (r == -1) break
                                    output.write(buf, 0, r)
                                    copied += r
                                    if (totalLen > 0L) {
                                        updateProgress(
                                            messageId,
                                            (copied.toFloat() / totalLen.toFloat()).coerceIn(0.03f, 0.99f)
                                        )
                                    }
                                }
                            }
                        }
                        updateProgress(messageId, 1f)
                        if (cachedDest.exists() && cachedDest.length() > 0L) {
                            return@withContext Result.success(cachedDest)
                        }
                    }
                } catch (_: Exception) {
                    if (cachedDest.exists()) cachedDest.delete()
                }
            }

            // 3. Cross-network Peer-to-Peer WebRTC DataChannel stream (supports 1.5 GB - 4 GB)
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            val shouldTryP2P = currentUid != null &&
                !senderUid.isNullOrBlank() &&
                senderUid != currentUid &&
                extractedFileId != null &&
                (transferMode == "P2P_WEBRTC" || expectedSizeBytes > CLOUD_CHUNK_CAP_BYTES)
            if (shouldTryP2P) {
                updateProgress(messageId, 0.02f)
                val p2pRes = WebRtcFileTransferEngine.downloadFileOverP2P(
                    context = context,
                    ownerUid = senderUid!!,
                    conversationId = conversationId,
                    messageId = messageId,
                    fileId = extractedFileId!!,
                    fileName = attachmentName,
                    expectedSizeBytes = expectedSizeBytes,
                    destFile = cachedDest,
                    onProgress = { pct -> updateProgress(messageId, pct) }
                )
                if (p2pRes.isSuccess) {
                    updateProgress(messageId, 1f)
                    return@withContext p2pRes
                }
                if (cachedDest.exists()) cachedDest.delete()
                updateProgress(messageId, -1f)
                return@withContext Result.failure(
                    p2pRes.exceptionOrNull()
                        ?: IllegalStateException("P2P transfer başarısız. 1,5 GB+ dosyalar için gönderici cihazın çevrimiçi olması gerekir.")
                )
            }

            // 4. Async Firestore Cloud Chunks fallback (<= 100 MB)
            if (currentUid != null &&
                (transferMode == "CLOUD_CHUNKS" || !attachmentUrl.isNullOrBlank())
            ) {
                updateProgress(messageId, 0.08f)
                val repo = FirestoreChatRepository.create(context)
                val chunks = repo.downloadMessageFileChunks(conversationId, messageId).getOrThrow()
                if (chunks.isNotEmpty()) {
                    FileOutputStream(cachedDest).use { fos ->
                        chunks.forEachIndexed { i, (_, b64) ->
                            val bytes = Base64.decode(b64, Base64.NO_WRAP)
                            fos.write(bytes)
                            updateProgress(messageId, (i + 1).toFloat() / chunks.size.toFloat())
                        }
                    }
                    updateProgress(messageId, 1f)
                    return@withContext Result.success(cachedDest)
                }
            }

            Result.failure(IllegalStateException("Dosya henüz yerel olarak indirilemedi."))
        } catch (e: Exception) {
            if (cachedDest.exists() && (expectedSizeBytes <= 0L || cachedDest.length() < expectedSizeBytes)) {
                cachedDest.delete()
            }
            updateProgress(messageId, -1f)
            Result.failure(e)
        }
    }

    fun openFileWithSystem(context: Context, file: File, mimeType: String?) {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val resolvedMime = mimeType?.takeIf { it.isNotBlank() } ?: guessMimeType(file.name)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, resolvedMime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Dosyayı Aç"))
    }

    private fun updateProgress(key: String, value: Float) {
        val map = _transferProgress.value.toMutableMap()
        if (value < 0f) {
            map.remove(key)
        } else {
            map[key] = value
        }
        _transferProgress.value = map
    }

    private fun detectAttachmentType(fileName: String, mimeType: String): String {
        val lowerName = fileName.lowercase()
        val lowerMime = mimeType.lowercase()
        return when {
            lowerName.endsWith(".apk") || lowerMime.contains("android.package-archive") -> "APK"
            lowerMime.startsWith("image/") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".webp") || lowerName.endsWith(".gif") -> "IMAGE"
            lowerMime.startsWith("video/") || lowerName.endsWith(".mp4") || lowerName.endsWith(".mkv") ||
                lowerName.endsWith(".webm") || lowerName.endsWith(".3gp") || lowerName.endsWith(".mov") -> "VIDEO"
            lowerMime.contains("pdf") || lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") ||
                lowerName.endsWith(".docx") || lowerName.endsWith(".txt") || lowerName.endsWith(".xlsx") -> "DOCUMENT"
            else -> "FILE"
        }
    }

    private fun guessMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
