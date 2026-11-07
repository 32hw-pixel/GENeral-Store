package com.example.data

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
    val isEncrypted: Boolean = true,
    val isVoiceNote: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val attachmentUri: String? = null,
    val attachmentUrl: String? = attachmentUri,
    val attachmentName: String? = null,
    val attachmentType: String? = null,
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
    val isGroup: Boolean = false,
    val isOnline: Boolean = false,
    val lastMessage: String,
    val lastTime: String,
    val unreadCount: Int = 0,
    val isVerified: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,
    var customTheme: String = "CYBER_YELLOW",
    val messages: List<DirectMessage> = emptyList()
)

data class CallHistoryItem(
    val id: String,
    val contactName: String,
    val initials: String,
    val type: CallType,
    val timeString: String,
    val duration: String
)

val defaultConversations = listOf(
    ConversationItem(
        id = "conv_gen_support",
        name = "GEN Destek Hattı",
        initials = "GD",
        isOnline = true,
        lastMessage = "GENeral Chat güvenli iletişim tüneline hoş geldiniz.",
        lastTime = "Şimdi",
        unreadCount = 1,
        isVerified = true,
        messages = listOf(
            DirectMessage("m1", "conv_gen_support", "GEN Destek", false, "GENeral Chat v7.7.3 Pro merkezine hoş geldiniz.", "12:00"),
            DirectMessage("m2", "conv_gen_support", "GEN Destek", false, "Uçtan uca şifreli (E2EE) tünel aktif. Sağ üstteki '+' ile yeni kişi veya 2. cihaz ekleyip doğrudan mesajlaşabilirsiniz.", "12:01")
        )
    )
)

val defaultCallHistory = listOf(
    CallHistoryItem("c1", "GEN Destek Hattı", "GD", CallType.VOICE_OUTGOING, "Bugün", "01:18")
)

