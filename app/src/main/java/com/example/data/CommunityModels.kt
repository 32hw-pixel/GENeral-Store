package com.example.data

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object CommunitiesRepository {
    private const val PREFS_NAME = "general_chat_communities"
    private const val KEY_LIST = "communities_json"
    private var appContext: Context? = null

    val communities = mutableStateListOf<CommunityItem>()

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        loadFromPrefs(context.applicationContext)
    }

    fun addCommunity(item: CommunityItem, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        val finalItem = if (ctx != null && !item.logoUri.isNullOrBlank() && item.logoUri.startsWith("content://")) {
            val savedPath = persistLogoToInternalStorage(ctx, item.id, Uri.parse(item.logoUri))
            if (savedPath != null) item.copy(logoUri = savedPath) else item
        } else {
            item
        }
        communities.add(0, finalItem)
        ctx?.let { saveToPrefs(it) }
    }

    fun updateMembership(communityId: String, isJoined: Boolean) {
        val idx = communities.indexOfFirst { it.id == communityId }
        if (idx != -1) {
            val current = communities[idx]
            communities[idx] = current.copy(
                isJoined = isJoined,
                memberCount = if (isJoined) current.memberCount + 1 else (current.memberCount - 1).coerceAtLeast(1)
            )
            appContext?.let { saveToPrefs(it) }
        }
    }

    private fun persistLogoToInternalStorage(context: Context, communityId: String, sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "community_logos").apply { mkdirs() }
            val destFile = File(dir, "${communityId}.img")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (destFile.exists() && destFile.length() > 0L) Uri.fromFile(destFile).toString() else null
        } catch (_: Exception) {
            null
        }
    }

    private fun loadFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_LIST, null) ?: return
            val arr = JSONArray(raw)
            val loaded = mutableListOf<CommunityItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val tagsJson = obj.optJSONArray("channelTags")
                val tags = mutableListOf<String>()
                if (tagsJson != null) {
                    for (t in 0 until tagsJson.length()) tags.add(tagsJson.getString(t))
                }
                loaded.add(
                    CommunityItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        description = obj.optString("description", ""),
                        emoji = obj.optString("emoji", "⚡"),
                        logoUri = obj.optString("logoUri", "").takeIf { it.isNotBlank() },
                        category = obj.optString("category", "Genel"),
                        memberCount = obj.optInt("memberCount", 1),
                        isVerified = obj.optBoolean("isVerified", false),
                        isJoined = obj.optBoolean("isJoined", true),
                        createdByMe = obj.optBoolean("createdByMe", true),
                        channelTags = if (tags.isNotEmpty()) tags else listOf("genel", "sohbet")
                    )
                )
            }
            communities.clear()
            communities.addAll(loaded)
        } catch (_: Exception) {
        }
    }

    private fun saveToPrefs(context: Context) {
        try {
            val arr = JSONArray()
            communities.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("description", item.description)
                    put("emoji", item.emoji)
                    put("logoUri", item.logoUri ?: "")
                    put("category", item.category)
                    put("memberCount", item.memberCount)
                    put("isVerified", item.isVerified)
                    put("isJoined", item.isJoined)
                    put("createdByMe", item.createdByMe)
                    put("channelTags", JSONArray(item.channelTags))
                }
                arr.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LIST, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }
}

data class ScheduledMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val conversationId: String,
    val targetRecipientName: String,
    val text: String,
    val scheduledEpochMillis: Long,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val isDelivered: Boolean = false
)

data class CommunityItem(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String = "⚡",
    val logoUri: String? = null,
    val category: String,
    val memberCount: Int,
    val isVerified: Boolean = true,
    var isJoined: Boolean = false,
    val createdByMe: Boolean = false,
    val rules: String = "1. Saygılı ve yapıcı olun\n2. Gizlilik ve topluluk güvenliği ilkelerine uyun\n3. Reklam ve spam yasaktır",
    val channelTags: List<String> = listOf("genel", "duyurular", "teknoloji"),
    val messages: MutableList<DirectMessage> = mutableListOf()
)

val defaultCommunities: List<CommunityItem> = emptyList()
