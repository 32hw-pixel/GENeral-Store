package com.example.util

import android.content.Context
import com.example.data.ConversationItem
import com.example.data.DirectMessage
import com.example.data.formatClockTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LastSentSummary(
    val conversationId: String,
    val recipientName: String,
    val messageText: String,
    val formattedTime: String,
    val timestampMs: Long
)

data class ChatContextSnapshot(
    val totalSentByMeCount: Int,
    val visibleConversationCount: Int,
    val lockedConversationCount: Int,
    val lastSentSummary: LastSentSummary?,
    val searchHits: List<String>,
    val structuredContextBlock: String
)

/**
 * Aggregates active user conversations (excluding Gelişmiş Sohbet Gizliliği-locked rooms)
 * so GEN AI can answer questions like:
 *  - "En son kime yazdım?"
 *  - "Bir mesajı arıyorum"
 *  - Or accurately state "Uygulamayı kullandığından beri şu ana kadar kimseyle konuşmadın / kimseye mesaj yazmadın."
 */
object ChatContextAnalyzer {

    fun buildSnapshot(
        conversations: List<ConversationItem>,
        roomMessagesMap: Map<String, List<DirectMessage>>,
        userQuery: String,
        isExcludedProvider: (String) -> Boolean
    ): ChatContextSnapshot {
        val eligibleConvs = conversations.filter { conv ->
            conv.id != "conv_gemini_ai" &&
                !conv.advancedPrivacyEnabled &&
                !isExcludedProvider(conv.id)
        }
        val lockedCount = conversations.count { conv ->
            conv.id != "conv_gemini_ai" && (conv.advancedPrivacyEnabled || isExcludedProvider(conv.id))
        }

        val allAllowedMessages = mutableListOf<Pair<ConversationItem, DirectMessage>>()
        eligibleConvs.forEach { conv ->
            val msgs = (roomMessagesMap[conv.id].orEmpty() + conv.messages)
                .distinctBy { it.id }
                .sortedBy { it.timestamp }
            msgs.forEach { msg ->
                allAllowedMessages.add(conv to msg)
            }
        }

        val sentByMeMessages = allAllowedMessages
            .filter { (_, msg) -> msg.isMe && !msg.isDeletedForEveryone }
            .sortedByDescending { (_, msg) -> msg.timestamp }

        val lastSent = sentByMeMessages.firstOrNull()?.let { (conv, msg) ->
            LastSentSummary(
                conversationId = conv.id,
                recipientName = conv.name,
                messageText = msg.text,
                formattedTime = if (msg.time.isNotBlank() && msg.time != "Şimdi") msg.time else formatClockTime(msg.timestamp),
                timestampMs = msg.timestamp
            )
        } ?: eligibleConvs.firstOrNull {
            // Fallback ONLY if a real user message metadata exists on room without hydrated list
            val lm = it.lastMessage.trim()
            lm.isNotBlank() &&
                !lm.startsWith("Uçtan uca şifreli bulut sohbeti açıldı") &&
                !lm.startsWith("Bulut sohbeti açıldı") &&
                !lm.startsWith("Grup sohbeti oluşturuldu") &&
                allAllowedMessages.any { pair -> pair.first.id == it.id && pair.second.isMe }
        }?.let { conv ->
            LastSentSummary(
                conversationId = conv.id,
                recipientName = conv.name,
                messageText = conv.lastMessage,
                formattedTime = conv.lastTime,
                timestampMs = System.currentTimeMillis()
            )
        }

        // Extract meaningful search terms from user's query
        val stopWords = setOf(
            "bir", "mesajı", "mesaji", "mesaj", "arıyorum", "ariyorum", "ara", "bul",
            "nerede", "kim", "kime", "yazdım", "yazdim", "en", "son", "ne", "zaman", "hangi"
        )
        val queryTokens = userQuery.lowercase(Locale.getDefault())
            .split(Regex("[^a-zA-ZçğıöşüÇĞİÖŞÜ0-9]+"))
            .filter { it.length >= 2 && it !in stopWords }

        val hits = if (queryTokens.isNotEmpty()) {
            allAllowedMessages
                .filter { (conv, msg) ->
                    val haystack = "${msg.text} ${msg.senderName} ${conv.name} ${msg.attachmentName.orEmpty()}".lowercase(Locale.getDefault())
                    queryTokens.any { token -> haystack.contains(token) }
                }
                .sortedByDescending { (_, msg) -> msg.timestamp }
                .take(12)
                .map { (conv, msg) ->
                    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                    "- [${conv.name}] (${if (msg.isMe) "Sen" else msg.senderName} · $dateStr): \"${msg.text.take(160)}\""
                }
        } else {
            allAllowedMessages
                .sortedByDescending { (_, msg) -> msg.timestamp }
                .take(15)
                .map { (conv, msg) ->
                    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                    "- [${conv.name}] (${if (msg.isMe) "Sen" else msg.senderName} · $dateStr): \"${msg.text.take(140)}\""
                }
        }

        val sb = StringBuilder()
        sb.appendLine("[GENERAL CHAT CANLI SOHBET BAĞLAMI — GİZLİLİK FİLTRELİ]")
        sb.appendLine("Erişilebilir (kilitsiz) sohbet sayısı: ${eligibleConvs.size}")
        sb.appendLine("Gelişmiş Sohbet Gizliliği nedeniyle AI dışı bırakılan sohbet sayısı: $lockedCount")
        sb.appendLine("Kullanıcının başkalarına gönderdiği toplam mesaj sayısı: ${sentByMeMessages.size}")

        if (sentByMeMessages.isEmpty()) {
            sb.appendLine("DURUM: ZERO_SENT_MESSAGES (Kullanıcı uygulamayı kullandığından beri hiçbir kişiye veya gruba mesaj göndermedi.)")
            sb.appendLine("KRİTİK KURAL: Eğer kullanıcı en son kime yazdığını veya kimlerle konuştuğunu sorarsa mutlaka 'Uygulamayı kullandığından beri şu ana kadar kimseyle konuşmadın ve kimseye mesaj yazmadın.' şeklinde net ve dürüst cevap ver.")
        } else {
            lastSent?.let { last ->
                sb.appendLine("EN SON YAZILAN KİŞİ/SOHBET: ${last.recipientName} | Saat: ${last.formattedTime} | Mesaj: \"${last.messageText.take(160)}\"")
            }
            sb.appendLine("Mevcut sohbet odaları:")
            eligibleConvs.take(10).forEach { conv ->
                sb.appendLine("  • ${conv.name} (${conv.lastTime}): son ileti=\"${conv.lastMessage.take(90)}\"")
            }
        }
        if (hits.isNotEmpty()) {
            sb.appendLine("Eşleşen / Mevcut Mesajlar:")
            hits.forEach { sb.appendLine(it) }
        }
        sb.appendLine("Eğer kullanıcı 'bir mesajı arıyorum' derse: yukarıdaki mesajlarda ara; eşleşme varsa sohbet adı, gönderici, saat ve mesaj içeriğini paylaş; eşleşme yoksa daha spesifik bir kelime, kişi adı veya tarih isteyerek yardımcı ol.")

        return ChatContextSnapshot(
            totalSentByMeCount = sentByMeMessages.size,
            visibleConversationCount = eligibleConvs.size,
            lockedConversationCount = lockedCount,
            lastSentSummary = lastSent,
            searchHits = hits,
            structuredContextBlock = sb.toString()
        )
    }
}

