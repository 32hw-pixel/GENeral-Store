package com.example.util

import com.example.data.ConversationItem
import com.example.data.DirectMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatContextAnalyzerTest {

    @Test
    fun `empty history outputs zero sent rule so GEN AI states user has not messaged anyone`() {
        val snapshot = ChatContextAnalyzer.buildSnapshot(
            conversations = emptyList(),
            roomMessagesMap = emptyMap(),
            userQuery = "En son kime yazdım?",
            isExcludedProvider = { false }
        )

        assertEquals(0, snapshot.totalSentByMeCount)
        assertNull(snapshot.lastSentSummary)
        assertTrue(snapshot.structuredContextBlock.contains("ZERO_SENT_MESSAGES"))
        assertTrue(
            snapshot.structuredContextBlock.contains("Uygulamayı kullandığından beri şu ana kadar kimseyle konuşmadın")
        )
    }

    @Test
    fun `last sent query identifies exact recipient, message content, and HHmm timestamp`() {
        val room = ConversationItem(
            id = "dm_alice",
            name = "Alice",
            initials = "AL",
            lastMessage = "Yarın saat 14:30 buluşalım",
            lastTime = "14:30"
        )
        val msgIncoming = DirectMessage(
            id = "m1",
            conversationId = "dm_alice",
            senderName = "Alice",
            isMe = false,
            text = "Nasılsın?",
            time = "14:25",
            timestamp = 1_700_000_100_000L
        )
        val msgSent = DirectMessage(
            id = "m2",
            conversationId = "dm_alice",
            senderName = "Ben",
            isMe = true,
            text = "Yarın saat 14:30 buluşalım",
            time = "14:30",
            timestamp = 1_700_000_200_000L
        )

        val snapshot = ChatContextAnalyzer.buildSnapshot(
            conversations = listOf(room),
            roomMessagesMap = mapOf("dm_alice" to listOf(msgIncoming, msgSent)),
            userQuery = "En son kime yazdım?",
            isExcludedProvider = { false }
        )

        assertEquals(1, snapshot.totalSentByMeCount)
        assertNotNull(snapshot.lastSentSummary)
        assertEquals("Alice", snapshot.lastSentSummary!!.recipientName)
        assertEquals("Yarın saat 14:30 buluşalım", snapshot.lastSentSummary!!.messageText)
        assertEquals("14:30", snapshot.lastSentSummary!!.formattedTime)
        assertTrue(snapshot.structuredContextBlock.contains("EN SON YAZILAN KİŞİ/SOHBET: Alice"))
        assertTrue(snapshot.structuredContextBlock.contains("Saat: 14:30"))
    }

    @Test
    fun `message search locates matching keyword across allowed conversations`() {
        val room = ConversationItem(
            id = "grp_dev",
            name = "Geliştirici Grubu",
            initials = "GG",
            isGroup = true,
            lastMessage = "Tamam",
            lastTime = "16:10"
        )
        val invoiceMsg = DirectMessage(
            id = "m_inv",
            conversationId = "grp_dev",
            senderName = "Mehmet",
            isMe = false,
            text = "Sunucu şifresi ve port bilgisi 8080 olarak ayarlandı",
            time = "11:15",
            timestamp = 1_700_000_000_000L
        )

        val snapshot = ChatContextAnalyzer.buildSnapshot(
            conversations = listOf(room),
            roomMessagesMap = mapOf("grp_dev" to listOf(invoiceMsg)),
            userQuery = "Bir mesajı arıyorum içinde port 8080 geçiyordu",
            isExcludedProvider = { false }
        )

        assertTrue(snapshot.searchHits.isNotEmpty())
        assertTrue(snapshot.searchHits.first().contains("8080"))
        assertTrue(snapshot.structuredContextBlock.contains("Geliştirici Grubu"))
    }

    @Test
    fun `advanced privacy rooms are strictly excluded from GEN AI context`() {
        val secretRoom = ConversationItem(
            id = "dm_secret",
            name = "Gizli Kişi",
            initials = "GK",
            advancedPrivacyEnabled = true,
            lastMessage = "Çok gizli parola XYZ999",
            lastTime = "18:00"
        )
        val secretMsg = DirectMessage(
            id = "m_sec",
            conversationId = "dm_secret",
            senderName = "Ben",
            isMe = true,
            text = "Çok gizli parola XYZ999",
            time = "18:00",
            timestamp = 1_700_000_500_000L
        )

        val snapshot = ChatContextAnalyzer.buildSnapshot(
            conversations = listOf(secretRoom),
            roomMessagesMap = mapOf("dm_secret" to listOf(secretMsg)),
            userQuery = "XYZ999 mesajı nerede ve en son kime yazdım?",
            isExcludedProvider = { convId -> convId == "dm_secret" }
        )

        assertEquals(0, snapshot.visibleConversationCount)
        assertEquals(1, snapshot.lockedConversationCount)
        assertEquals(0, snapshot.totalSentByMeCount)
        assertNull(snapshot.lastSentSummary)
        assertFalse(snapshot.structuredContextBlock.contains("XYZ999"))
        assertFalse(snapshot.structuredContextBlock.contains("Gizli Kişi"))
    }
}
