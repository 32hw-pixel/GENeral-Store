package com.example.util

import android.content.Context
import com.example.data.DirectMessage
import org.json.JSONArray
import org.json.JSONObject

/**
 * Strictly device-local storage for the automatic "Siz" (personal self-notes) conversation.
 * Messages, attachments, voice notes, and location pins created inside "Siz" never leave
 * this device and are never transmitted to Firebase Firestore, P2P peers, or parental telemetry.
 */
object SelfChatStorage {
    const val SELF_NOTES_CONVERSATION_ID = "conv_self_notes"
    private const val PREFS_NAME = "general_chat_self_notes_prefs"
    private const val KEY_MESSAGES_JSON = "self_messages_json"

    fun loadMessages(context: Context): List<DirectMessage> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MESSAGES_JSON, null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { idx ->
                val obj = arr.optJSONObject(idx) ?: return@mapNotNull null
                DirectMessage(
                    id = obj.optString("id"),
                    conversationId = SELF_NOTES_CONVERSATION_ID,
                    senderName = obj.optString("senderName", "Siz"),
                    isMe = true,
                    text = obj.optString("text"),
                    time = obj.optString("time"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    senderId = null,
                    isEncrypted = true,
                    deliveredAtMs = obj.optLong("timestamp", System.currentTimeMillis()),
                    isVoiceNote = obj.optBoolean("isVoiceNote", false),
                    voiceDurationSeconds = obj.optInt("voiceDurationSeconds", 0),
                    attachmentUri = obj.optString("attachmentUri").takeIf { it.isNotBlank() },
                    attachmentUrl = obj.optString("attachmentUrl").takeIf { it.isNotBlank() },
                    attachmentName = obj.optString("attachmentName").takeIf { it.isNotBlank() },
                    attachmentType = obj.optString("attachmentType").takeIf { it.isNotBlank() },
                    attachmentSizeBytes = obj.optLong("attachmentSizeBytes", 0L),
                    attachmentMimeType = obj.optString("attachmentMimeType").takeIf { it.isNotBlank() },
                    isEdited = obj.optBoolean("isEdited", false),
                    isDeletedForEveryone = obj.optBoolean("isDeletedForEveryone", false),
                    replyToMessageId = obj.optString("replyToMessageId").takeIf { it.isNotBlank() },
                    replyToText = obj.optString("replyToText").takeIf { it.isNotBlank() },
                    replyToSender = obj.optString("replyToSender").takeIf { it.isNotBlank() },
                    isPinned = obj.optBoolean("isPinned", false)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMessages(context: Context, messages: List<DirectMessage>) {
        val arr = JSONArray()
        messages.filter { !it.isDeletedForEveryone }.forEach { msg ->
            arr.put(JSONObject().apply {
                put("id", msg.id)
                put("senderName", msg.senderName)
                put("text", msg.text)
                put("time", msg.time)
                put("timestamp", msg.timestamp)
                put("isVoiceNote", msg.isVoiceNote)
                put("voiceDurationSeconds", msg.voiceDurationSeconds)
                put("attachmentUri", msg.attachmentUri ?: "")
                put("attachmentUrl", msg.attachmentUrl ?: "")
                put("attachmentName", msg.attachmentName ?: "")
                put("attachmentType", msg.attachmentType ?: "")
                put("attachmentSizeBytes", msg.attachmentSizeBytes)
                put("attachmentMimeType", msg.attachmentMimeType ?: "")
                put("isEdited", msg.isEdited)
                put("isDeletedForEveryone", msg.isDeletedForEveryone)
                put("replyToMessageId", msg.replyToMessageId ?: "")
                put("replyToText", msg.replyToText ?: "")
                put("replyToSender", msg.replyToSender ?: "")
                put("isPinned", msg.isPinned)
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MESSAGES_JSON, arr.toString())
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
