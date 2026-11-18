package com.example

import com.example.data.AiMode
import com.example.data.PasswordStrength
import com.example.data.SecurityUtils
import com.example.data.defaultCallHistory
import com.example.data.defaultCommunities
import com.example.data.defaultConversations
import com.example.data.formatChatDayHeader
import com.example.data.formatClockTime
import com.example.data.formatConversationListTime
import com.example.data.isSameCalendarDay
import com.example.util.ChatWallpaper
import com.example.util.ChatWallpaperManager
import com.example.util.KeyboardSoundProfile
import com.example.util.LocalMeshServer
import com.example.util.NotificationSoundProfile
import com.example.util.SoundProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class ExampleRobolectricTest {

  @Test
  fun `test password hashing and verification`() {
    val password = "SuperSecret123!"
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPassword(password, salt)

    assertTrue(SecurityUtils.verifyPassword(password, salt, hash))
    assertFalse(SecurityUtils.verifyPassword("WrongPassword", salt, hash))
  }

  @Test
  fun `test email validation`() {
    assertTrue(SecurityUtils.isValidEmail("user@example.com"))
    assertTrue(SecurityUtils.isValidEmail("test.name+tag@sub.domain.org"))
    assertFalse(SecurityUtils.isValidEmail("invalid-email"))
    assertFalse(SecurityUtils.isValidEmail("@example.com"))
  }

  @Test
  fun `test password strength evaluation`() {
    assertEquals(PasswordStrength.WEAK, SecurityUtils.evaluatePasswordStrength("123"))
    assertEquals(PasswordStrength.MEDIUM, SecurityUtils.evaluatePasswordStrength("Secret1"))
    assertEquals(PasswordStrength.STRONG, SecurityUtils.evaluatePasswordStrength("SuperSecret123!"))
  }

  @Test
  fun `test reset code generation`() {
    val code = SecurityUtils.generateResetCode()
    assertEquals(6, code.length)
    assertTrue(code.all { it.isDigit() })
  }

  @Test
  fun `test no fake pre-populated communities calls or guest rooms`() {
    assertTrue(defaultCommunities.isEmpty())
    assertTrue(defaultCallHistory.isEmpty())
    assertEquals(2, defaultConversations.size)
    assertEquals("conv_self_notes", defaultConversations[0].id)
    assertEquals("Siz", defaultConversations[0].name)
    assertEquals("conv_gemini_ai", defaultConversations[1].id)
    assertEquals("GEN AI", defaultConversations[1].name)
  }

  @Test
  fun `test gen ai identity and code generation prompts`() {
    assertTrue(AiMode.GENERAL.systemPrompt.contains("GEN AI"))
    assertTrue(AiMode.CODE.systemPrompt.contains("GEN AI"))
    assertTrue(AiMode.CODE.systemPrompt.contains("Kotlin"))
    SoundProfile.entries.forEach { profile ->
      assertFalse(profile.displayName.contains("Asphalt", ignoreCase = true))
      assertFalse(profile.displayName.contains("Nitro", ignoreCase = true))
    }
    KeyboardSoundProfile.entries.forEach { profile ->
      assertFalse(profile.displayName.contains("Asphalt", ignoreCase = true))
      assertFalse(profile.displayName.contains("Nitro", ignoreCase = true))
    }
    NotificationSoundProfile.entries.forEach { profile ->
      assertFalse(profile.displayName.contains("Asphalt", ignoreCase = true))
      assertFalse(profile.displayName.contains("Nitro", ignoreCase = true))
    }
  }

  @Test
  fun `test wallpaper manager updates state cleanly`() {
    assertEquals(ChatWallpaper.CYBER_GRID, ChatWallpaperManager.selectedWallpaper.value)
    ChatWallpaperManager.setWallpaper(ChatWallpaper.NEON_PURPLE)
    assertEquals(ChatWallpaper.NEON_PURPLE, ChatWallpaperManager.selectedWallpaper.value)
    ChatWallpaperManager.setWallpaper(ChatWallpaper.CYBER_HEX_PATTERN)
    assertEquals(ChatWallpaper.CYBER_HEX_PATTERN, ChatWallpaperManager.selectedWallpaper.value)
    ChatWallpaperManager.setWallpaper(ChatWallpaper.CYBER_GRID)
  }

  @Test
  fun `test free local mesh server serves status and accepts messages`() {
    LocalMeshServer.clearMessages()
    LocalMeshServer.start(preferredPort = 18765)
    try {
      var waitedMs = 0
      while (!LocalMeshServer.isRunning.value && waitedMs < 2000) {
        Thread.sleep(50)
        waitedMs += 50
      }
      assertTrue(LocalMeshServer.isRunning.value)
      val port = LocalMeshServer.serverPort.value

      val statusConn = (URL("http://127.0.0.1:$port/api/status").openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 2000
        readTimeout = 2000
      }
      assertEquals(200, statusConn.responseCode)
      val statusBody = statusConn.inputStream.bufferedReader().readText()
      assertTrue(statusBody.contains("FREE_0_TRY"))

      val postConn = (URL("http://127.0.0.1:$port/api/messages").openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
      }
      postConn.outputStream.use {
        it.write("""{"sender":"TestUser","text":"Merhaba ücretsiz sunucu!"}""".toByteArray())
      }
      assertEquals(201, postConn.responseCode)

      val listConn = (URL("http://127.0.0.1:$port/api/messages").openConnection() as HttpURLConnection)
      assertEquals(200, listConn.responseCode)
      val listBody = listConn.inputStream.bufferedReader().readText()
      assertTrue(listBody.contains("TestUser"))
      assertTrue(listBody.contains("Merhaba ücretsiz sunucu!"))
    } finally {
      LocalMeshServer.stop()
      LocalMeshServer.clearMessages()
    }
  }

  @Test
  fun `test accurate day headers, calendar comparisons, and conversation list dates`() {
    val nowCal = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 17, 15, 45, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val nowMs = nowCal.timeInMillis

    val todayMorningMs = java.util.Calendar.getInstance().apply {
      timeInMillis = nowMs
      set(java.util.Calendar.HOUR_OF_DAY, 8)
      set(java.util.Calendar.MINUTE, 15)
    }.timeInMillis

    val yesterdayMs = java.util.Calendar.getInstance().apply {
      timeInMillis = nowMs
      add(java.util.Calendar.DAY_OF_YEAR, -1)
      set(java.util.Calendar.HOUR_OF_DAY, 22)
      set(java.util.Calendar.MINUTE, 5)
    }.timeInMillis

    val olderMs = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.OCTOBER, 5, 9, 30, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    assertTrue(isSameCalendarDay(todayMorningMs, nowMs))
    assertFalse(isSameCalendarDay(yesterdayMs, nowMs))

    assertEquals("Bugün", formatChatDayHeader(todayMorningMs, nowMs))
    assertEquals("Dün", formatChatDayHeader(yesterdayMs, nowMs))
    assertTrue(formatChatDayHeader(olderMs, nowMs).contains("2026"))

    assertEquals(formatClockTime(todayMorningMs), formatConversationListTime(todayMorningMs, nowMs))
    assertTrue(formatConversationListTime(yesterdayMs, nowMs).startsWith("Dün · "))
    assertTrue(formatConversationListTime(olderMs, nowMs).contains(" · "))
  }
}
