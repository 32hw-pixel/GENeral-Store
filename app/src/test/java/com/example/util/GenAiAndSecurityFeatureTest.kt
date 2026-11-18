package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiMode
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.HttpURLConnection
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GenAiAndSecurityFeatureTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `offline ai engine handles identity, dictionary, math, code, and chat context`() {
        // Identity sanitization never leaks Gemini
        val sanitized = OfflineAiEngine.sanitizeIdentity("Merhaba, ben Google'ın geliştirdiği Gemini asistanıyım.")
        assertFalse(sanitized.contains("Gemini", ignoreCase = true))
        assertTrue(sanitized.contains("GEN AI"))

        val whoAmI = OfflineAiEngine.generateResponse("Sen kimsin?", AiMode.GENERAL)
        assertTrue(whoAmI.contains("GEN AI"))
        assertFalse(whoAmI.contains("Gemini", ignoreCase = true))

        // Multilingual dictionary lookup
        val dict = OfflineAiEngine.lookupDictionary("algoritma almanca ne demek")
        assertNotNull(dict)
        assertTrue(dict!!.contains("algorithmus"))
        assertTrue(dict.contains("algorithm"))

        val greetingDict = OfflineAiEngine.lookupDictionary("merhaba")
        assertNotNull(greetingDict)
        assertTrue(greetingDict!!.contains("hello"))
        assertTrue(greetingDict.contains("bonjour"))

        // Deterministic math
        val math = OfflineAiEngine.generateResponse("125 * 8", AiMode.GENERAL)
        assertTrue(math.contains("1000"))

        // Code generation
        val kotlinCode = OfflineAiEngine.generateResponse("Kotlin örneği yaz", AiMode.CODE)
        assertTrue(kotlinCode.contains("```kotlin"))

        val pythonCode = OfflineAiEngine.generateResponse("Python kodu yaz", AiMode.CODE)
        assertTrue(pythonCode.contains("```python"))

        // Zero messages vs populated chat history
        val zeroResp = OfflineAiEngine.generateResponse(
            prompt = "En son kime yazdım?",
            mode = AiMode.GENERAL,
            chatContextSummary = "ZERO_SENT_MESSAGES"
        )
        assertTrue(zeroResp.contains("kimseyle konuşmadın", ignoreCase = true))

        val populatedResp = OfflineAiEngine.generateResponse(
            prompt = "En son kime yazdım?",
            mode = AiMode.GENERAL,
            chatContextSummary = "EN SON YAZILAN KİŞİ/SOHBET: Zeynep (Saat: 14:30)"
        )
        assertTrue(populatedResp.contains("Zeynep"))
        assertTrue(populatedResp.contains("14:30"))
    }

    @Test
    fun `profanity filter enforces default on, 18+ verification challenge, and minor lockout`() {
        // Adult access (>=18 and no active parental restrictions) allows verification bypass
        ParentalControlManager.clearParentEmailVerification(context)
        ParentalControlManager.setUserAge(22, context)
        ProfanityFilter.enableFilter(context)
        assertTrue(ProfanityFilter.isFilterEnabled.value)
        assertEquals("Bu ***** mesajı", ProfanityFilter.filter("Bu aptal mesajı"))

        // Wrong code or unconfirmed checkbox fails
        val wrongCodeRes = ProfanityFilter.requestDisableWithVerification(
            context = context,
            adultConfirmed = true,
            expectedCode = "4821",
            enteredCode = "0000"
        )
        assertTrue(wrongCodeRes.isFailure)
        assertTrue(ProfanityFilter.isFilterEnabled.value)

        val unconfirmedRes = ProfanityFilter.requestDisableWithVerification(
            context = context,
            adultConfirmed = false,
            expectedCode = "4821",
            enteredCode = "4821"
        )
        assertTrue(unconfirmedRes.isFailure)
        assertTrue(ProfanityFilter.isFilterEnabled.value)

        // Valid 18+ confirmation + matching code turns censorship off
        val okRes = ProfanityFilter.requestDisableWithVerification(
            context = context,
            adultConfirmed = true,
            expectedCode = "4821",
            enteredCode = "4821"
        )
        assertTrue(okRes.isSuccess)
        assertFalse(ProfanityFilter.isFilterEnabled.value)
        assertEquals("Serbest metin aptal", ProfanityFilter.filter("Serbest metin aptal"))

        // Minor (<18) automatically locks profanity filter ON and rejects disabling
        ParentalControlManager.setUserAge(14, context)
        assertTrue(ParentalControlManager.state.value.isMinorMode)
        assertTrue(ProfanityFilter.isFilterEnabled.value)

        val minorBypassAttempt = ProfanityFilter.requestDisableWithVerification(
            context = context,
            adultConfirmed = true,
            expectedCode = "9999",
            enteredCode = "9999"
        )
        assertTrue(minorBypassAttempt.isFailure)
        assertTrue(ProfanityFilter.isFilterEnabled.value)

        // Restore adult non-restricted state
        ParentalControlManager.clearParentEmailVerification(context)
        ParentalControlManager.setUserAge(22, context)
    }

    @Test
    fun `parental controls track usage minutes, trigger screen-time lock, and log events`() {
        ParentalControlManager.setUserAge(12, context)
        ParentalControlManager.setDailyLimitMinutes(10)
        assertTrue(ParentalControlManager.state.value.isMinorMode)
        assertFalse(ParentalControlManager.state.value.isScreenTimeLocked)

        ParentalControlManager.logContactedPerson("Ayşe", "@ayse")
        ParentalControlManager.logNavigationEvent("Sohbet: Ayşe", "Giriş")
        assertTrue(ParentalControlManager.state.value.contactedPeers.any { it.peerName == "Ayşe" })
        assertTrue(ParentalControlManager.state.value.screenEvents.any { it.screenName == "Sohbet: Ayşe" })

        repeat(10) {
            ParentalControlManager.recordUsageMinute()
        }
        assertTrue(ParentalControlManager.state.value.isScreenTimeLocked)

        // Parent unlock adds extra minutes
        ParentalControlManager.unlockScreenTimeByParent(30)
        assertFalse(ParentalControlManager.state.value.isScreenTimeLocked)

        // Restore adult non-restricted state
        ParentalControlManager.clearParentEmailVerification(context)
        ParentalControlManager.setUserAge(22, context)
    }

    @Test
    fun `app translations switch cleanly across tr, en, de, and fr`() {
        val tr = AppTranslations.forCode("tr")
        val en = AppTranslations.forCode("en")
        val de = AppTranslations.forCode("de")
        val fr = AppTranslations.forCode("fr")

        assertEquals("Sohbetler", tr.tabChats)
        assertEquals("Sesli Oku", tr.aiReadAloud)
        assertEquals("Chats", en.tabChats)
        assertEquals("Read Aloud", en.aiReadAloud)
        assertEquals("Anrufe", de.tabCalls)
        assertEquals("Discussions", fr.tabChats)
    }

    @Test
    fun `linked devices manager and local mesh server companion pairing and tts html work`() {
        LinkedDevicesManager.init(context)
        LinkedDevicesManager.clearAllDevices()
        val pairingCode = LinkedDevicesManager.activePairingCode.value
        assertTrue(pairingCode.startsWith("GC-"))

        ParentalControlManager.clearParentEmailVerification(context)
        ParentalControlManager.setUserAge(22, context)
        ProfanityFilter.enableFilter(context)
        LocalMeshServer.clearMessages()
        LocalMeshServer.start(preferredPort = 18890)
        try {
            var waited = 0
            while (!LocalMeshServer.isRunning.value && waited < 2500) {
                Thread.sleep(50)
                waited += 50
            }
            assertTrue(LocalMeshServer.isRunning.value)
            val port = LocalMeshServer.serverPort.value

            // Verify companion portal serves SpeechSynthesis and SpeechRecognition HTML
            val companionConn = (URL("http://127.0.0.1:$port/companion").openConnection() as HttpURLConnection)
            assertEquals(200, companionConn.responseCode)
            val html = companionConn.inputStream.bufferedReader().readText()
            assertTrue(html.contains("speechSynthesis"))
            assertTrue(html.contains("SpeechRecognition"))
            assertTrue(html.contains("Sesli Oku"))

            // Verify companion pairing endpoint registers device
            val pairConn = (URL("http://127.0.0.1:$port/api/companion/pair").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            pairConn.outputStream.use {
                it.write("""{"code":"$pairingCode","deviceName":"iPhone Safari","platform":"iOS Safari"}""".toByteArray())
            }
            assertEquals(200, pairConn.responseCode)
            assertTrue(LinkedDevicesManager.devices.value.any { it.deviceName == "iPhone Safari" })

            // Verify guest message API filters profanity when filter is ON
            val msgConn = (URL("http://127.0.0.1:$port/api/messages").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            msgConn.outputStream.use {
                it.write("""{"sender":"TestUser","text":"Selam ahmak dostum"}""".toByteArray())
            }
            assertEquals(201, msgConn.responseCode)

            val listBody = URL("http://127.0.0.1:$port/api/messages").readText()
            assertFalse(listBody.contains("ahmak"))
            assertTrue(listBody.contains("*****"))
        } finally {
            LocalMeshServer.stop()
            LocalMeshServer.clearMessages()
            LinkedDevicesManager.clearAllDevices()
        }
    }

    @Test
    fun `parental email verification enforces valid email, 6-digit code, lockout, and clear`() {
        ParentalControlManager.clearParentEmailVerification(context)

        val invalidRes = ParentalControlManager.requestParentEmailVerification("gecersiz-eposta", context)
        assertTrue(invalidRes.isFailure)

        val code = ParentalControlManager.requestParentEmailVerification("ebeveyn@example.org", context).getOrThrow()
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })

        // 5 failed attempts trigger 30s lockout
        repeat(5) {
            val fail = ParentalControlManager.verifyParentEmailCode("000000", context)
            assertTrue(fail.isFailure)
        }
        val lockedOut = ParentalControlManager.verifyParentEmailCode(code, context)
        assertTrue(lockedOut.isFailure)

        // Re-issuing a fresh code resets cooldown and succeeds on exact match
        val freshCode = ParentalControlManager.requestParentEmailVerification("ebeveyn@example.org", context).getOrThrow()
        val ok = ParentalControlManager.verifyParentEmailCode(freshCode, context)
        assertTrue(ok.isSuccess)
        assertTrue(ParentalControlManager.state.value.isParentEmailVerified)
        assertTrue(ParentalControlManager.state.value.restrictionsEnabled)
        assertEquals("ebeveyn@example.org", ParentalControlManager.state.value.parentEmail)

        ParentalControlManager.clearParentEmailVerification(context)
        assertFalse(ParentalControlManager.state.value.isParentEmailVerified)
        assertFalse(ParentalControlManager.state.value.restrictionsEnabled)
    }

    @Test
    fun `encrypted vault enforces minimum passphrase and security utils hashes app lock`() {
        var threwShort = false
        try {
            EncryptedVaultUtil.encryptTranscript(
                context = context,
                chatTitle = "Test",
                messages = listOf("Ali" to "Selam"),
                passphrase = "12345"
            )
        } catch (_: IllegalArgumentException) {
            threwShort = true
        }
        assertTrue(threwShort)

        val result = EncryptedVaultUtil.encryptTranscript(
            context = context,
            chatTitle = "Test Sohbet",
            messages = listOf("Ali" to "Selam"),
            passphrase = "GuvenliParola123"
        )
        try {
            assertTrue(result.file.exists())
            assertTrue(result.previewEncryptedText.contains("PROTOCOL: GENERAL_CHAT_LOCAL_AES256"))
        } finally {
            result.file.delete()
        }

        com.example.data.SecurityUtils.clearAppLockPasscode(context)
        assertFalse(com.example.data.SecurityUtils.hasAppLockPasscode(context))
        com.example.data.SecurityUtils.saveAppLockPasscode(context, "8426")
        assertTrue(com.example.data.SecurityUtils.hasAppLockPasscode(context))
        assertTrue(com.example.data.SecurityUtils.verifyAppLockPasscode(context, "8426"))
        assertFalse(com.example.data.SecurityUtils.verifyAppLockPasscode(context, "1234"))
        com.example.data.SecurityUtils.clearAppLockPasscode(context)
        assertFalse(com.example.data.SecurityUtils.hasAppLockPasscode(context))
    }

    @Test
    fun `web research helper skips local chat queries and triggers factual lookup`() {
        assertFalse(WebResearchHelper.shouldResearchOnline("En son kime mesaj yazdım?"))
        assertFalse(WebResearchHelper.shouldResearchOnline("Bir mesajı arıyorum"))
        assertFalse(WebResearchHelper.shouldResearchOnline("Merhaba naber"))
        assertFalse(WebResearchHelper.shouldResearchOnline("Kotlin kodu yaz"))
        assertTrue(WebResearchHelper.shouldResearchOnline("Kuantum bilgisayar nedir?"))
    }

    @Test
    fun `file transfer manager supports 1_5GB+ P2P and LocalMeshServer HTTP 206 Range streaming`() {
        // 1. Verify capacity constants & routing modes
        assertEquals(100L * 1024L * 1024L, UnlimitedFileTransferManager.CLOUD_CHUNK_CAP_BYTES)
        assertEquals(1_610_612_736L, UnlimitedFileTransferManager.TARGET_LARGE_FILE_MIN_BYTES) // 1.5 GiB
        assertTrue(UnlimitedFileTransferManager.MAX_TRANSFER_FILE_BYTES >= UnlimitedFileTransferManager.TARGET_LARGE_FILE_MIN_BYTES)

        assertEquals("CLOUD_CHUNKS", UnlimitedFileTransferManager.chooseTransferMode(100L * 1024L * 1024L, isSignedIn = true))
        assertEquals("P2P_WEBRTC", UnlimitedFileTransferManager.chooseTransferMode(UnlimitedFileTransferManager.TARGET_LARGE_FILE_MIN_BYTES, isSignedIn = true))
        assertEquals("LAN_HTTP", UnlimitedFileTransferManager.chooseTransferMode(UnlimitedFileTransferManager.TARGET_LARGE_FILE_MIN_BYTES, isSignedIn = false))

        // 2. Verify HTTP 206 Partial Content byte range streaming on LocalMeshServer
        val sampleFile = java.io.File(context.filesDir, "range_test_sample.bin")
        val content = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        sampleFile.writeText(content)
        LocalMeshServer.start(preferredPort = 18895)
        try {
            var waited = 0
            while (!LocalMeshServer.isRunning.value && waited < 2500) {
                Thread.sleep(25)
                waited += 25
            }
            assertTrue(LocalMeshServer.isRunning.value)
            val lanUrl = LocalMeshServer.registerSharedFile("att_rangetest", sampleFile, "range_test_sample.bin", "application/octet-stream")
            assertTrue(lanUrl.endsWith("/api/files/att_rangetest"))
            val loopbackUrl = "http://127.0.0.1:${LocalMeshServer.serverPort.value}/api/files/att_rangetest"

            val partialConn = (URL(loopbackUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                setRequestProperty("Range", "bytes=10-19")
            }
            assertEquals(206, partialConn.responseCode)
            assertEquals("bytes", partialConn.getHeaderField("Accept-Ranges"))
            assertEquals("bytes 10-19/${content.length}", partialConn.getHeaderField("Content-Range"))
            assertEquals("ABCDEFGHIJ", partialConn.inputStream.bufferedReader().readText())

            // Full 200 OK download matches exact payload
            val fullConn = (URL(loopbackUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
            }
            assertEquals(200, fullConn.responseCode)
            assertEquals(content, fullConn.inputStream.bufferedReader().readText())
        } finally {
            LocalMeshServer.stop()
            sampleFile.delete()
        }
    }

    @Test
    fun `self chat storage persists device-only private notes cleanly`() {
        SelfChatStorage.clear(context)
        val initial = SelfChatStorage.loadMessages(context)
        assertTrue(initial.isEmpty())

        val sample = com.example.data.DirectMessage(
            id = "self_note_1",
            conversationId = SelfChatStorage.SELF_NOTES_CONVERSATION_ID,
            senderName = "Siz",
            isMe = true,
            text = "Alışveriş listesi ve gizli notlar",
            time = "10:15"
        )
        SelfChatStorage.saveMessages(context, listOf(sample))
        val loaded = SelfChatStorage.loadMessages(context)
        assertEquals(1, loaded.size)
        assertEquals("Alışveriş listesi ve gizli notlar", loaded.first().text)
        assertEquals(SelfChatStorage.SELF_NOTES_CONVERSATION_ID, loaded.first().conversationId)

        SelfChatStorage.clear(context)
        assertTrue(SelfChatStorage.loadMessages(context).isEmpty())
    }

    @Test
    fun `feedback analytics manager enforces kvkk consent, computes summary KPIs, and exports json and csv`() {
        FeedbackAnalyticsManager.clearLocalFeedbackHistory(context)
        FeedbackAnalyticsManager.init(context)

        // Requires KVKK consent checkbox
        val deniedRes = FeedbackAnalyticsManager.submitReport(
            context = context,
            category = FeedbackCategory.BUG,
            severity = FeedbackSeverity.HIGH,
            rating = 3,
            moduleTag = "GEN AI",
            title = "Gecikme Testi",
            description = "Bağlantı kesilince uyarı gösterildi.",
            kvkkApproved = false
        )
        assertTrue(deniedRes.isFailure)

        // Submit structured reports
        FeedbackAnalyticsManager.submitReport(
            context = context,
            category = FeedbackCategory.BUG,
            severity = FeedbackSeverity.HIGH,
            rating = 2,
            moduleTag = "Konum & Galeri",
            title = "Harita butonu testi",
            description = "Konum etiketinde küçük hizalama hatası var.",
            kvkkApproved = true
        ).getOrThrow()

        FeedbackAnalyticsManager.submitReport(
            context = context,
            category = FeedbackCategory.SUGGESTION,
            severity = FeedbackSeverity.LOW,
            rating = 5,
            moduleTag = "GEN AI",
            title = "Yeni tema önerisi",
            description = "Gece modu için ekstra kontrast seçeneği harika olur.",
            kvkkApproved = true
        ).getOrThrow()

        val summary = FeedbackAnalyticsManager.summary.value
        assertEquals(2, summary.totalCount)
        assertEquals(3.5, summary.averageRating, 0.01)
        assertEquals(1, summary.categoryCounts[FeedbackCategory.BUG])
        assertEquals(1, summary.categoryCounts[FeedbackCategory.SUGGESTION])
        assertEquals(1, summary.openBugCount)

        val jsonExport = FeedbackAnalyticsManager.exportAnalyticsJson()
        assertTrue(jsonExport.contains("analyticsSummary"))
        assertTrue(jsonExport.contains("Harita butonu testi"))
        assertTrue(jsonExport.contains("SUGGESTION"))

        val csvExport = FeedbackAnalyticsManager.exportAnalyticsCsv()
        assertTrue(csvExport.startsWith("id,createdAtIso,category,severity,rating"))
        assertTrue(csvExport.contains("Yeni tema önerisi"))

        FeedbackAnalyticsManager.clearLocalFeedbackHistory(context)
        assertEquals(0, FeedbackAnalyticsManager.summary.value.totalCount)
    }

    @Test
    fun `network connectivity manager detects offline transitions, persists outbox and cache, and erases under kvkk`() {
        NetworkConnectivityAndOfflineManager.clearAll(context)
        NetworkConnectivityAndOfflineManager.init(context)

        // Transition to offline triggers custom offline-first notification
        NetworkConnectivityAndOfflineManager.onConnectivityChanged(
            isOnline = false,
            transportLabel = "Çevrimdışı",
            context = context
        )
        val offlineState = NetworkConnectivityAndOfflineManager.serviceStatus.value
        assertFalse(offlineState.isOnline)
        assertEquals("Çevrimdışı", offlineState.transportLabel)
        assertTrue(offlineState.offlineModeNotificationVisible)
        assertTrue(offlineState.offlineNotificationText.contains("Çevrimdışı", ignoreCase = true))

        // Enqueue offline message persists in LocalStorage (SharedPreferences) + conversation cache
        val convId = "conv_offline_test_1"
        val offlineMsg = com.example.data.DirectMessage(
            id = "msg_off_101",
            conversationId = convId,
            senderName = "Ben",
            isMe = true,
            text = "İnternet yokken kaydedilen mesaj",
            time = "12:00",
            timestamp = 1700000001000L
        )
        NetworkConnectivityAndOfflineManager.enqueueOfflineMessage(
            context = context,
            conversationId = convId,
            message = offlineMsg,
            participantIds = listOf("u1", "u2"),
            senderDisplayName = "Test Kullanıcı"
        )

        assertEquals(1, NetworkConnectivityAndOfflineManager.serviceStatus.value.pendingOutboxCount)
        val pendingList = NetworkConnectivityAndOfflineManager.getPendingMessagesForConversation(context, convId)
        assertEquals(1, pendingList.size)
        assertEquals("İnternet yokken kaydedilen mesaj", pendingList.first().text)

        val cachedList = NetworkConnectivityAndOfflineManager.loadCachedConversationMessages(context, convId)
        assertEquals(1, cachedList.size)
        assertEquals("msg_off_101", cachedList.first().id)

        // Transition back online clears offline-only alert banner while preserving pending count until synced
        NetworkConnectivityAndOfflineManager.onConnectivityChanged(
            isOnline = true,
            transportLabel = "Wi-Fi",
            context = context
        )
        val onlineState = NetworkConnectivityAndOfflineManager.serviceStatus.value
        assertTrue(onlineState.isOnline)
        assertEquals("Wi-Fi", onlineState.transportLabel)
        assertFalse(onlineState.offlineModeNotificationVisible)

        // KVKK Article 11 erasure wipes cached conversations & queued messages
        NetworkConnectivityAndOfflineManager.clearAll(context)
        assertEquals(0, NetworkConnectivityAndOfflineManager.serviceStatus.value.pendingOutboxCount)
        assertTrue(NetworkConnectivityAndOfflineManager.getPendingMessagesForConversation(context, convId).isEmpty())
        assertTrue(NetworkConnectivityAndOfflineManager.loadCachedConversationMessages(context, convId).isEmpty())
    }

    @Test
    fun `local mesh server root web client serves service status dashboard with navigator onLine and localStorage`() {
        LocalMeshServer.clearMessages()
        LocalMeshServer.start(preferredPort = 18899)
        try {
            var waited = 0
            while (!LocalMeshServer.isRunning.value && waited < 2500) {
                Thread.sleep(25)
                waited += 25
            }
            assertTrue(LocalMeshServer.isRunning.value)
            val port = LocalMeshServer.serverPort.value
            val rootConn = (URL("http://127.0.0.1:$port/").openConnection() as HttpURLConnection)
            assertEquals(200, rootConn.responseCode)
            val html = rootConn.inputStream.bufferedReader().readText()

            assertTrue(html.contains("navigator.onLine"))
            assertTrue(html.contains("localStorage"))
            assertTrue(html.contains("serviceStatusCard"))
            assertTrue(html.contains("offlineModeNotification"))
            assertTrue(html.contains("gc_web_outbox_v1"))
            assertTrue(html.contains("50dvh"))
            assertTrue(html.contains(":focus-visible"))
            assertTrue(html.contains("prefers-reduced-motion"))
            assertTrue(html.contains("<header"))
            assertTrue(html.contains("<main"))
            assertTrue(html.contains("userAgentData"))
        } finally {
            LocalMeshServer.stop()
            LocalMeshServer.clearMessages()
        }
    }

    @Test
    fun `e2ee crypto manager encrypts with ECDH P256 and AES256-GCM and isolates conversations`() {
        val kpg = java.security.KeyPairGenerator.getInstance("EC").apply {
            initialize(java.security.spec.ECGenParameterSpec("secp256r1"))
        }
        val alice = kpg.generateKeyPair()
        val bob = kpg.generateKeyPair()
        val mallory = kpg.generateKeyPair()

        val convId = "dm_alice_bob"
        val secretText = "Uçtan uca şifreli gizli mesaj 4829!"
        val payload = E2eeCryptoManager.encryptWithKeys(
            myPrivateKey = alice.private,
            peerPublicKey = bob.public,
            conversationId = convId,
            plaintext = secretText
        )

        assertTrue(E2eeCryptoManager.isE2eeCiphertext(payload))
        assertTrue(payload.startsWith("E2EE:v1:"))
        assertFalse(payload.contains("gizli mesaj"))

        // Recipient Bob decrypts using his private key + Alice's public key
        val bobPlain = E2eeCryptoManager.decryptWithKeys(
            myPrivateKey = bob.private,
            peerPublicKey = alice.public,
            conversationId = convId,
            payload = payload
        )
        assertEquals(secretText, bobPlain)

        // Third party Mallory or wrong conversation AAD fails AEAD verification
        val wrongConvDecrypt = try {
            E2eeCryptoManager.decryptWithKeys(bob.private, alice.public, "dm_other_conv", payload)
        } catch (_: Exception) {
            null
        }
        assertEquals(null, wrongConvDecrypt)

        val malloryDecrypt = try {
            E2eeCryptoManager.decryptWithKeys(mallory.private, alice.public, convId, payload)
        } catch (_: Exception) {
            null
        }
        assertEquals(null, malloryDecrypt)

        val pubB64 = android.util.Base64.encodeToString(alice.public.encoded, android.util.Base64.NO_WRAP)
        val fp = E2eeCryptoManager.getKeyFingerprint(pubB64)
        assertTrue(fp.contains("-"))
        assertEquals(19, fp.length) // XXXX-XXXX-XXXX-XXXX
    }

    @Test
    fun `security utils enforces 4-digit pin rules and background or panic lock preferences`() {
        com.example.data.SecurityUtils.clearAppLockPasscode(context)

        // Rejects non-4-digit PIN when isPinMode = true
        var rejectedInvalidPin = false
        try {
            com.example.data.SecurityUtils.saveAppLockPasscode(context, "12ab", isPinMode = true)
        } catch (_: IllegalArgumentException) {
            rejectedInvalidPin = true
        }
        assertTrue(rejectedInvalidPin)

        // Saves strict 4-digit numeric PIN and background/panic toggles
        com.example.data.SecurityUtils.saveAppLockPasscode(context, "9514", isPinMode = true)
        assertTrue(com.example.data.SecurityUtils.hasAppLockPasscode(context))
        assertTrue(com.example.data.SecurityUtils.isAppLockPinMode(context))
        assertTrue(com.example.data.SecurityUtils.verifyAppLockPasscode(context, "9514"))
        assertFalse(com.example.data.SecurityUtils.verifyAppLockPasscode(context, "0000"))

        assertTrue(com.example.data.SecurityUtils.isLockOnBackgroundEnabled(context))
        assertTrue(com.example.data.SecurityUtils.isLockOnPanicEnabled(context))
        com.example.data.SecurityUtils.setLockOnBackgroundEnabled(context, false)
        com.example.data.SecurityUtils.setLockOnPanicEnabled(context, false)
        assertFalse(com.example.data.SecurityUtils.isLockOnBackgroundEnabled(context))
        assertFalse(com.example.data.SecurityUtils.isLockOnPanicEnabled(context))

        // Background suppression single-shot guard
        com.example.data.SecurityUtils.suppressNextBackgroundLockOnce()
        assertTrue(com.example.data.SecurityUtils.consumeSuppressNextBackgroundLock())
        assertFalse(com.example.data.SecurityUtils.consumeSuppressNextBackgroundLock())

        com.example.data.SecurityUtils.clearAppLockPasscode(context)
        assertFalse(com.example.data.SecurityUtils.hasAppLockPasscode(context))
    }

    @Test
    fun `auth viewmodel configures 4-digit PIN, locks screen preserving active tab, and clears lock`() {
        com.example.data.SecurityUtils.clearAppLockPasscode(context)
        val db = com.example.data.AppDatabase.getDatabase(context)
        val authVm = com.example.ui.AuthViewModel(com.example.data.AuthRepository(db.userDao()))

        // Without PIN, lockScreenIfConfigured returns false
        assertFalse(authVm.lockScreenIfConfigured(context))

        // Configure 4-digit PIN from Ayarlar (tab 4)
        authVm.selectedMainTab.value = 4
        authVm.setAppLockPasscode(context, "4729", isPinMode = true)
        assertTrue(authVm.hasAppLock.value)
        assertTrue(authVm.isPinLockMode.value)
        assertTrue(authVm.verifyAppLockPasscode(context, "4729"))
        assertFalse(authVm.verifyAppLockPasscode(context, "1111"))

        // Lock screen triggers Screen.AppLockVerify while preserving active tab
        assertTrue(authVm.lockScreenIfConfigured(context))
        assertEquals(com.example.ui.Screen.AppLockVerify, authVm.currentScreen.value)
        assertEquals(4, authVm.selectedMainTab.value)

        // Removing lock resets hasAppLock cleanly
        authVm.clearAppLock(context)
        assertFalse(authVm.hasAppLock.value)
        assertFalse(authVm.lockScreenIfConfigured(context))
    }

    @Test
    fun `security utils enforces 1 min then +5 min escalating lockout per 5 failed attempts`() {
        com.example.data.SecurityUtils.clearAppLockPasscode(context)
        com.example.data.SecurityUtils.saveAppLockPasscode(context, "7391", isPinMode = true)

        var now = 1_700_000_000_000L
        // Attempts 1..4 do not lock out yet
        for (i in 1..4) {
            val st = com.example.data.SecurityUtils.recordFailedLockAttempt(context, nowMs = now)
            assertFalse(st.isLockedOut)
            assertEquals(5 - i, st.remainingAttempts)
        }

        // 5th wrong attempt triggers 1-minute lockout (Stage 1)
        val stage1 = com.example.data.SecurityUtils.recordFailedLockAttempt(context, nowMs = now)
        assertTrue(stage1.isLockedOut)
        assertEquals(1, stage1.lockoutStage)
        assertEquals(1, stage1.lockoutMinutes)
        assertEquals(60, stage1.remainingSeconds)
        assertEquals(60, com.example.data.SecurityUtils.getLockoutRemainingSeconds(context, nowMs = now))

        // Advance time past 1 minute lockout; attempts 1..4 open again
        now += 61_000L
        assertEquals(0, com.example.data.SecurityUtils.getLockoutRemainingSeconds(context, nowMs = now))
        repeat(4) {
            assertFalse(com.example.data.SecurityUtils.recordFailedLockAttempt(context, nowMs = now).isLockedOut)
        }

        // 5th wrong attempt in round 2 adds +5 min => 6 minutes (360s)
        val stage2 = com.example.data.SecurityUtils.recordFailedLockAttempt(context, nowMs = now)
        assertTrue(stage2.isLockedOut)
        assertEquals(2, stage2.lockoutStage)
        assertEquals(6, stage2.lockoutMinutes)
        assertEquals(360, stage2.remainingSeconds)

        // Advance time past 6 minutes; 5 wrong attempts in round 3 adds +5 min => 11 minutes (660s)
        now += 361_000L
        repeat(4) {
            assertFalse(com.example.data.SecurityUtils.recordFailedLockAttempt(context, nowMs = now).isLockedOut)
        }
        val stage3 = com.example.data.SecurityUtils.recordFailedLockAttempt(context, nowMs = now)
        assertTrue(stage3.isLockedOut)
        assertEquals(3, stage3.lockoutStage)
        assertEquals(11, stage3.lockoutMinutes)
        assertEquals(660, stage3.remainingSeconds)

        // Successful unlock resets stage and attempts
        com.example.data.SecurityUtils.resetLockAttemptState(context)
        assertEquals(0, com.example.data.SecurityUtils.getFailedLockAttempts(context))
        assertEquals(0, com.example.data.SecurityUtils.getLockoutStage(context))
        assertEquals(0, com.example.data.SecurityUtils.getLockoutRemainingSeconds(context, nowMs = now))
        com.example.data.SecurityUtils.clearAppLockPasscode(context)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `imha protocol removes security lock and wipes local and session state`() {
        val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val authVm = com.example.ui.AuthViewModel(com.example.data.AuthRepository(db.userDao()))
            authVm.setAppLockPasscode(context, "8842", isPinMode = true)
            assertTrue(com.example.data.SecurityUtils.hasAppLockPasscode(context))

            val doneLatch = java.util.concurrent.CountDownLatch(1)
            authVm.selfDestructOnForgottenLock(context, db) {
                doneLatch.countDown()
            }
            assertTrue(doneLatch.await(5, java.util.concurrent.TimeUnit.SECONDS))

            assertFalse(com.example.data.SecurityUtils.hasAppLockPasscode(context))
            assertFalse(authVm.hasAppLock.value)
            assertEquals(com.example.ui.Screen.Splash, authVm.currentScreen.value)
        } finally {
            db.close()
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }

    @Test
    fun `private LAN HTTP socket client enforces private IPs and exchanges peer messages`() {
        assertTrue(LocalMeshServer.isPrivateLanHost("192.168.1.42"))
        assertTrue(LocalMeshServer.isPrivateLanHost("10.0.0.15"))
        assertTrue(LocalMeshServer.isPrivateLanHost("172.16.4.8"))
        assertTrue(LocalMeshServer.isPrivateLanHost("127.0.0.1"))
        assertTrue(LocalMeshServer.isPrivateLanHost("localhost"))
        assertFalse(LocalMeshServer.isPrivateLanHost("8.8.8.8"))
        assertFalse(LocalMeshServer.isPrivateLanHost("example.com"))
        assertFalse(LocalMeshServer.isPrivateLanHost("172.32.0.1"))
        assertFalse(LocalMeshServer.isPrivateLanHost("192.169.1.1"))

        LocalMeshServer.clearMessages()
        LocalMeshServer.start(preferredPort = 18905)
        try {
            var waited = 0
            while (!LocalMeshServer.isRunning.value && waited < 2500) {
                Thread.sleep(25)
                waited += 25
            }
            assertTrue(LocalMeshServer.isRunning.value)
            val port = LocalMeshServer.serverPort.value

            kotlinx.coroutines.runBlocking {
                val blockedRes = LocalMeshServer.sendRemoteMessage("http://8.8.8.8:8765", "Ali", "Merhaba")
                assertTrue(blockedRes.isFailure)

                val sendRes = LocalMeshServer.sendRemoteMessage("http://127.0.0.1:$port", "Cihaz 2", "Yerel ağ P2P testi")
                assertTrue(sendRes.isSuccess)

                val fetched = LocalMeshServer.fetchRemoteMessages("127.0.0.1:$port").getOrThrow()
                assertTrue(fetched.any { it.sender == "Cihaz 2" && it.text == "Yerel ağ P2P testi" })
            }
        } finally {
            LocalMeshServer.stop()
            LocalMeshServer.clearMessages()
        }
    }

    @Test
    fun `manifest disables allowBackup and restricts unencrypted backup extraction`() {
        val allowBackupFlag = context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP
        assertEquals(0, allowBackupFlag)
    }
}
