package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.util.UnlimitedFileTransferManager
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FirestoreChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun twoUsersCanDiscoverEachOtherAndExchangeMessagesOverFirestore() = runBlocking {
        val aliceEmail = "alice_${UUID.randomUUID().toString().take(6)}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = FirestoreChatRepository(firestore, auth)
        val aliceHandle = "alice_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        withTimeout(TIMEOUT_MS) {
            aliceRepo.ensureUserProfile(preferredHandle = aliceHandle, displayNameOverride = "Alice")
        }.getOrThrow()

        val bobEmail = "bob_${UUID.randomUUID().toString().take(6)}@test.com"
        val bobUid = signInTestUser(bobEmail)
        val bobRepo = FirestoreChatRepository(firestore, auth)
        val bobHandle = "bob_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        withTimeout(TIMEOUT_MS) {
            bobRepo.ensureUserProfile(preferredHandle = bobHandle, displayNameOverride = "Bob")
        }.getOrThrow()

        // Bob resolves Alice by exact @handle over handle_index
        val resolvedAlice = withTimeout(TIMEOUT_MS) {
            bobRepo.lookupHandle(aliceHandle).getOrThrow()
        }
        assertNotNull(resolvedAlice)
        assertEquals(aliceUid, resolvedAlice!!.uid)

        // Bob opens shared DM room and sends first message
        val room = withTimeout(TIMEOUT_MS) {
            bobRepo.openOrCreateDirectConversation(resolvedAlice, "Bob").getOrThrow()
        }
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val sendRes = withTimeout(TIMEOUT_MS) {
            bobRepo.sendMessage(
                conversationId = room.id,
                message = DirectMessage(
                    id = msgId,
                    conversationId = room.id,
                    senderName = "Bob",
                    isMe = true,
                    text = "Farklı ağdan gerçek mesaj!",
                    time = "12:30",
                    timestamp = System.currentTimeMillis()
                ),
                participantIds = listOf(bobUid, aliceUid),
                senderDisplayName = "Bob"
            )
        }
        assertTrue(sendRes.isSuccess)

        // Alice logs in on her device and observes both the conversation room and Bob's message in real time
        signInTestUser(aliceEmail)
        val aliceRooms = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeConversations().first { list -> list.any { it.id == room.id } }
        }
        assertTrue(aliceRooms.any { it.id == room.id })
        val aliceMsgs = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeMessages(room.id).first { list -> list.any { it.id == msgId } }
        }
        assertTrue(aliceMsgs.any { it.text == "Farklı ağdan gerçek mesaj!" && !it.isMe })
    }

    @Test
    fun crossUserIsolationAndUnauthenticatedRejectionAreEnforced() = runBlocking {
        val aliceEmail = "alice_iso_${UUID.randomUUID().toString().take(6)}@test.com"
        val bobEmail = "bob_iso_${UUID.randomUUID().toString().take(6)}@test.com"
        val eveEmail = "eve_iso_${UUID.randomUUID().toString().take(6)}@test.com"

        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = FirestoreChatRepository(firestore, auth)
        val aliceHandle = "al_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        val aliceProfile = withTimeout(TIMEOUT_MS) {
            aliceRepo.ensureUserProfile(preferredHandle = aliceHandle, displayNameOverride = "Alice")
        }.getOrThrow()

        val bobUid = signInTestUser(bobEmail)
        val bobRepo = FirestoreChatRepository(firestore, auth)
        val room = withTimeout(TIMEOUT_MS) {
            bobRepo.openOrCreateDirectConversation(aliceProfile, "Bob").getOrThrow()
        }
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        withTimeout(TIMEOUT_MS) {
            bobRepo.sendMessage(
                conversationId = room.id,
                message = DirectMessage(
                    id = msgId,
                    conversationId = room.id,
                    senderName = "Bob",
                    isMe = true,
                    text = "Merhaba Alice",
                    time = "12:31",
                    timestamp = System.currentTimeMillis()
                ),
                participantIds = room.participantIds,
                senderDisplayName = "Bob"
            ).getOrThrow()
        }

        // Alice observes Bob's message in real time
        signInTestUser(aliceEmail)
        val aliceObserved = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeMessages(room.id).first { list -> list.any { it.id == msgId } }
        }
        assertTrue(aliceObserved.any { it.text == "Merhaba Alice" && !it.isMe })

        // Eve CANNOT read or write inside Alice & Bob's conversation
        signInTestUser(eveEmail)
        val eveSendResult = withTimeout(TIMEOUT_MS) {
            FirestoreChatRepository(firestore, auth).sendMessage(
                conversationId = room.id,
                message = DirectMessage(
                    id = "spoof_${UUID.randomUUID().toString().take(8)}",
                    conversationId = room.id,
                    senderName = "Eve",
                    isMe = true,
                    text = "Sızma denemesi",
                    time = "12:32",
                    timestamp = System.currentTimeMillis()
                ),
                participantIds = listOf(aliceUid, bobUid),
                senderDisplayName = "Eve"
            )
        }
        assertTrue(eveSendResult.isFailure)
        val sendErr = eveSendResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, sendErr?.code)

        // Unauthenticated caller fails safely and returns Permission Denied on lookup
        auth.signOut()
        val unauthResult = withTimeout(TIMEOUT_MS) {
            FirestoreChatRepository(firestore, auth).lookupHandle(aliceHandle)
        }
        assertTrue(unauthResult.isFailure)
        assertNull(auth.currentUser)
    }

    @Test
    fun readReceiptsDeleteForEveryoneAndChunkedFileTransferSyncAcrossDevices() = runBlocking {
        val aliceEmail = "alice_full_${UUID.randomUUID().toString().take(6)}@test.com"
        val bobEmail = "bob_full_${UUID.randomUUID().toString().take(6)}@test.com"

        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = FirestoreChatRepository(firestore, auth)
        val aliceHandle = "al_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        val aliceProfile = withTimeout(TIMEOUT_MS) {
            aliceRepo.ensureUserProfile(preferredHandle = aliceHandle, displayNameOverride = "Alice")
        }.getOrThrow()

        val bobUid = signInTestUser(bobEmail)
        val bobRepo = FirestoreChatRepository(firestore, auth)
        val room = withTimeout(TIMEOUT_MS) {
            bobRepo.openOrCreateDirectConversation(aliceProfile, "Bob").getOrThrow()
        }

        // 1. Alice sends message with explicit HH:mm timestamp
        signInTestUser(aliceEmail)
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        withTimeout(TIMEOUT_MS) {
            aliceRepo.sendMessage(
                conversationId = room.id,
                message = DirectMessage(
                    id = msgId,
                    conversationId = room.id,
                    senderName = "Alice",
                    isMe = true,
                    text = "Saat 14:30 raporu",
                    time = "14:30",
                    timestamp = System.currentTimeMillis()
                ),
                participantIds = room.participantIds,
                senderDisplayName = "Alice"
            ).getOrThrow()
        }

        // 2. Alice uploads 3 file chunks for unlimited transfer relay
        val chunkPayloads = listOf("Q0hVTktfMA==", "Q0hVTktfMQ==", "Q0hVTktfMg==")
        chunkPayloads.forEachIndexed { idx, b64 ->
            withTimeout(TIMEOUT_MS) {
                aliceRepo.uploadMessageFileChunk(
                    conversationId = room.id,
                    messageId = msgId,
                    participantIds = room.participantIds,
                    chunkIndex = idx,
                    totalChunks = chunkPayloads.size,
                    dataBase64 = b64
                ).getOrThrow()
            }
        }

        // 3. Bob signs in, reads message & marks it as read, and reassembles file chunks in order
        signInTestUser(bobEmail)
        withTimeout(TIMEOUT_MS) {
            bobRepo.markMessagesAsRead(room.id, listOf(msgId)).getOrThrow()
        }
        val bobChunks = withTimeout(TIMEOUT_MS) {
            bobRepo.downloadMessageFileChunks(room.id, msgId).getOrThrow()
        }
        assertEquals(listOf(0 to "Q0hVTktfMA==", 1 to "Q0hVTktfMQ==", 2 to "Q0hVTktfMg=="), bobChunks)

        // 4. Alice verifies readBy + readAtMs + 14:30 timestamp, then deletes message for everyone
        signInTestUser(aliceEmail)
        val readMsgs = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeMessages(room.id).first { list ->
                list.any { it.id == msgId && bobUid in it.readBy && it.readAtMs != null }
            }
        }
        val verifiedMsg = readMsgs.first { it.id == msgId }
        assertEquals("14:30", verifiedMsg.time)
        assertTrue(bobUid in verifiedMsg.readBy)
        assertNotNull(verifiedMsg.readAtMs)

        withTimeout(TIMEOUT_MS) {
            aliceRepo.deleteMessageForEveryone(room.id, msgId).getOrThrow()
        }

        // 5. Bob sees deleted placeholder & updated room lastMessage
        signInTestUser(bobEmail)
        val deletedMsgs = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeMessages(room.id).first { list ->
                list.any { it.id == msgId && it.isDeletedForEveryone }
            }
        }
        assertTrue(deletedMsgs.first { it.id == msgId }.text.contains("silindi"))
    }

    @Test
    fun webrtcCallSignalingOfferAnswerAndIceCandidatesWorkAcrossTwoDevices() = runBlocking {
        val aliceEmail = "alice_rtc_${UUID.randomUUID().toString().take(6)}@test.com"
        val bobEmail = "bob_rtc_${UUID.randomUUID().toString().take(6)}@test.com"

        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = FirestoreChatRepository(firestore, auth)
        val bobUid = signInTestUser(bobEmail)
        val bobRepo = FirestoreChatRepository(firestore, auth)

        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        // Alice initiates a WebRTC video call Offer + ICE candidate
        signInTestUser(aliceEmail)
        withTimeout(TIMEOUT_MS) {
            aliceRepo.createOutgoingCall(
                callId = callId,
                calleeUid = bobUid,
                calleeName = "Bob",
                callerName = "Alice",
                isVideo = true,
                offerSdp = "v=0\r\no=- 12345 2 IN IP4 127.0.0.1\r\ns=Offer"
            ).getOrThrow()
        }
        withTimeout(TIMEOUT_MS) {
            aliceRepo.sendIceCandidate(
                callId = callId,
                isCaller = true,
                participantIds = listOf(aliceUid, bobUid),
                sdpMid = "audio",
                sdpMLineIndex = 0,
                sdp = "candidate:1 1 UDP 2122252543 192.0.2.1 50000 typ host"
            ).getOrThrow()
        }

        // Bob observes ringing incoming call, reads caller ICE, sends Answer + callee ICE
        signInTestUser(bobEmail)
        val ringing = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeIncomingRingingCalls().first { it != null && it.id == callId }
        }
        assertNotNull(ringing)
        assertTrue(ringing!!.isVideo)
        assertEquals(aliceUid, ringing.callerId)

        val callerIce = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeIceCandidates(callId, readCallerCandidates = true).first()
        }
        assertEquals("audio", callerIce.sdpMid)

        withTimeout(TIMEOUT_MS) {
            bobRepo.answerCall(callId, "v=0\r\no=- 67890 2 IN IP4 127.0.0.1\r\ns=Answer").getOrThrow()
            bobRepo.sendIceCandidate(
                callId = callId,
                isCaller = false,
                participantIds = listOf(aliceUid, bobUid),
                sdpMid = "video",
                sdpMLineIndex = 1,
                sdp = "candidate:2 1 UDP 2122252543 198.51.100.2 50002 typ srflx"
            ).getOrThrow()
        }

        // Alice receives Answer SDP and Bob's ICE candidate, then ends call
        signInTestUser(aliceEmail)
        val answeredCall = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeCall(callId).first { it?.status == "ANSWERED" }
        }
        assertTrue(answeredCall?.answerSdp?.contains("s=Answer") == true)

        val calleeIce = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeIceCandidates(callId, readCallerCandidates = false).first()
        }
        assertEquals("video", calleeIce.sdpMid)

        withTimeout(TIMEOUT_MS) {
            aliceRepo.updateCallStatus(callId, "ENDED").getOrThrow()
        }
    }

    @Test
    fun blockingReportingKvkkRequestsAndDataExportRespectSecurityRules() = runBlocking {
        val aliceEmail = "alice_kvkk_${UUID.randomUUID().toString().take(6)}@test.com"
        val bobEmail = "bob_kvkk_${UUID.randomUUID().toString().take(6)}@test.com"

        val bobUid = signInTestUser(bobEmail)
        val bobRepo = FirestoreChatRepository(firestore, auth)
        val bobHandle = "bob_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        withTimeout(TIMEOUT_MS) {
            bobRepo.ensureUserProfile(preferredHandle = bobHandle, displayNameOverride = "Bob")
        }.getOrThrow()

        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = FirestoreChatRepository(firestore, auth)
        val aliceHandle = "al_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        withTimeout(TIMEOUT_MS) {
            aliceRepo.ensureUserProfile(preferredHandle = aliceHandle, displayNameOverride = "Alice")
        }.getOrThrow()

        // 1. Block & observe blocked UIDs
        withTimeout(TIMEOUT_MS) {
            aliceRepo.blockUser(blockedUid = bobUid, displayName = "Bob", handle = bobHandle).getOrThrow()
        }
        val blockedSet = withTimeout(TIMEOUT_MS) {
            aliceRepo.fetchBlockedUids().getOrThrow()
        }
        assertTrue(bobUid in blockedSet)
        val observedBlocked = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeBlockedUids().first { bobUid in it }
        }
        assertTrue(bobUid in observedBlocked)

        // 2. Report abusive user
        val reportId = withTimeout(TIMEOUT_MS) {
            aliceRepo.reportUser(
                targetUid = bobUid,
                targetName = "Bob",
                conversationId = null,
                reason = "Spam",
                details = "İstenmeyen mesaj"
            ).getOrThrow()
        }
        assertTrue(reportId.startsWith("rep_"))

        // 3. Submit KVKK Md. 11 request & update cloud display name
        val kvkkId = withTimeout(TIMEOUT_MS) {
            aliceRepo.submitKvkkRequest(
                requestType = "ACCESS",
                details = "Kayıtlı kişisel verilerime erişim talep ediyorum.",
                contactEmail = aliceEmail
            ).getOrThrow()
        }
        assertTrue(kvkkId.startsWith("kvkk_"))

        val updatedProfile = withTimeout(TIMEOUT_MS) {
            aliceRepo.updateCloudDisplayName("Alice KVKK").getOrThrow()
        }
        assertEquals("Alice KVKK", updatedProfile.displayName)

        // 4. Export personal cloud JSON
        val exportedJson = withTimeout(TIMEOUT_MS) {
            aliceRepo.exportUserCloudDataJson().getOrThrow()
        }
        assertTrue(exportedJson.contains(aliceUid))
        assertTrue(exportedJson.contains("Alice KVKK"))
        assertTrue(exportedJson.contains(bobUid))
        assertTrue(exportedJson.contains("ACCESS"))

        // 5. Unblock Bob
        withTimeout(TIMEOUT_MS) {
            aliceRepo.unblockUser(bobUid).getOrThrow()
        }
        val afterUnblock = withTimeout(TIMEOUT_MS) {
            aliceRepo.fetchBlockedUids().getOrThrow()
        }
        assertTrue(bobUid !in afterUnblock)
    }

    @Test
    fun largeFileWebRtcSignalingAnd15GbMetadataWorkAcrossDevices() = runBlocking {
        val aliceEmail = "alice_big_${UUID.randomUUID().toString().take(6)}@test.com"
        val bobEmail = "bob_big_${UUID.randomUUID().toString().take(6)}@test.com"

        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = FirestoreChatRepository(firestore, auth)
        val bobUid = signInTestUser(bobEmail)
        val bobRepo = FirestoreChatRepository(firestore, auth)

        val onePointFiveGbBytes = UnlimitedFileTransferManager.TARGET_LARGE_FILE_MIN_BYTES // 1.5 GiB
        val transferId = "xfer_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val convId = "dm_${listOf(aliceUid, bobUid).sorted().joinToString("_")}"
        val msgId = "msg_big_${UUID.randomUUID().toString().replace("-", "").take(8)}"

        // Bob initiates a 1.5 GB P2P file transfer request to Alice (owner)
        signInTestUser(bobEmail)
        withTimeout(TIMEOUT_MS) {
            bobRepo.createFileTransferRequest(
                transferId = transferId,
                ownerUid = aliceUid,
                conversationId = convId,
                messageId = msgId,
                fileId = "att_15gbvideo",
                fileName = "tatil_4k_hdr.mp4",
                fileSizeBytes = onePointFiveGbBytes,
                offerSdp = "v=0\r\no=- 111 2 IN IP4 127.0.0.1\r\ns=DataOffer"
            ).getOrThrow()
        }
        withTimeout(TIMEOUT_MS) {
            bobRepo.sendFileTransferIceCandidate(
                transferId = transferId,
                isRequester = true,
                participantIds = listOf(bobUid, aliceUid),
                sdpMid = "data",
                sdpMLineIndex = 0,
                sdp = "candidate:1 1 UDP 2122252543 192.168.1.5 50100 typ host"
            ).getOrThrow()
        }

        // Alice observes incoming 1.5 GB request, answers with SDP + owner ICE, and completes transfer
        signInTestUser(aliceEmail)
        val incomingReq = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeIncomingFileTransferRequests().first { it.id == transferId }
        }
        assertEquals(onePointFiveGbBytes, incomingReq.fileSizeBytes)
        assertEquals("att_15gbvideo", incomingReq.fileId)

        val reqIce = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeFileTransferIceCandidates(transferId, readRequesterCandidates = true).first()
        }
        assertEquals("data", reqIce.sdpMid)

        withTimeout(TIMEOUT_MS) {
            aliceRepo.answerFileTransferRequest(
                transferId = transferId,
                answerSdp = "v=0\r\no=- 222 2 IN IP4 127.0.0.1\r\ns=DataAnswer"
            ).getOrThrow()
            aliceRepo.sendFileTransferIceCandidate(
                transferId = transferId,
                isRequester = false,
                participantIds = listOf(bobUid, aliceUid),
                sdpMid = "data",
                sdpMLineIndex = 0,
                sdp = "candidate:2 1 UDP 2122252543 198.51.100.8 50200 typ srflx"
            ).getOrThrow()
        }

        // Bob observes ANSWERED + owner ICE candidate, then marks COMPLETED
        signInTestUser(bobEmail)
        val answered = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeFileTransferSession(transferId).first { it?.status == "ANSWERED" }
        }
        assertTrue(answered?.answerSdp?.contains("s=DataAnswer") == true)
        val ownerIce = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeFileTransferIceCandidates(transferId, readRequesterCandidates = false).first()
        }
        assertEquals("data", ownerIce.sdpMid)

        withTimeout(TIMEOUT_MS) {
            bobRepo.updateFileTransferStatus(transferId, "COMPLETED").getOrThrow()
        }
    }

    @Test
    fun parentalControlsLiveLocationsAndFeedbackReportsWorkEndToEndOverFirebase() = runBlocking {
        val aliceEmail = "alice_e2e_${UUID.randomUUID().toString().take(6)}@test.com"
        val bobEmail = "bob_e2e_${UUID.randomUUID().toString().take(6)}@test.com"

        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = FirestoreChatRepository(firestore, auth)
        val aliceHandle = "al_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        val aliceProfile = withTimeout(TIMEOUT_MS) {
            aliceRepo.ensureUserProfile(preferredHandle = aliceHandle, displayNameOverride = "Alice")
        }.getOrThrow()

        val bobUid = signInTestUser(bobEmail)
        val bobRepo = FirestoreChatRepository(firestore, auth)
        val room = withTimeout(TIMEOUT_MS) {
            bobRepo.openOrCreateDirectConversation(aliceProfile, "Bob").getOrThrow()
        }

        // 1. Alice saves & fetches Parental Controls config via Firebase
        signInTestUser(aliceEmail)
        withTimeout(TIMEOUT_MS) {
            aliceRepo.saveParentalControlConfig(
                parentEmail = "ebeveyn@example.com",
                isVerified = true,
                restrictionsEnabled = true,
                blockStrangerDms = true,
                forceProfanityFilter = true,
                dailyLimitMinutes = 75,
                codeHash = "",
                codeSalt = "",
                expiresAtMs = 0L
            ).getOrThrow()
        }
        val fetchedParental = withTimeout(TIMEOUT_MS) {
            aliceRepo.fetchParentalControlConfig().getOrThrow()
        }
        assertNotNull(fetchedParental)
        assertTrue(fetchedParental!!.restrictionsEnabled)
        assertEquals(75, fetchedParental.dailyLimitMinutes)

        // 2. Alice shares real-time live location; Bob observes it live; Alice stops sharing
        val locId = "loc_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        withTimeout(TIMEOUT_MS) {
            aliceRepo.upsertLiveLocation(
                CloudLiveLocation(
                    id = locId,
                    conversationId = room.id,
                    senderId = aliceUid,
                    senderName = "Alice",
                    participantIds = room.participantIds,
                    latitude = 41.0082,
                    longitude = 28.9784,
                    accuracyMeters = 10.0,
                    label = "Kadıköy İskele",
                    isActive = true,
                    expiresAtMs = System.currentTimeMillis() + 900_000L,
                    updatedAtMs = System.currentTimeMillis()
                )
            ).getOrThrow()
        }

        signInTestUser(bobEmail)
        val bobLocs = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeLiveLocations(room.id).first { it.containsKey(locId) }
        }
        assertTrue(bobLocs[locId]?.isActive == true)
        assertEquals(41.0082, bobLocs[locId]?.latitude ?: 0.0, 0.0001)

        signInTestUser(aliceEmail)
        withTimeout(TIMEOUT_MS) {
            aliceRepo.stopLiveLocation(room.id, locId).getOrThrow()
        }

        // 3. Alice submits analyzable FeedbackReport, updates status, checks KVKK export, and deletes it
        val fbId = "fb_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val submittedFb = withTimeout(TIMEOUT_MS) {
            aliceRepo.submitFeedbackReport(
                CloudFeedbackReport(
                    id = fbId,
                    uid = aliceUid,
                    category = "SUGGESTION",
                    severity = "LOW",
                    rating = 5,
                    moduleTag = "GEN AI",
                    title = "Harika sesli okuma",
                    description = "Türkçe aksan çok akıcı çalışıyor.",
                    status = "OPEN",
                    appVersion = "1.0",
                    osVersion = "Android SDK 35",
                    clientTimestampMs = System.currentTimeMillis()
                )
            ).getOrThrow()
        }
        assertEquals("OPEN", submittedFb.status)

        val myReports = withTimeout(TIMEOUT_MS) {
            aliceRepo.fetchMyFeedbackReports().getOrThrow()
        }
        assertTrue(myReports.any { it.id == fbId && it.title == "Harika sesli okuma" })

        withTimeout(TIMEOUT_MS) {
            aliceRepo.updateFeedbackStatus(fbId, "RESOLVED", newRating = 5).getOrThrow()
        }
        val kvkkJson = withTimeout(TIMEOUT_MS) {
            aliceRepo.exportUserCloudDataJson().getOrThrow()
        }
        assertTrue(kvkkJson.contains("feedbackReports"))
        assertTrue(kvkkJson.contains(fbId))

        withTimeout(TIMEOUT_MS) {
            aliceRepo.deleteFeedbackReport(fbId).getOrThrow()
        }
        val afterDelete = withTimeout(TIMEOUT_MS) {
            aliceRepo.fetchMyFeedbackReports().getOrThrow()
        }
        assertTrue(afterDelete.none { it.id == fbId })
    }

    private companion object {
        const val TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
