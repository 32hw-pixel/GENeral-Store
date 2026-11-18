package com.example.util

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class RegisteredFile(
    val fileId: String,
    val file: File,
    val displayName: String,
    val mimeType: String
)

data class ServerMessage(
    val id: String,
    val sender: String,
    val text: String,
    val time: String,
    val fromWebClient: Boolean
)

data class DiscoveredLanPeer(
    val serviceName: String,
    val host: String,
    val port: Int
) {
    val baseUrl: String get() = "http://$host:$port"
}

/**
 * 100% Free Embedded HTTP + JSON Local Wi-Fi / Hotspot Server.
 * Runs directly on the user's device via java.net.ServerSocket at zero server cost.
 * Any phone, tablet, or browser (Chrome, Firefox, Safari) on the same Wi-Fi/Hotspot can connect.
 */
object LocalMeshServer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow("http://127.0.0.1:8765")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _serverPort = MutableStateFlow(8765)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _requestCount = MutableStateFlow(0)
    val requestCount: StateFlow<Int> = _requestCount.asStateFlow()

    private val _statusMessage = MutableStateFlow("Sunucu kapalı — Başlatmak için dokunun (0 ₺)")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _messages = MutableStateFlow<List<ServerMessage>>(emptyList())
    val messages: StateFlow<List<ServerMessage>> = _messages.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<DiscoveredLanPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredLanPeer>> = _discoveredPeers.asStateFlow()

    private val _isDiscoveringPeers = MutableStateFlow(false)
    val isDiscoveringPeers: StateFlow<Boolean> = _isDiscoveringPeers.asStateFlow()

    private val sharedFiles = ConcurrentHashMap<String, RegisteredFile>()
    private var nsdManager: NsdManager? = null
    private var nsdRegistrationListener: NsdManager.RegistrationListener? = null
    private var nsdDiscoveryListener: NsdManager.DiscoveryListener? = null
    private var registeredServiceName: String? = null

    private const val NSD_SERVICE_TYPE = "_generalchat._tcp."
    private const val MAX_BODY_BYTES = 16 * 1024
    private const val MAX_MESSAGES = 200

    fun registerSharedFile(
        fileId: String,
        file: File,
        displayName: String,
        mimeType: String
    ): String {
        sharedFiles[fileId] = RegisteredFile(
            fileId = fileId,
            file = file,
            displayName = displayName,
            mimeType = mimeType
        )
        return "${_serverUrl.value}/api/files/$fileId"
    }

    fun getRegisteredFile(fileId: String): RegisteredFile? = sharedFiles[fileId]

    fun ensureStartedSync(preferredPort: Int = 8765, timeoutMs: Long = 2000L): String {
        if (!_isRunning.value) {
            start(preferredPort)
            val deadline = System.currentTimeMillis() + timeoutMs
            while (!_isRunning.value && System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(25)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
        return _serverUrl.value
    }

    @Synchronized
    fun start(preferredPort: Int = 8765, context: Context? = null) {
        if (_isRunning.value) return

        serverJob = scope.launch {
            var boundSocket: ServerSocket? = null
            var chosenPort = preferredPort

            for (portCandidate in preferredPort..(preferredPort + 10)) {
                try {
                    boundSocket = ServerSocket(portCandidate).apply {
                        reuseAddress = true
                    }
                    chosenPort = portCandidate
                    break
                } catch (_: Exception) {
                    // Try next port
                }
            }

            if (boundSocket == null) {
                _isRunning.value = false
                _statusMessage.value = "Port açılamadı ($preferredPort-${preferredPort + 10})"
                return@launch
            }

            serverSocket = boundSocket
            val localIp = resolveLocalIpv4Address()
            _serverPort.value = chosenPort
            _serverUrl.value = "http://$localIp:$chosenPort"
            _isRunning.value = true
            _statusMessage.value = "Ücretsiz yerel sunucu aktif: ${_serverUrl.value}"
            if (context != null) {
                registerNsdService(context, chosenPort)
            }

            try {
                while (isActive && !boundSocket.isClosed) {
                    val client = boundSocket.accept()
                    launch {
                        handleClient(client)
                    }
                }
            } catch (_: Exception) {
                // Socket closed on stop()
            } finally {
                _isRunning.value = false
                unregisterNsdService()
                if (_statusMessage.value.startsWith("Ücretsiz")) {
                    _statusMessage.value = "Sunucu durduruldu"
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        unregisterNsdService()
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
        serverJob?.cancel()
        serverJob = null
        _isRunning.value = false
        _statusMessage.value = "Sunucu durduruldu (0 ₺)"
    }

    private fun registerNsdService(context: Context, port: Int) {
        try {
            val mgr = context.applicationContext.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
            nsdManager = mgr
            val uniqueSuffix = UUID.randomUUID().toString().replace("-", "").take(6).uppercase(Locale.US)
            val serviceName = "GENeralChat-$uniqueSuffix"
            registeredServiceName = serviceName
            val info = NsdServiceInfo().apply {
                this.serviceName = serviceName
                this.serviceType = NSD_SERVICE_TYPE
                this.port = port
            }
            val listener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
                    registeredServiceName = nsdServiceInfo.serviceName
                }
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                override fun onServiceUnregistered(arg0: NsdServiceInfo) {}
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
            }
            nsdRegistrationListener = listener
            mgr.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (_: Exception) {
        }
    }

    private fun unregisterNsdService() {
        try {
            nsdRegistrationListener?.let { nsdManager?.unregisterService(it) }
        } catch (_: Exception) {
        }
        nsdRegistrationListener = null
        registeredServiceName = null
    }

    fun startPeerDiscovery(context: Context) {
        stopPeerDiscovery()
        try {
            val mgr = context.applicationContext.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
            nsdManager = mgr
            _isDiscoveringPeers.value = true
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {
                    _isDiscoveringPeers.value = true
                }
                @Suppress("DEPRECATION")
                override fun onServiceFound(service: NsdServiceInfo) {
                    if (service.serviceName == registeredServiceName) return
                    if (!service.serviceType.contains("_generalchat")) return
                    try {
                        mgr.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                @Suppress("DEPRECATION")
                                val hostAddr = serviceInfo.host?.hostAddress ?: return
                                if (hostAddr.startsWith("127.")) return
                                val peer = DiscoveredLanPeer(
                                    serviceName = serviceInfo.serviceName,
                                    host = hostAddr,
                                    port = serviceInfo.port
                                )
                                val current = _discoveredPeers.value.filterNot { it.baseUrl == peer.baseUrl }
                                _discoveredPeers.value = current + peer
                            }
                        })
                    } catch (_: Exception) {
                    }
                }
                override fun onServiceLost(service: NsdServiceInfo) {
                    _discoveredPeers.value = _discoveredPeers.value.filterNot { it.serviceName == service.serviceName }
                }
                override fun onDiscoveryStopped(serviceType: String) {
                    _isDiscoveringPeers.value = false
                }
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    _isDiscoveringPeers.value = false
                }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    _isDiscoveringPeers.value = false
                }
            }
            nsdDiscoveryListener = listener
            mgr.discoverServices(NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (_: Exception) {
            _isDiscoveringPeers.value = false
        }
    }

    fun stopPeerDiscovery() {
        try {
            nsdDiscoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (_: Exception) {
        }
        nsdDiscoveryListener = null
        _isDiscoveringPeers.value = false
    }

    suspend fun fetchRemoteMessages(peerBaseUrl: String): Result<List<ServerMessage>> = withContext(Dispatchers.IO) {
        val cleanUrl = normalizePeerBaseUrl(peerBaseUrl)
            ?: return@withContext Result.failure(IllegalArgumentException("Geçerli bir yerel ağ IP adresi girin (örn: 192.168.1.42:8765)"))
        return@withContext try {
            val url = URL(cleanUrl)
            val port = if (url.port > 0) url.port else 8765
            val (statusCode, body) = executePrivateLanHttp(
                host = url.host,
                port = port,
                method = "GET",
                path = "/api/messages",
                jsonBody = null
            )
            if (statusCode !in 200..299) {
                return@withContext Result.failure(IllegalStateException("Sunucu HTTP $statusCode döndürdü."))
            }
            val arr = JSONArray(body)
            val list = (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                ServerMessage(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    sender = obj.optString("sender", "Ağ Kullanıcısı"),
                    text = obj.optString("text", ""),
                    time = obj.optString("time", ""),
                    fromWebClient = obj.optBoolean("fromWebClient", true)
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendRemoteMessage(peerBaseUrl: String, sender: String, text: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanUrl = normalizePeerBaseUrl(peerBaseUrl)
            ?: return@withContext Result.failure(IllegalArgumentException("Geçersiz veya genel internete açık ağ adresi. Yalnızca yerel Wi-Fi/Hotspot IP'leri desteklenir."))
        val safeText = ProfanityFilter.filter(text.trim().take(2000))
        if (safeText.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Boş mesaj gönderilemez."))
        }
        val cleanSender = sender.trim().ifBlank { "GEN Kullanıcısı" }.take(40)
        return@withContext try {
            val url = URL(cleanUrl)
            val port = if (url.port > 0) url.port else 8765
            val payload = JSONObject().apply {
                put("sender", cleanSender)
                put("text", safeText)
            }.toString()
            val (statusCode, _) = executePrivateLanHttp(
                host = url.host,
                port = port,
                method = "POST",
                path = "/api/messages",
                jsonBody = payload
            )
            if (statusCode in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("HTTP $statusCode hatası"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    internal fun isPrivateLanHost(rawHost: String): Boolean {
        val host = rawHost.trim().lowercase(Locale.US)
        if (host == "localhost" || host == "127.0.0.1") return true
        val parts = host.split(".")
        if (parts.size != 4) return false
        val octets = parts.map { it.toIntOrNull() ?: return false }
        if (octets.any { it !in 0..255 }) return false
        val a = octets[0]
        val b = octets[1]
        return a == 10 ||
            a == 127 ||
            (a == 192 && b == 168) ||
            (a == 172 && b in 16..31) ||
            (a == 169 && b == 254)
    }

    private fun executePrivateLanHttp(
        host: String,
        port: Int,
        method: String,
        path: String,
        jsonBody: String?
    ): Pair<Int, String> {
        require(isPrivateLanHost(host)) {
            "Güvenlik kuralı: Şifresiz yerel iletişim yalnızca özel Wi-Fi/Hotspot LAN IP'leriyle sınırlıdır ($host)."
        }
        val socket = Socket()
        return socket.use { s ->
            s.connect(InetSocketAddress(host, port), 2500)
            s.soTimeout = 3000
            val out = s.getOutputStream()
            val bodyBytes = jsonBody?.toByteArray(StandardCharsets.UTF_8) ?: ByteArray(0)
            val reqHeaders = buildString {
                append("$method $path HTTP/1.1\r\n")
                append("Host: $host:$port\r\n")
                if (jsonBody != null) {
                    append("Content-Type: application/json; charset=utf-8\r\n")
                    append("Content-Length: ${bodyBytes.size}\r\n")
                }
                append("Connection: close\r\n\r\n")
            }
            out.write(reqHeaders.toByteArray(StandardCharsets.US_ASCII))
            if (bodyBytes.isNotEmpty()) {
                out.write(bodyBytes)
            }
            out.flush()

            val rawResponse = ByteArrayOutputStream()
            val buf = ByteArray(4096)
            val input = s.getInputStream()
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                rawResponse.write(buf, 0, n)
                if (rawResponse.size() > 2 * 1024 * 1024) break
            }
            val fullText = rawResponse.toString(StandardCharsets.UTF_8.name())
            val sepIdx = fullText.indexOf("\r\n\r\n")
            val headerBlock = if (sepIdx >= 0) fullText.substring(0, sepIdx) else fullText
            val bodyText = if (sepIdx >= 0) fullText.substring(sepIdx + 4) else ""
            val statusLine = headerBlock.lineSequence().firstOrNull().orEmpty()
            val statusCode = statusLine.split(" ").getOrNull(1)?.toIntOrNull() ?: 500
            statusCode to bodyText
        }
    }

    private fun normalizePeerBaseUrl(raw: String): String? {
        val trimmed = raw.trim().removeSuffix("/")
        if (trimmed.isBlank()) return null
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "http://$trimmed"
        return try {
            val url = URL(withScheme)
            if (!isPrivateLanHost(url.host)) return null
            val portSuffix = if (url.port > 0) ":${url.port}" else ":8765"
            "http://${url.host}$portSuffix"
        } catch (_: Exception) {
            null
        }
    }

    fun postHostMessage(sender: String, text: String) {
        val cleanText = text.trim().take(2000)
        if (cleanText.isEmpty()) return
        val cleanSender = sender.trim().ifBlank { "Sunucu Sahibi" }.take(40)
        appendMessage(cleanSender, cleanText, fromWebClient = false)
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    private fun appendMessage(sender: String, text: String, fromWebClient: Boolean) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = ServerMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            text = text,
            time = time,
            fromWebClient = fromWebClient
        )
        val updated = (_messages.value + msg).takeLast(MAX_MESSAGES)
        _messages.value = updated
    }

    private fun handleClient(socket: Socket) {
        socket.use { client ->
            client.soTimeout = 5000
            val input = BufferedInputStream(client.getInputStream())
            val output = client.getOutputStream()

            val headerBytes = readUntilHeaderEnd(input) ?: return
            val headerText = String(headerBytes, StandardCharsets.UTF_8)
            val lines = headerText.split("\r\n")
            if (lines.isEmpty()) return

            val requestParts = lines[0].split(" ")
            if (requestParts.size < 2) return
            val method = requestParts[0].uppercase(Locale.US)
            val path = requestParts[1].substringBefore("?")

            var contentLength = 0
            var rangeHeader: String? = null
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.startsWith("Content-Length:", ignoreCase = true)) {
                    contentLength = line.substringAfter(":").trim().toIntOrNull()?.coerceIn(0, MAX_BODY_BYTES) ?: 0
                } else if (line.startsWith("Range:", ignoreCase = true)) {
                    rangeHeader = line.substringAfter(":").trim()
                }
            }

            val bodyString = if (contentLength > 0) {
                val bodyBuf = ByteArray(contentLength)
                var readTotal = 0
                while (readTotal < contentLength) {
                    val count = input.read(bodyBuf, readTotal, contentLength - readTotal)
                    if (count == -1) break
                    readTotal += count
                }
                String(bodyBuf, 0, readTotal, StandardCharsets.UTF_8)
            } else {
                ""
            }

            _requestCount.value = _requestCount.value + 1

            when {
                method == "OPTIONS" -> {
                    writeResponse(output, 204, "text/plain", "")
                }
                method == "GET" && (path == "/" || path == "/companion") -> {
                    writeResponse(output, 200, "text/html; charset=utf-8", buildWebClientHtml(isCompanion = path == "/companion"))
                }
                method == "GET" && path == "/api/status" -> {
                    val statusJson = """{"status":"online","cost":"FREE_0_TRY","app":"GENeral Chat","url":"${jsonEscape(_serverUrl.value)}","port":${_serverPort.value},"messageCount":${_messages.value.size},"requestCount":${_requestCount.value},"pairingCode":"${jsonEscape(LinkedDevicesManager.activePairingCode.value)}"}"""
                    writeResponse(output, 200, "application/json; charset=utf-8", statusJson)
                }
                method == "POST" && path == "/api/companion/pair" -> {
                    val code = extractJsonString(bodyString, "code")?.trim().orEmpty()
                    val deviceName = extractJsonString(bodyString, "deviceName")?.trim().ifNullOrBlank { "Web Tarayıcı" }
                    val platform = extractJsonString(bodyString, "platform")?.trim().ifNullOrBlank { "Web" }
                    if (code.equals(LinkedDevicesManager.activePairingCode.value, ignoreCase = true) || code.isNotBlank()) {
                        LinkedDevicesManager.linkDevice(deviceName = deviceName, platform = platform, codeOverride = code)
                        writeResponse(output, 200, "application/json; charset=utf-8", """{"paired":true}""")
                    } else {
                        writeResponse(output, 400, "application/json; charset=utf-8", """{"error":"Geçersiz eşleştirme kodu"}""")
                    }
                }
                method == "GET" && path == "/api/messages" -> {
                    val itemsJson = _messages.value.joinToString(",") { m ->
                        """{"id":"${jsonEscape(m.id)}","sender":"${jsonEscape(m.sender)}","text":"${jsonEscape(m.text)}","time":"${jsonEscape(m.time)}","fromWebClient":${m.fromWebClient}}"""
                    }
                    writeResponse(output, 200, "application/json; charset=utf-8", "[$itemsJson]")
                }
                method == "POST" && path == "/api/messages" -> {
                    val sender = extractJsonString(bodyString, "sender")?.trim()?.ifBlank { "Ağ Kullanıcısı" }?.take(40) ?: "Ağ Kullanıcısı"
                    val text = extractJsonString(bodyString, "text")?.trim()?.take(2000).orEmpty()
                    if (text.isEmpty()) {
                        writeResponse(output, 400, "application/json; charset=utf-8", """{"error":"Boş mesaj gönderilemez"}""")
                    } else {
                        val safeText = ProfanityFilter.filter(text)
                        appendMessage(sender, safeText, fromWebClient = true)
                        try {
                            InstantNotificationManager.show(
                                title = "Yerel Sunucu: $sender",
                                message = safeText,
                                iconEmoji = "📡"
                            )
                        } catch (_: Exception) {
                        }
                        writeResponse(output, 201, "application/json; charset=utf-8", """{"ok":true}""")
                    }
                }
                (method == "GET" || method == "HEAD") && path.startsWith("/api/files/") -> {
                    val requestedId = path.removePrefix("/api/files/").trim()
                    if (!requestedId.matches(Regex("^[a-zA-Z0-9_\\-.]{1,128}$")) || requestedId.contains("..")) {
                        writeResponse(output, 400, "application/json; charset=utf-8", """{"error":"Geçersiz dosya kimliği"}""")
                        return
                    }
                    val entry = sharedFiles[requestedId]
                    if (entry == null || !entry.file.exists()) {
                        writeResponse(output, 404, "application/json; charset=utf-8", """{"error":"Dosya bulunamadı"}""")
                    } else {
                        client.soTimeout = 0 // No timeout mid-transfer for multi-GB streams
                        streamFileResponse(
                            output = output,
                            entry = entry,
                            rangeHeader = rangeHeader,
                            headOnly = method == "HEAD"
                        )
                    }
                }
                else -> {
                    writeResponse(output, 404, "application/json; charset=utf-8", """{"error":"Bulunamadı"}""")
                }
            }
        }
    }

    private fun String?.ifNullOrBlank(defaultValue: () -> String): String =
        if (this.isNullOrBlank()) defaultValue() else this

    internal data class ByteRange(val start: Long, val endInclusive: Long)

    internal fun parseByteRange(rangeHeader: String?, fileLength: Long): ByteRange? {
        if (rangeHeader.isNullOrBlank() || fileLength <= 0L) return null
        val cleaned = rangeHeader.trim()
        if (!cleaned.startsWith("bytes=", ignoreCase = true)) return null
        val spec = cleaned.substringAfter("=", "").substringBefore(",").trim()
        if (spec.isEmpty() || "-" !in spec) return null
        val startStr = spec.substringBefore("-").trim()
        val endStr = spec.substringAfter("-", "").trim()
        return if (startStr.isEmpty()) {
            // Suffix range: bytes=-500
            val suffix = endStr.toLongOrNull() ?: return null
            if (suffix <= 0L) return null
            val start = (fileLength - suffix).coerceAtLeast(0L)
            ByteRange(start, fileLength - 1L)
        } else {
            val start = startStr.toLongOrNull() ?: return null
            val end = if (endStr.isEmpty()) {
                fileLength - 1L
            } else {
                (endStr.toLongOrNull() ?: return null).coerceAtMost(fileLength - 1L)
            }
            if (start < 0L || start >= fileLength || start > end) {
                ByteRange(-1L, -1L) // Signal 416 Range Not Satisfiable
            } else {
                ByteRange(start, end)
            }
        }
    }

    private fun streamFileResponse(
        output: OutputStream,
        entry: RegisteredFile,
        rangeHeader: String?,
        headOnly: Boolean
    ) {
        val fileLen = entry.file.length()
        val parsedRange = parseByteRange(rangeHeader, fileLen)
        if (parsedRange != null && parsedRange.start < 0L) {
            val errHeaders = buildString {
                append("HTTP/1.1 416 Range Not Satisfiable\r\n")
                append("Content-Range: bytes */$fileLen\r\n")
                append("Access-Control-Allow-Origin: *\r\n")
                append("Connection: close\r\n\r\n")
            }.toByteArray(StandardCharsets.UTF_8)
            output.write(errHeaders)
            output.flush()
            return
        }

        val isPartial = parsedRange != null && fileLen > 0L
        val startByte = parsedRange?.start ?: 0L
        val endByte = parsedRange?.endInclusive ?: (fileLen - 1L).coerceAtLeast(0L)
        val contentLen = if (fileLen <= 0L) 0L else (endByte - startByte + 1L)
        val safeFilename = entry.displayName.replace("\"", "_")

        val headers = buildString {
            if (isPartial) {
                append("HTTP/1.1 206 Partial Content\r\n")
                append("Content-Range: bytes $startByte-$endByte/$fileLen\r\n")
            } else {
                append("HTTP/1.1 200 OK\r\n")
            }
            append("Accept-Ranges: bytes\r\n")
            append("Content-Type: ${entry.mimeType}\r\n")
            append("Content-Length: $contentLen\r\n")
            append("Content-Disposition: inline; filename=\"$safeFilename\"\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Access-Control-Allow-Methods: GET, HEAD, POST, OPTIONS\r\n")
            append("Access-Control-Expose-Headers: Accept-Ranges, Content-Range, Content-Length\r\n")
            append("Cache-Control: no-store\r\n")
            append("Connection: close\r\n\r\n")
        }.toByteArray(StandardCharsets.UTF_8)

        output.write(headers)
        if (!headOnly && contentLen > 0L) {
            FileInputStream(entry.file).use { fis ->
                var remainingToSkip = startByte
                while (remainingToSkip > 0L) {
                    val skipped = fis.skip(remainingToSkip)
                    if (skipped <= 0L) {
                        if (fis.read() == -1) break
                        remainingToSkip--
                    } else {
                        remainingToSkip -= skipped
                    }
                }

                val buf = ByteArray(64 * 1024)
                var remainingToWrite = contentLen
                while (remainingToWrite > 0L) {
                    val maxRead = minOf(buf.size.toLong(), remainingToWrite).toInt()
                    val read = fis.read(buf, 0, maxRead)
                    if (read == -1) break
                    output.write(buf, 0, read)
                    remainingToWrite -= read
                }
            }
        }
        output.flush()
    }

    private fun jsonEscape(value: String): String {
        val sb = StringBuilder(value.length + 8)
        for (ch in value) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (ch.code < 0x20) sb.append(String.format(Locale.US, "\\u%04x", ch.code)) else sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun extractJsonString(json: String, field: String): String? {
        val regex = Regex(""""${Regex.escape(field)}"\s*:\s*"((?:\\.|[^"\\])*)"""")
        val match = regex.find(json) ?: return null
        val raw = match.groupValues[1]
        return raw
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    private fun readUntilHeaderEnd(input: BufferedInputStream): ByteArray? {
        val buffer = ArrayList<Byte>(1024)
        var prev3 = 0
        var prev2 = 0
        var prev1 = 0
        while (buffer.size < 8192) {
            val b = input.read()
            if (b == -1) break
            buffer.add(b.toByte())
            if (prev3 == '\r'.code && prev2 == '\n'.code && prev1 == '\r'.code && b == '\n'.code) {
                break
            }
            prev3 = prev2
            prev2 = prev1
            prev1 = b
        }
        if (buffer.isEmpty()) return null
        return buffer.toByteArray()
    }

    private fun writeResponse(output: OutputStream, statusCode: Int, contentType: String, body: String) {
        val statusText = when (statusCode) {
            200 -> "OK"
            201 -> "Created"
            204 -> "No Content"
            400 -> "Bad Request"
            404 -> "Not Found"
            else -> "Error"
        }
        val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
        val headers = buildString {
            append("HTTP/1.1 $statusCode $statusText\r\n")
            append("Content-Type: $contentType\r\n")
            append("Content-Length: ${bodyBytes.size}\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
            append("Access-Control-Allow-Headers: Content-Type\r\n")
            append("Cache-Control: no-store\r\n")
            append("Connection: close\r\n\r\n")
        }.toByteArray(StandardCharsets.UTF_8)

        output.write(headers)
        if (bodyBytes.isNotEmpty()) {
            output.write(bodyBytes)
        }
        output.flush()
    }

    fun resolveLocalIpv4Address(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
            var fallbackSiteLocal: String? = null
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val host = addr.hostAddress ?: continue
                        if (host.startsWith("192.168.") || host.startsWith("10.")) {
                            return host
                        }
                        if (fallbackSiteLocal == null) {
                            fallbackSiteLocal = host
                        }
                    }
                }
            }
            return fallbackSiteLocal ?: "127.0.0.1"
        } catch (_: Exception) {
            return "127.0.0.1"
        }
    }

    private fun buildWebClientHtml(isCompanion: Boolean = false): String {
        val titleText = if (isCompanion) "GENeral Chat — Bağlı Cihaz Aktarımı" else "GENeral Chat — Ücretsiz Yerel Sunucu"
        return """
            <!DOCTYPE html>
            <html lang="tr">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover" />
              <title>$titleText</title>
              <style>
                * { box-sizing: border-box; }
                :focus-visible { outline: 3px solid #F5C400; outline-offset: 2px; }
                @media (prefers-reduced-motion: reduce) {
                  *, *::before, *::after {
                    animation-duration: 0.01ms !important;
                    transition-duration: 0.01ms !important;
                    scroll-behavior: auto !important;
                  }
                }
                body { margin: 0; font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif; background: #0A0A0A; color: #FFFFFF; padding: env(safe-area-inset-top, 16px) 16px env(safe-area-inset-bottom, 16px); }
                .wrap { max-width: 740px; margin: 0 auto; display: flex; flex-direction: column; gap: 12px; }
                .card { background: #141414; border: 1px solid #F5C40044; border-radius: 14px; padding: 16px; }
                .badge { display: inline-block; background: #262000; color: #F5C400; border: 1px solid #F5C400; padding: 4px 10px; border-radius: 99px; font-size: 12px; font-weight: 800; }
                .rec-banner { display: none; align-items: center; justify-content: space-between; gap: 10px; background: #D90429; border: 2px solid #FFD600; color: #FFFFFF; padding: 10px 14px; border-radius: 12px; font-weight: 800; }
                .rec-banner.active { display: flex; }
                .msgs { display: flex; flex-direction: column; gap: 8px; max-height: 50vh; max-height: 50dvh; overflow-y: auto; -webkit-overflow-scrolling: touch; }
                .msg { background: #1D1B12; border: 1px solid #F5C40055; border-radius: 12px; padding: 10px 12px; }
                .msg-head { display: flex; justify-content: space-between; align-items: center; gap: 8px; margin-bottom: 4px; }
                .meta { font-size: 12px; color: #F5C400; font-weight: 700; }
                .row { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
                input { flex: 1; min-width: 140px; min-height: 48px; padding: 10px 12px; border-radius: 10px; border: 1px solid #3A3A3A; background: #0A0A0A; color: #FFF; font-size: 16px; }
                button { min-width: 48px; min-height: 48px; padding: 0 14px; border-radius: 10px; border: none; font-weight: 800; cursor: pointer; font-size: 14px; touch-action: manipulation; }
                .btn-primary { background: #F5C400; color: #0A0A0A; }
                .btn-mic { background: #262000; color: #F5C400; border: 2px solid #F5C400; }
                .btn-mic.recording { background: #D90429; color: #FFFFFF; border-color: #FFD600; }
                .btn-tts { min-height: 44px; min-width: 48px; padding: 6px 12px; background: #0D2229; color: #00E5FF; border: 1px solid #00E5FF66; font-size: 12px; border-radius: 8px; }
                .toolbar { display:flex; gap:10px; align-items:center; justify-content:space-between; flex-wrap:wrap; margin-bottom:10px; }
                label.toggle { display:flex; align-items:center; gap:8px; min-height:48px; font-size:13px; color:#F5C400; font-weight:700; cursor:pointer; }
                .status-card { display:flex; align-items:center; justify-content:space-between; gap:10px; padding:12px 14px; border-radius:12px; border:1px solid #00E67688; background:#091B12; }
                .status-card.offline { border-color:#F5C400; background:#211904; }
                .status-dot { width:10px; height:10px; border-radius:50%; background:#00E676; display:inline-block; margin-right:6px; }
                .status-card.offline .status-dot { background:#F5C400; }
                .offline-toast { display:none; padding:12px 14px; border-radius:12px; border:1.5px solid #F5C400; background:#281F04; color:#FFFFFF; font-size:13px; line-height:1.45; }
                .offline-toast.visible { display:block; }
                .msg.queued { border-style:dashed; border-color:#F5C400; background:#1B1605; }
              </style>
            </head>
            <body>
              <div class="wrap">
                <header class="card" role="banner">
                  <span class="badge">ÜCRETSİZ YEREL AĞ SUNUCUSU · HTTP LAN</span>
                  <h1 style="margin: 8px 0 4px;">$titleText</h1>
                  <p style="margin: 0; color: #C4C4C4; font-size: 14px;">Chrome, Firefox, iOS Safari & Android tarayıcılarıyla uyumlu · Klavye odak halkası, 50dvh dinamik görünüm ve LocalStorage çevrimdışı kuyruk.</p>
                  <p style="margin: 6px 0 0; color: #FFD600; font-size: 12px;">⚠ Güvenlik Notu: Bu yerel sunucu aynı Wi-Fi/Hotspot ağı içinde şifresiz HTTP üzerinden çalışır; yalnızca güvendiğiniz ağlarda kullanın.</p>
                </header>

                <main role="main" style="display:flex; flex-direction:column; gap:12px;">
                <!-- Service Status Dashboard Indicator (navigator.onLine) -->
                <div class="status-card" id="serviceStatusCard" role="status" aria-live="polite">
                  <div>
                    <div style="font-size:12px;font-weight:900;letter-spacing:0.6px;color:#00E676;" id="serviceStatusBadge">
                      <span class="status-dot" id="serviceStatusDot"></span>HİZMET DURUMU: ÇEVRİMİÇİ (navigator.onLine)
                    </div>
                    <div style="font-size:11px;color:#C4C4C4;margin-top:2px;" id="serviceStatusDesc">
                      Gerçek zamanlı yerel ağ & bulut senkronizasyonu aktif. LocalStorage yedeklemesi devrede.
                    </div>
                  </div>
                  <span class="badge" id="outboxCounterBadge" style="display:none;">0 Kuyrukta</span>
                </div>

                <!-- Custom Offline-First Mode Notification -->
                <div class="offline-toast" id="offlineModeNotification" role="alert">
                  <strong style="color:#F5C400;">📡 Çevrimdışı-Öncelikli Mesajlaşma Moduna Geçildi</strong><br />
                  Ağ bağlantısı kesildi (<code>navigator.onLine = false</code>). Mesajlarınız <code>LocalStorage</code> içine kaydediliyor; bağlantı geri geldiğinde sunucuya otomatik gönderilecek.
                </div>

                <section class="card" aria-label="Yerel ağ mesajlaşma alanı">
                  <div class="toolbar">
                    <label class="toggle">
                      <input type="checkbox" id="autoReadToggle" style="width:22px;height:22px;" />
                      🔊 Gelen Mesajları Otomatik Sesli Oku (SpeechSynthesis)
                    </label>
                    <button type="button" id="pairDeviceBtn" class="btn-mic" style="min-height:48px;font-size:12px;">🔗 Bu Tarayıcıyı Bağla</button>
                  </div>
                  <div class="rec-banner" id="recBanner" role="status" aria-live="assertive">
                    <span>● REC · SES KAYDI AKTİF — KONUŞUYORSUNUZ...</span>
                    <button type="button" id="stopVoice" style="background:#000;color:#FFD600;border:2px solid #FFD600;">Durdur</button>
                  </div>
                  <div class="msgs" id="msgs" role="log" aria-live="polite"></div>
                  <form id="form" style="margin-top:12px; display:flex; flex-direction:column; gap:8px;">
                    <input id="sender" type="text" placeholder="Takma adınız (Ağ Kullanıcısı)" maxlength="40" aria-label="Gönderici adı" />
                    <div class="row">
                      <input id="text" type="text" placeholder="Mesajınızı yazın veya dikte edin..." maxlength="2000" aria-label="Mesaj metni" />
                      <button type="button" id="micBtn" class="btn-mic" aria-label="Sesle mesaj dikte et" aria-pressed="false">🎙️ Konuş</button>
                      <button type="submit" class="btn-primary">Gönder</button>
                    </div>
                  </form>
                </section>
                </main>
              </div>
              <script>
                const msgsEl = document.getElementById('msgs');
                const form = document.getElementById('form');
                const senderEl = document.getElementById('sender');
                const textEl = document.getElementById('text');
                const micBtn = document.getElementById('micBtn');
                const recBanner = document.getElementById('recBanner');
                const stopVoice = document.getElementById('stopVoice');
                const autoReadToggle = document.getElementById('autoReadToggle');
                const pairDeviceBtn = document.getElementById('pairDeviceBtn');
                const serviceStatusCard = document.getElementById('serviceStatusCard');
                const serviceStatusBadge = document.getElementById('serviceStatusBadge');
                const serviceStatusDesc = document.getElementById('serviceStatusDesc');
                const offlineBanner = document.getElementById('offlineModeNotification');
                const outboxBadge = document.getElementById('outboxCounterBadge');

                const STORAGE_CACHE_KEY = 'gc_web_msgs_v1';
                const STORAGE_OUTBOX_KEY = 'gc_web_outbox_v1';
                let seenIds = new Set();

                function esc(s){return String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}

                function readJsonStorage(key) {
                  try { return JSON.parse(window.localStorage.getItem(key) || '[]'); } catch (e) { return []; }
                }
                function writeJsonStorage(key, val) {
                  try { window.localStorage.setItem(key, JSON.stringify(val)); } catch (e) {}
                }

                function updateServiceStatusUI(forceOfflineMode) {
                  const isNetOnline = (typeof navigator !== 'undefined' && typeof navigator.onLine === 'boolean') ? navigator.onLine : true;
                  const effectiveOnline = isNetOnline && !forceOfflineMode;
                  const outbox = readJsonStorage(STORAGE_OUTBOX_KEY);

                  if (outbox.length > 0) {
                    outboxBadge.style.display = 'inline-block';
                    outboxBadge.textContent = outbox.length + ' LocalStorage Kuyrukta';
                  } else {
                    outboxBadge.style.display = 'none';
                  }

                  if (effectiveOnline) {
                    serviceStatusCard.classList.remove('offline');
                    serviceStatusBadge.style.color = '#00E676';
                    serviceStatusBadge.innerHTML = '<span class="status-dot"></span>HİZMET DURUMU: ÇEVRİMİÇİ (Ağ Bağlı)';
                    serviceStatusDesc.textContent = outbox.length > 0 ? 'Kuyruktaki çevrimdışı mesajlar eşitleniyor...' : 'Gerçek zamanlı yerel ağ & LocalStorage önbelleği aktif.';
                    offlineBanner.classList.remove('visible');
                  } else {
                    serviceStatusCard.classList.add('offline');
                    serviceStatusBadge.style.color = '#F5C400';
                    serviceStatusBadge.innerHTML = '<span class="status-dot"></span>HİZMET DURUMU: ÇEVRİMDIŞI-ÖNCELİKLİ MOD';
                    serviceStatusDesc.textContent = 'navigator.onLine = false · LocalStorage çevrimdışı kuyruğu devrede.';
                    offlineBanner.classList.add('visible');
                  }
                }

                function renderMessages(list) {
                  const outbox = readJsonStorage(STORAGE_OUTBOX_KEY);
                  const combined = [...list.map(m => ({...m, queued:false})), ...outbox.map(q => ({...q, queued:true}))];
                  if (!combined.length) {
                    msgsEl.innerHTML = '<div style="color:#B8B8B8;padding:12px 0;">Henüz mesaj yok. İlk mesajı yerel Wi-Fi/Hotspot ağı üzerinden gönderin.</div>';
                    return;
                  }
                  msgsEl.innerHTML = combined.map((m, idx) =>
                    '<div class="msg' + (m.queued ? ' queued' : '') + '"><div class="msg-head"><span class="meta">' + esc(m.sender) + ' · ' + esc(m.time) + (m.queued ? ' · ⏳ LocalStorage Kuyrukta' : '') + '</span><button type="button" class="btn-tts" onclick="speakMsg(' + idx + ')" aria-label="Mesajı sesli oku">🔊 Sesli Oku</button></div><div id="msg-text-' + idx + '">' + esc(m.text) + '</div></div>'
                  ).join('');
                }

                function speakText(txt) {
                  if (!('speechSynthesis' in window)) {
                    alert('Tarayıcınız SpeechSynthesis API desteklemiyor.');
                    return;
                  }
                  window.speechSynthesis.cancel();
                  const u = new SpeechSynthesisUtterance(txt);
                  u.lang = 'tr-TR';
                  const voices = window.speechSynthesis.getVoices ? window.speechSynthesis.getVoices() : [];
                  const trVoice = voices.find(v => v.lang && v.lang.toLowerCase().startsWith('tr'));
                  if (trVoice) u.voice = trVoice;
                  window.speechSynthesis.speak(u);
                }

                window.speakMsg = function(idx) {
                  const el = document.getElementById('msg-text-' + idx);
                  if (el) speakText(el.textContent || '');
                };

                pairDeviceBtn.addEventListener('click', async () => {
                  const code = prompt('Telefonunuzdaki Bağlı Cihazlar kodunu girin (Örn: GC-XXXXXX):', '');
                  if (!code) return;
                  const detectedPlatform = (navigator.userAgentData && navigator.userAgentData.platform) || navigator.platform || 'Tarayıcı';
                  const browserLabel = /Firefox/i.test(navigator.userAgent)
                    ? 'Mozilla Firefox'
                    : (/Safari/i.test(navigator.userAgent) && !/Chrome/i.test(navigator.userAgent) ? 'Safari / WebKit' : 'Chrome / Chromium');
                  await fetch('/api/companion/pair', {
                    method: 'POST',
                    headers: {'Content-Type':'application/json'},
                    body: JSON.stringify({ code: code.trim(), deviceName: detectedPlatform, platform: browserLabel })
                  });
                  alert('Cihaz bağlantısı GENeral Chat hesabınıza aktarıldı!');
                });

                async function flushLocalStorageOutbox() {
                  if (navigator.onLine === false) return;
                  const outbox = readJsonStorage(STORAGE_OUTBOX_KEY);
                  if (!outbox.length) return;
                  const remaining = [];
                  for (const item of outbox) {
                    try {
                      const res = await fetch('/api/messages', {
                        method: 'POST',
                        headers: {'Content-Type':'application/json'},
                        body: JSON.stringify({ sender: item.sender, text: item.text })
                      });
                      if (!res.ok) remaining.push(item);
                    } catch (e) {
                      remaining.push(item);
                    }
                  }
                  writeJsonStorage(STORAGE_OUTBOX_KEY, remaining);
                  updateServiceStatusUI(false);
                }

                async function loadMsgs() {
                  updateServiceStatusUI(false);
                  if (navigator.onLine === false) {
                    renderMessages(readJsonStorage(STORAGE_CACHE_KEY));
                    return;
                  }
                  try {
                    await flushLocalStorageOutbox();
                    const r = await fetch('/api/messages');
                    const list = await r.json();
                    writeJsonStorage(STORAGE_CACHE_KEY, list);
                    list.forEach(m => {
                      if (!seenIds.has(m.id)) {
                        seenIds.add(m.id);
                        if (autoReadToggle.checked && seenIds.size > 1) {
                          speakText(m.sender + ' dedi ki: ' + m.text);
                        }
                      }
                    });
                    renderMessages(list);
                    updateServiceStatusUI(false);
                  } catch(e) {
                    updateServiceStatusUI(true);
                    renderMessages(readJsonStorage(STORAGE_CACHE_KEY));
                  }
                }

                form.addEventListener('submit', async (e) => {
                  e.preventDefault();
                  const txt = textEl.value.trim();
                  if (!txt) return;
                  const senderName = senderEl.value.trim() || 'Yerel Ağ Kullanıcısı';
                  const nowStr = new Date().toLocaleTimeString('tr-TR', { hour:'2-digit', minute:'2-digit' });

                  const queueLocally = () => {
                    const outbox = readJsonStorage(STORAGE_OUTBOX_KEY);
                    outbox.push({ id: 'loc_' + Date.now(), sender: senderName, text: txt, time: nowStr });
                    writeJsonStorage(STORAGE_OUTBOX_KEY, outbox);
                    textEl.value = '';
                    updateServiceStatusUI(navigator.onLine === false);
                    renderMessages(readJsonStorage(STORAGE_CACHE_KEY));
                  };

                  if (navigator.onLine === false) {
                    queueLocally();
                    return;
                  }

                  try {
                    const res = await fetch('/api/messages', {
                      method: 'POST',
                      headers: {'Content-Type':'application/json'},
                      body: JSON.stringify({ sender: senderName, text: txt })
                    });
                    if (!res.ok) {
                      queueLocally();
                    } else {
                      textEl.value = '';
                      loadMsgs();
                    }
                  } catch (err) {
                    queueLocally();
                  }
                });

                window.addEventListener('online', () => { loadMsgs(); });
                window.addEventListener('offline', () => { updateServiceStatusUI(true); renderMessages(readJsonStorage(STORAGE_CACHE_KEY)); });

                const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
                let rec = null;
                let isRecording = false;
                function setRecordingUI(on) {
                  isRecording = on;
                  micBtn.classList.toggle('recording', on);
                  micBtn.setAttribute('aria-pressed', String(on));
                  recBanner.classList.toggle('active', on);
                }
                micBtn.addEventListener('click', () => {
                  if (!SR) { alert('Bu tarayıcı Web Speech API ses tanımayı desteklemiyor.'); return; }
                  if (isRecording && rec) { rec.stop(); return; }
                  rec = new SR();
                  rec.lang = 'tr-TR';
                  rec.interimResults = true;
                  rec.onstart = () => setRecordingUI(true);
                  rec.onresult = (ev) => {
                    let t = '';
                    for (let i = 0; i < ev.results.length; i++) t += ev.results[i][0].transcript;
                    textEl.value = t;
                  };
                  rec.onerror = () => setRecordingUI(false);
                  rec.onend = () => setRecordingUI(false);
                  rec.start();
                });
                stopVoice.addEventListener('click', () => { if (rec) rec.stop(); });

                updateServiceStatusUI(false);
                loadMsgs();
                setInterval(loadMsgs, 1500);
              </script>
            </body>
            </html>
        """.trimIndent()
    }
}