/**
 * Live in-memory snapshot shared between ChatScreen / DirectMessageScreen / Firestore listeners
 * and ChatViewModel so GEN AI always has up-to-date awareness of non-private user conversations.
 */
object ChatContextCache {
    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations = _conversations.asStateFlow()

    private val _roomMessages = MutableStateFlow<Map<String, List<DirectMessage>>>(emptyMap())
    val roomMessages = _roomMessages.asStateFlow()

    fun updateConversations(list: List<ConversationItem>) {
        _conversations.value = list.filter { it.id != "conv_gemini_ai" }
    }

    fun updateRoomMessages(conversationId: String, messages: List<DirectMessage>) {
        if (conversationId == "conv_gemini_ai") return
        val next = _roomMessages.value.toMutableMap()
        next[conversationId] = messages
        _roomMessages.value = next
    }

    fun buildPromptContext(context: Context, userQuery: String): String {
        val snapshot = ChatContextAnalyzer.buildSnapshot(
            conversations = _conversations.value,
            roomMessagesMap = _roomMessages.value,
            userQuery = userQuery,
            isExcludedProvider = { convId ->
                val cfg = PrivacyPreferencesManager.getChatConfig(context, convId)
                cfg.advancedPrivacyEnabled || cfg.excludeFromAi
            }
        )
        return snapshot.structuredContextBlock
    }
}
