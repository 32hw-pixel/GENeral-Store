package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.DirectMessage
import com.example.data.FirestoreChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ServiceStatusState(
    val isOnline: Boolean = true,
    val transportLabel: String = "Ağ Hazır",
    val offlineModeNotificationVisible: Boolean = false,
    val offlineNotificationText: String = "",
    val pendingOutboxCount: Int = 0,
    val lastSyncEvent: String? = null,
    val updatedAtTime: String = ""
)

data class QueuedOfflineMessage(
    val id: String,
    val conversationId: String,
    val participantIds: List<String>,
    val senderDisplayName: String,
    val message: DirectMessage,
    val queuedAtMs: Long
)

/**
 * KVKK-compliant real-time Connectivity Monitor + Offline-First Local Storage Queue.
 * - Uses Android ConnectivityManager.NetworkCallback to detect online/offline transitions live.
 * - Automatically persists conversations and outgoing messages locally (MODE_PRIVATE) when offline.
 * - Automatically flushes pending outgoing messages to Firebase Firestore as soon as internet returns.
 */
object NetworkConnectivityAndOfflineManager {
    private const val TAG = "NetworkStatusMgr"
    private const val PREFS_OUTBOX = "general_chat_offline_outbox_prefs"
    private const val PREFS_CACHE = "general_chat_offline_conv_cache_prefs"
    private const val KEY_OUTBOX_JSON = "offline_outbox_v1"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var wasPreviouslyOnline: Boolean? = null

    private val _serviceStatus = MutableStateFlow(ServiceStatusState())
    val serviceStatus: StateFlow<ServiceStatusState> = _serviceStatus.asStateFlow()

    @Synchronized
    fun init(context: Context) {
        val ctx = context.applicationContext ?: context
        appContext = ctx
        val queued = loadOutbox(ctx)
        val initialOnline = evaluateCurrentOnlineState(ctx)
        val initialTransport = evaluateTransportLabel(ctx)
        val timeStr = nowClock()

        wasPreviouslyOnline = initialOnline
        _serviceStatus.value = ServiceStatusState(
            isOnline = initialOnline,
            transportLabel = initialTransport,
            offlineModeNotificationVisible = !initialOnline,
            offlineNotificationText = if (!initialOnline) {
                "Çevrimdışı-öncelikli moda geçildi. Mesajlarınız cihaz yerel deposunda (Local Storage) güvende; aynı Wi-Fi/Hotspot içindeyseniz Ücretsiz Yerel Sunucuyu kullanabilirsiniz."
            } else {
                ""
            },
            pendingOutboxCount = queued.size,
            updatedAtTime = timeStr
        )

        registerConnectivityCallback(ctx)

        if (initialOnline && queued.isNotEmpty()) {
            scope.launch { flushOfflineOutbox(ctx) }
        }
    }

    fun dismissOfflineNotification() {
        _serviceStatus.value = _serviceStatus.value.copy(offlineModeNotificationVisible = false)
    }

    /**
     * Deterministic state update helper (also used by unit tests to verify online/offline transitions).
     */
    fun onConnectivityChanged(isOnline: Boolean, transportLabel: String, context: Context? = appContext) {
        val prev = wasPreviouslyOnline
        wasPreviouslyOnline = isOnline
        val ctx = context ?: appContext
        val pendingCount = if (ctx != null) loadOutbox(ctx).size else _serviceStatus.value.pendingOutboxCount

        val switchedOffline = !isOnline && (prev == null || prev == true)
        val switchedBackOnline = isOnline && prev == false

        _serviceStatus.value = _serviceStatus.value.copy(
            isOnline = isOnline,
            transportLabel = transportLabel,
            offlineModeNotificationVisible = if (switchedOffline) true else if (isOnline) false else _serviceStatus.value.offlineModeNotificationVisible,
            offlineNotificationText = if (!isOnline) {
                "İnternet kesildi: Çevrimdışı-öncelikli mod aktif. Yazdığınız mesajlar Yerel Depolamada (LocalStorage) kuyruklanır ve internet geldiğinde otomatik iletilir."
            } else {
                ""
            },
            pendingOutboxCount = pendingCount,
            lastSyncEvent = if (switchedBackOnline) "İnternet bağlantısı geri geldi — Bekleyen mesajlar eşitleniyor..." else _serviceStatus.value.lastSyncEvent,
            updatedAtTime = nowClock()
        )

        if (isOnline && ctx != null && pendingCount > 0) {
            scope.launch { flushOfflineOutbox(ctx) }
        }
    }

    fun cacheConversationMessages(context: Context, conversationId: String, messages: List<DirectMessage>) {
        if (conversationId.isBlank() || conversationId == SelfChatStorage.SELF_NOTES_CONVERSATION_ID) return
        val arr = JSONArray()
        messages.takeLast(120).forEach { arr.put(it.toJson()) }
        context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
            .edit()
            .putString("conv_$conversationId", arr.toString())
            .apply()
    }

