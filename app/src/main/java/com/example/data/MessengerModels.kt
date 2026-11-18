package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatClockTime(epochMs: Long = System.currentTimeMillis()): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
}

fun isSameCalendarDay(epochA: Long, epochB: Long): Boolean {
    if (epochA <= 0L || epochB <= 0L) return false
    val calA = Calendar.getInstance().apply { timeInMillis = epochA }
    val calB = Calendar.getInstance().apply { timeInMillis = epochB }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}

fun formatChatDayHeader(epochMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val safeTs = if (epochMs > 0L) epochMs else nowMs
    val yesterdayCal = Calendar.getInstance().apply {
        timeInMillis = nowMs
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return when {
        isSameCalendarDay(safeTs, nowMs) -> "Bugün"
        isSameCalendarDay(safeTs, yesterdayCal.timeInMillis) -> "Dün"
        else -> SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(Date(safeTs))
    }
}

fun formatConversationListTime(epochMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val safeTs = if (epochMs > 0L) epochMs else nowMs
    val clock = formatClockTime(safeTs)
    val yesterdayCal = Calendar.getInstance().apply {
        timeInMillis = nowMs
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return when {
        isSameCalendarDay(safeTs, nowMs) -> clock
        isSameCalendarDay(safeTs, yesterdayCal.timeInMillis) -> "Dün · $clock"
        else -> "${SimpleDateFormat("d MMM", Locale("tr")).format(Date(safeTs))} · $clock"
    }
}

fun formatByteSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when {
        bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb)
        bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes / mb)
        bytes >= kb -> String.format(Locale.US, "%.1f KB", bytes / kb)
        else -> "$bytes B"
    }
}

enum class CallType {
    VOICE_INCOMING,
    VOICE_OUTGOING,
    VOICE_MISSED,
    VIDEO_INCOMING,
    VIDEO_OUTGOING
}

data class DirectMessage(
    val id: String,
    val conversationId: String,
    val senderName: String,
    val isMe: Boolean,
    val text: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val senderId: String? = null,
    val isEncrypted: Boolean = false,
    val deliveredAtMs: Long? = null,
    val readBy: List<String> = emptyList(),
    val readAtMs: Long? = null,
    val isVoiceNote: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val attachmentUri: String? = null,
    val attachmentUrl: String? = attachmentUri,
    val attachmentName: String? = null,
    val attachmentType: String? = null,
    val attachmentSizeBytes: Long = 0L,
    val attachmentMimeType: String? = null,
    val transferMode: String? = null,
    val chunkCount: Int = 0,
    val isEdited: Boolean = false,
    val isDeletedForEveryone: Boolean = false,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val selfDestructSeconds: Int? = null,
    val transcribedText: String? = null,
    val isTranscribing: Boolean = false
)

data class ConversationItem(
    val id: String,
    val name: String,
    val initials: String,
    val username: String? = null,
    val avatarEmoji: String? = null,
    val logoUri: String? = null,
    val isGroup: Boolean = false,
    val isOnline: Boolean = false,
    val lastMessage: String,
    val lastTime: String,
    val unreadCount: Int = 0,
    val isVerified: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,
    val advancedPrivacyEnabled: Boolean = false,
    var customTheme: String = "CYBER_YELLOW",
    val messages: List<DirectMessage> = emptyList(),
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap()
)

data class CallHistoryItem(
    val id: String,
    val contactName: String,
    val initials: String,
    val type: CallType,
    val timeString: String,
    val duration: String
)

val defaultConversations: List<ConversationItem> by lazy {
    val nowMs = System.currentTimeMillis()
    val nowTime = formatClockTime(nowMs)
    listOf(
        ConversationItem(
            id = "conv_self_notes",
            name = "Siz",
            initials = "SZ",
            avatarEmoji = "📝",
            isOnline = true,
            lastMessage = "Kişisel notlar · Yalnızca sizin cihazınızda kalır",
            lastTime = nowTime,
            unreadCount = 0,
            isVerified = true,
            customTheme = "CYBER_YELLOW",
            messages = emptyList()
        ),
        ConversationItem(
            id = "conv_gemini_ai",
            name = "GEN AI",
            initials = "AI",
            avatarEmoji = "🤖",
            isOnline = true,
            lastMessage = "Merhaba! Ben GEN AI. Kodlama, matematik, sohbet arama ve tüm sorularında hazırım.",
            lastTime = nowTime,
            unreadCount = 0,
            isVerified = true,
            customTheme = "CYBER_YELLOW",
            messages = listOf(
                DirectMessage(
                    id = "m_ai_welcome",
                    conversationId = "conv_gemini_ai",
                    senderName = "GEN AI",
                    isMe = false,
                    text = "Merhaba! Ben GEN AI yapay zekâ asistanınım. Kod yazma, matematik, ders, çeviri veya sohbet geçmişinde mesaj bulma konusunda anında yardımcı olabilirim.",
                    time = nowTime,
                    timestamp = nowMs
                )
            )
        )
    )
}

val defaultCallHistory: List<CallHistoryItem> = emptyList()
