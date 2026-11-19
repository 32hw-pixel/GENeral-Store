package com.example.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.DirectMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Zero-permission MediaStore gallery saver for both sent and received images.
 * Uses Android Scoped Storage (API 29+ RELATIVE_PATH + IS_PENDING) with zero
 * broad storage permissions (compliant with Android Scoped Storage & GitHub Releases security practices).
 */
object MediaGallerySaver {

    suspend fun saveMessageImageToGallery(
        context: Context,
        message: DirectMessage,
        senderUid: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val rawName = message.attachmentName?.takeIf { it.isNotBlank() }
                ?: "general_chat_${System.currentTimeMillis()}.jpg"
            val mimeType = resolveImageMimeType(message.attachmentMimeType, rawName)
            val displayName = ensureExtension(rawName, mimeType)

            val rawUrl = message.attachmentUrl ?: message.attachmentUri
            val localFileRes = UnlimitedFileTransferManager.ensureLocalFileAvailable(
                context = context,
                conversationId = message.conversationId,
                messageId = message.id,
                attachmentName = displayName,
                attachmentUrl = rawUrl,
                transferMode = message.transferMode,
                senderUid = senderUid,
                expectedSizeBytes = message.attachmentSizeBytes
            )

            val sourceStream: InputStream = when {
                localFileRes.isSuccess -> {
                    localFileRes.getOrThrow().inputStream()
                }
                !rawUrl.isNullOrBlank() && rawUrl.startsWith("content://") -> {
                    context.contentResolver.openInputStream(Uri.parse(rawUrl))
                        ?: throw IllegalStateException("Görsel içeriği okunamadı.")
                }
                !rawUrl.isNullOrBlank() && File(rawUrl).exists() -> {
                    File(rawUrl).inputStream()
                }
                !rawUrl.isNullOrBlank() && (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) -> {
                    val conn = (URL(rawUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15_000
                        readTimeout = 30_000
                    }
                    if (conn.responseCode !in 200..299) {
                        throw IllegalStateException("Sunucu hatası (HTTP ${conn.responseCode})")
                    }
                    conn.inputStream
                }
                else -> throw IllegalStateException("Görsel kaynağı bulunamadı veya indirilemedi.")
            }

            saveStreamToMediaStore(
                context = context,
                displayName = displayName,
                mimeType = mimeType,
                inputStream = sourceStream
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveStreamToMediaStore(
        context: Context,
        displayName: String,
        mimeType: String,
        inputStream: InputStream
    ): Result<Uri> {
        return try {
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/GENeralChat")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values)
                    ?: return Result.failure(IllegalStateException("Galeri kaydı oluşturulamadı."))
                try {
                    resolver.openOutputStream(uri)?.use { out ->
                        inputStream.use { it.copyTo(out) }
                    } ?: throw IllegalStateException("Galeri akışı açılamadı.")
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    Result.success(uri)
                } catch (err: Exception) {
                    runCatching { resolver.delete(uri, null, null) }
                    Result.failure(err)
                }
            } else {
                val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: File(context.filesDir, "GENeralChatPictures")
                val targetDir = File(picturesDir, "GENeralChat").apply { mkdirs() }
                val dest = File(targetDir, displayName)
                inputStream.use { input ->
                    dest.outputStream().use { out -> input.copyTo(out) }
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(dest.absolutePath),
                    arrayOf(mimeType),
                    null
                )
                Result.success(Uri.fromFile(dest))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resolveImageMimeType(declared: String?, fileName: String): String {
        if (!declared.isNullOrBlank() && declared.startsWith("image/")) return declared
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".gif") -> "image/gif"
            else -> "image/jpeg"
        }
    }

    private fun ensureExtension(name: String, mimeType: String): String {
        val ext = when (mimeType) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            else -> ".jpg"
        }
        val clean = name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return if (clean.contains('.')) clean else "$clean$ext"
    }
}
