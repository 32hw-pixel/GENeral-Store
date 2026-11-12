package com.example.data

import androidx.compose.runtime.mutableStateListOf

object CommunitiesRepository {
    val communities = mutableStateListOf<CommunityItem>().apply {
        addAll(defaultCommunities)
    }

    fun addCommunity(item: CommunityItem) {
        communities.add(0, item)
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
    val category: String, // "Siber Güvenlik", "Yazılım", "Oyun", "Yapay Zekâ", "Genel"
    val memberCount: Int,
    val isVerified: Boolean = true,
    var isJoined: Boolean = false,
    val createdByMe: Boolean = false,
    val rules: String = "1. Saygılı ve yapıcı olun\n2. Gizlilik ve E2EE ilkelerine uyun\n3. Reklam ve spam yasaktır",
    val channelTags: List<String> = listOf("genel", "duyurular", "teknoloji"),
    val messages: MutableList<DirectMessage> = mutableListOf()
)

val defaultCommunities = listOf(
    CommunityItem(
        id = "comm_cyber_sec",
        name = "🇹🇷 Türkiye Siber Güvenlik Topluluğu",
        description = "Beyaz şapkalı hackerlar, ağ güvenliği, CTF yarışmaları ve E2EE protokolleri.",
        emoji = "🛡️",
        category = "Siber Güvenlik",
        memberCount = 2840,
        isVerified = true,
        isJoined = true,
        channelTags = listOf("genel", "ctf-duyuru", "güvenlik-açıkları"),
        messages = mutableListOf(
            DirectMessage("cm1", "comm_cyber_sec", "Yönetici @general_ytb", false, "Topluluğa hoş geldiniz! Bu frekansta tüm mesajlar SHA-256 doğrulamalıdır.", "Dün"),
            DirectMessage("cm2", "comm_cyber_sec", "Ahmet_Sec", false, "Yeni Android 120Hz ProMotion güncellemesinde dokunma gecikmesi mükemmel olmuş.", "10:30")
        )
    ),
    CommunityItem(
        id = "comm_kotlin_devs",
        name = "⚡ Android & Jetpack Compose Devs",
        description = "Modern Kotlin, Coroutines, Room SQLite ve yüksek performanslı mobil mimari.",
        emoji = "⚡",
        category = "Yazılım",
        memberCount = 1920,
        isVerified = true,
        isJoined = false,
        channelTags = listOf("compose-ui", "ksp-room", "mimari")
    ),
    CommunityItem(
        id = "comm_gemini_ai",
        name = "🤖 Gemini 3.8 Flash AI Prompt Mühendisleri",
        description = "Derin zekâ, kod analizi, multimodal akıl yürütme ve yapay zekâ entegrasyonu.",
        emoji = "🤖",
        category = "Yapay Zekâ",
        memberCount = 3150,
        isVerified = true,
        isJoined = false,
        channelTags = listOf("prompts", "kod-yardım", "flash-model")
    ),
    CommunityItem(
        id = "comm_cyberpunk_gaming",
        name = "🎮 Cyberpunk & Retro Gaming Kulübü",
        description = "Synthwave estetiği, bağımsız oyunlar, retro konsollar ve canlı sohbet.",
        emoji = "🎮",
        category = "Oyun",
        memberCount = 1430,
        isVerified = false,
        isJoined = false,
        channelTags = listOf("sohbet", "oyun-önerileri", "turnuvalar")
    ),
    CommunityItem(
        id = "comm_crypto_p2p",
        name = "🌐 Anonim E2EE & P2P Tünel Ağı",
        description = "Uçtan uca şifreleme, sunucusuz direkt iletişim ve veri egemenliği.",
        emoji = "🌐",
        category = "Gizlilik",
        memberCount = 890,
        isVerified = true,
        isJoined = false,
        channelTags = listOf("p2p-node", "kriptografi", "mesh-net")
    )
)