    fun loadCachedConversationMessages(context: Context, conversationId: String): List<DirectMessage> {
        if (conversationId.isBlank() || conversationId == SelfChatStorage.SELF_NOTES_CONVERSATION_ID) return emptyList()
        val raw = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE)
            .getString("conv_$conversationId", null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { idx ->
                arr.optJSONObject(idx)?.toDirectMessage(conversationId)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun enqueueOfflineMessage(
        context: Context,
        conversationId: String,
        message: DirectMessage,
        participantIds: List<String>,
        senderDisplayName: String
    ): QueuedOfflineMessage {
        val pendingMsg = message.copy(
            deliveredAtMs = null,
            readAtMs = null
        )
        val item = QueuedOfflineMessage(
            id = pendingMsg.id,
            conversationId = conversationId,
            participantIds = participantIds,
            senderDisplayName = senderDisplayName,
            message = pendingMsg,
            queuedAtMs = System.currentTimeMillis()
        )
        val list = loadOutbox(context).toMutableList()
        if (list.none { it.id == item.id }) {
            list.add(item)
            saveOutbox(context, list)
        }
        // Also merge into cached conversation list so message persists visibly even after restart
        val cached = loadCachedConversationMessages(context, conversationId).toMutableList()
        if (cached.none { it.id == pendingMsg.id }) {
            cached.add(pendingMsg)
            cacheConversationMessages(context, conversationId, cached)
        }
        _serviceStatus.value = _serviceStatus.value.copy(
            pendingOutboxCount = list.size,
            offlineModeNotificationVisible = true,
            offlineNotificationText = "Mesaj çevrimdışı yerel kuyruğa kaydedildi (${list.size} bekleyen). İnternet bağlanınca otomatik gönderilecek.",
            updatedAtTime = nowClock()
        )
        return item
    }

    fun getPendingMessagesForConversation(context: Context, conversationId: String): List<DirectMessage> {
        return loadOutbox(context)
            .filter { it.conversationId == conversationId }
            .map { it.message }
    }

    suspend fun flushOfflineOutbox(context: Context): Int {
        val currentUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        } ?: return 0

        val queue = loadOutbox(context)
        if (queue.isEmpty()) {
            _serviceStatus.value = _serviceStatus.value.copy(pendingOutboxCount = 0)
            return 0
        }

        val repo = try {
            FirestoreChatRepository.create(context)
        } catch (e: Exception) {
            Log.w(TAG, "Cannot create repo for flush: ${e.message}")
            return 0
        }

        var deliveredCount = 0
        val remaining = mutableListOf<QueuedOfflineMessage>()
        queue.forEach { queued ->
            val pIds = if (queued.participantIds.contains(currentUser.uid)) {
                queued.participantIds
            } else {
                (listOf(currentUser.uid) + queued.participantIds).distinct()
            }
            val res = repo.sendMessage(
                conversationId = queued.conversationId,
                message = queued.message.copy(isMe = true),
                participantIds = pIds,
                senderDisplayName = queued.senderDisplayName.ifBlank { currentUser.displayName ?: "Kullanıcı" }
            )
            if (res.isSuccess) {
                deliveredCount++
            } else {
                remaining.add(queued)
            }
        }
        saveOutbox(context, remaining)
        _serviceStatus.value = _serviceStatus.value.copy(
            pendingOutboxCount = remaining.size,
            lastSyncEvent = if (deliveredCount > 0) "$deliveredCount çevrimdışı mesaj buluta iletildi ✓" else _serviceStatus.value.lastSyncEvent,
            updatedAtTime = nowClock()
        )
        return deliveredCount
    }

    /**
     * KVKK Article 11 local erasure: deletes all cached offline conversations & queued outbox entries.
     */
    fun clearAll(context: Context) {
        context.getSharedPreferences(PREFS_OUTBOX, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE).edit().clear().apply()
        _serviceStatus.value = _serviceStatus.value.copy(
            pendingOutboxCount = 0,
            lastSyncEvent = null
        )
    }

    private fun registerConnectivityCallback(context: Context) {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            networkCallback?.let {
                try {
                    cm.unregisterNetworkCallback(it)
                } catch (_: Exception) {
                }
            }
            val cb = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val online = evaluateCurrentOnlineState(context)
                    onConnectivityChanged(online, evaluateTransportLabel(context), context)
                }

                override fun onLost(network: Network) {
                    val online = evaluateCurrentOnlineState(context)
                    onConnectivityChanged(online, evaluateTransportLabel(context), context)
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    onConnectivityChanged(hasInternet, transportFromCaps(networkCapabilities), context)
                }
            }
            networkCallback = cb
            cm.registerDefaultNetworkCallback(cb)
        } catch (e: Exception) {
            Log.w(TAG, "Connectivity callback registration skipped: ${e.message}")
        }
    }

    private fun evaluateCurrentOnlineState(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } catch (_: Exception) {
            false
        }
    }

    private fun evaluateTransportLabel(context: Context): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "Çevrimdışı"
            val active = cm.activeNetwork ?: return "Çevrimdışı"
            val caps = cm.getNetworkCapabilities(active) ?: return "Çevrimdışı"
            transportFromCaps(caps)
        } catch (_: Exception) {
            "Çevrimdışı"
        }
    }

    private fun transportFromCaps(caps: NetworkCapabilities): String {
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobil Veri"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> "İnternet"
            else -> "Çevrimdışı"
        }
    }

    private fun saveOutbox(context: Context, list: List<QueuedOfflineMessage>) {
        val arr = JSONArray()
        list.take(200).forEach { q ->
            arr.put(JSONObject().apply {
                put("id", q.id)
                put("conversationId", q.conversationId)
                put("participantIds", JSONArray(q.participantIds))
                put("senderDisplayName", q.senderDisplayName)
                put("queuedAtMs", q.queuedAtMs)
                put("message", q.message.toJson())
            })
        }
        context.getSharedPreferences(PREFS_OUTBOX, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_OUTBOX_JSON, arr.toString())
            .apply()
    }

    private fun loadOutbox(context: Context): List<QueuedOfflineMessage> {
        val raw = context.getSharedPreferences(PREFS_OUTBOX, Context.MODE_PRIVATE)
            .getString(KEY_OUTBOX_JSON, null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val convId = obj.optString("conversationId", "")
                    val msgObj = obj.optJSONObject("message") ?: continue
                    val msg = msgObj.toDirectMessage(convId) ?: continue
                    val pArr = obj.optJSONArray("participantIds")
                    val pIds = buildList {
                        if (pArr != null) {
                            for (j in 0 until pArr.length()) {
                                add(pArr.optString(j))
                            }
                        }
                    }.filter { it.isNotBlank() }
                    add(
                        QueuedOfflineMessage(
                            id = obj.optString("id", msg.id),
                            conversationId = convId,
                            participantIds = pIds,
                            senderDisplayName = obj.optString("senderDisplayName", "Kullanıcı"),
                            message = msg,
                            queuedAtMs = obj.optLong("queuedAtMs", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun DirectMessage.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("conversationId", conversationId)
        put("senderName", senderName)
        put("isMe", isMe)
        put("text", text)
        put("time", time)
        put("timestamp", timestamp)
        put("senderId", senderId ?: "")
        put("deliveredAtMs", deliveredAtMs ?: -1L)
        put("readAtMs", readAtMs ?: -1L)
        put("isVoiceNote", isVoiceNote)
        put("voiceDurationSeconds", voiceDurationSeconds)
        put("attachmentUri", attachmentUri ?: "")
        put("attachmentUrl", attachmentUrl ?: "")
        put("attachmentName", attachmentName ?: "")
        put("attachmentType", attachmentType ?: "")
        put("attachmentSizeBytes", attachmentSizeBytes)
        put("attachmentMimeType", attachmentMimeType ?: "")
        put("transferMode", transferMode ?: "")
        put("replyToMessageId", replyToMessageId ?: "")
        put("replyToText", replyToText ?: "")
        put("replyToSender", replyToSender ?: "")
    }

    private fun JSONObject.toDirectMessage(convIdFallback: String): DirectMessage? {
        val id = optString("id", "")
        if (id.isBlank()) return null
        return DirectMessage(
            id = id,
            conversationId = optString("conversationId", convIdFallback).ifBlank { convIdFallback },
            senderName = optString("senderName", "Kullanıcı"),
            isMe = optBoolean("isMe", true),
            text = optString("text", ""),
            time = optString("time", ""),
            timestamp = optLong("timestamp", System.currentTimeMillis()),
            senderId = optString("senderId").takeIf { it.isNotBlank() },
            deliveredAtMs = optLong("deliveredAtMs", -1L).takeIf { it > 0L },
            readAtMs = optLong("readAtMs", -1L).takeIf { it > 0L },
            isVoiceNote = optBoolean("isVoiceNote", false),
            voiceDurationSeconds = optInt("voiceDurationSeconds", 0),
            attachmentUri = optString("attachmentUri").takeIf { it.isNotBlank() },
            attachmentUrl = optString("attachmentUrl").takeIf { it.isNotBlank() },
            attachmentName = optString("attachmentName").takeIf { it.isNotBlank() },
            attachmentType = optString("attachmentType").takeIf { it.isNotBlank() },
            attachmentSizeBytes = optLong("attachmentSizeBytes", 0L),
            attachmentMimeType = optString("attachmentMimeType").takeIf { it.isNotBlank() },
            transferMode = optString("transferMode").takeIf { it.isNotBlank() },
            replyToMessageId = optString("replyToMessageId").takeIf { it.isNotBlank() },
            replyToText = optString("replyToText").takeIf { it.isNotBlank() },
            replyToSender = optString("replyToSender").takeIf { it.isNotBlank() }
        )
    }

    private fun nowClock(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
