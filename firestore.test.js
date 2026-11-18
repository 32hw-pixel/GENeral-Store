const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const assert = require("node:assert/strict");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_001";
const BOB_UID = "bob_uid_002";
const EVE_UID = "eve_uid_003";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTs() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- 1. Unauthenticated access must fail across every collection ---
test("Unauthenticated caller cannot read or write users, handle_index, conversations, messages, or calls", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("handle_index").doc("alice_01").get());
  await assertFails(unauthDb.collection("conversations").where("participantIds", "array-contains", ALICE_UID).get());
  await assertFails(
    unauthDb.collection("conversations").doc("dm_alice_bob").collection("messages").get()
  );
  await assertFails(unauthDb.collection("calls").where("participantIds", "array-contains", ALICE_UID).get());
});

// --- 2. Private User PII Isolation ---
test("User can create and read own /users/{uid}, but cannot read another user's private PII profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const ts = pastTs();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      handle: "alice_cyber",
      displayName: "Alice",
      email: "alice@gmail.com",
      avatarEmoji: "⚡",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").where("uid", "==", ALICE_UID).get());
});

// --- 3. Public Handle Index (exact lookup allowed, list scraping blocked, hijacking blocked) ---
test("Signed-in user can create handle_index entry and another user can lookup by handle, but list is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const ts = pastTs();
  await assertSucceeds(
    aliceDb.collection("handle_index").doc("alice_cyber").set({
      uid: ALICE_UID,
      handle: "alice_cyber",
      displayName: "Alice",
      avatarEmoji: "⚡",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(bobDb.collection("handle_index").doc("alice_cyber").get());
  await assertFails(bobDb.collection("handle_index").get());
  await assertFails(
    bobDb.collection("handle_index").doc("alice_cyber").set({
      uid: BOB_UID,
      handle: "alice_cyber",
      displayName: "Hijack",
      avatarEmoji: "💀",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

// --- 4. Shadow Update & Ghost Field Injection Guard ---
test("Shadow update with extra ghost field or PII leak in handle_index is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTs();

  await assertFails(
    aliceDb.collection("handle_index").doc("alice_ghost").set({
      uid: ALICE_UID,
      handle: "alice_ghost",
      displayName: "Alice",
      avatarEmoji: "⚡",
      isVerifiedAdmin: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

// --- 5. Two-Device Messaging + Read Receipts + Delete-for-Everyone + Chunked File + Eve Isolation ---
test("Alice and Bob exchange messages, mark read receipts, delete for everyone, transfer chunks, while Eve is blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  const convId = `dm_${ALICE_UID}_${BOB_UID}`;
  const ts = pastTs();

  const preCheckSnap = await assertSucceeds(aliceDb.collection("conversations").doc(convId).get());
  assert.equal(preCheckSnap.exists, false);

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      id: convId,
      title: "Alice & Bob",
      isGroup: false,
      createdBy: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: { [ALICE_UID]: "Alice", [BOB_UID]: "Bob" },
      lastMessage: "Merhaba Bob!",
      lastSenderId: ALICE_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Alice sends a message with HH:mm time and attachment metadata
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").set({
      id: "msg_1",
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      participantIds: [ALICE_UID, BOB_UID],
      text: "[DOCUMENT] rapor.pdf",
      time: "14:30",
      clientTimestampMs: 1700000001000,
      isVoiceNote: false,
      voiceDurationSeconds: 0,
      attachmentType: "DOCUMENT",
      attachmentName: "rapor.pdf",
      attachmentSizeBytes: 524288000,
      transferMode: "CLOUD_CHUNKS",
      chunkCount: 1,
      isDeletedForEveryone: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Alice writes chunk 0
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_1")
      .collection("chunks").doc("chunk_0").set({
        index: 0,
        totalChunks: 1,
        senderId: ALICE_UID,
        participantIds: [ALICE_UID, BOB_UID],
        dataBase64: "SGVsbG8=",
        createdAt: ts,
      })
  );

  // Bob marks Alice's message as read (allowed!), cannot tamper with text (denied!)
  await assertFails(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").update({
      text: "Hacked!",
      readBy: [BOB_UID],
      readAtMs: 1700000005000,
      updatedAt: pastTs(),
    })
  );
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").update({
      readBy: [BOB_UID],
      readAtMs: 1700000005000,
      deliveredAtMs: 1700000002000,
      updatedAt: pastTs(),
    })
  );

  // Bob cannot delete Alice's message for everyone, ONLY Alice can
  await assertFails(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").update({
      text: "🚫 Bu mesaj herkes için silindi.",
      isDeletedForEveryone: true,
      updatedAt: pastTs(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").update({
      text: "🚫 Bu mesaj herkes için silindi.",
      isDeletedForEveryone: true,
      updatedAt: pastTs(),
    })
  );

  // Eve is totally blocked
  await assertFails(eveDb.collection("conversations").doc(convId).get());
  await assertFails(eveDb.collection("conversations").doc(convId).collection("messages").doc("msg_1").get());
  await assertFails(
    eveDb.collection("conversations").doc(convId).collection("messages").doc("msg_1")
      .collection("chunks").doc("chunk_0").get()
  );
});

// --- 6. WebRTC Call Signaling Rules (Alice calls Bob, Eve blocked) ---
test("WebRTC call offer, answer, and ICE candidates succeed for participants and fail for outsiders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  const callId = "call_alice_bob_01";
  const ts = pastTs();

  await assertSucceeds(
    aliceDb.collection("calls").doc(callId).set({
      id: callId,
      callerId: ALICE_UID,
      callerName: "Alice",
      calleeId: BOB_UID,
      calleeName: "Bob",
      participantIds: [ALICE_UID, BOB_UID],
      isVideo: true,
      status: "RINGING",
      offerSdp: "v=0\r\no=- 123 2 IN IP4 127.0.0.1\r\n",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Alice adds caller ICE candidate
  await assertSucceeds(
    aliceDb.collection("calls").doc(callId).collection("callerCandidates").doc("ice_0").set({
      senderId: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      sdpMid: "0",
      sdpMLineIndex: 0,
      sdp: "candidate:1 1 UDP 2122252543 192.168.1.2 50000 typ host",
      createdAt: ts,
    })
  );

  // Bob observes ringing call and answers
  const bobCalls = await assertSucceeds(
    bobDb.collection("calls").where("participantIds", "array-contains", BOB_UID).get()
  );
  assert.equal(bobCalls.size, 1);

  await assertSucceeds(
    bobDb.collection("calls").doc(callId).update({
      status: "ANSWERED",
      answerSdp: "v=0\r\no=- 456 2 IN IP4 127.0.0.1\r\n",
      updatedAt: pastTs(),
    })
  );

  await assertSucceeds(
    bobDb.collection("calls").doc(callId).collection("calleeCandidates").doc("ice_1").set({
      senderId: BOB_UID,
      participantIds: [ALICE_UID, BOB_UID],
      sdpMid: "0",
      sdpMLineIndex: 0,
      sdp: "candidate:2 1 UDP 2122252543 192.168.1.3 50001 typ host",
      createdAt: ts,
    })
  );

  // Eve cannot view or tamper with the call
  await assertFails(eveDb.collection("calls").doc(callId).get());
  await assertFails(
    eveDb.collection("calls").doc(callId).update({ status: "ENDED", updatedAt: pastTs() })
  );
});

// --- 7. Linked Devices Companion Pairing (/device_links) ---
test("Linked devices pairing doc is strictly isolated to its authenticated owner", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const code = "GC-7XK9M2";
  const ts = pastTs();

  await assertFails(unauthDb.collection("device_links").doc(code).get());

  await assertSucceeds(
    aliceDb.collection("device_links").doc(code).set({
      code,
      ownerUid: ALICE_UID,
      status: "WAITING",
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("device_links").doc(code).set(
      {
        code,
        ownerUid: ALICE_UID,
        status: "PAIRED",
        deviceName: "MacBook Safari",
        platform: "Safari / WebKit",
        updatedAt: pastTs(),
      },
      { merge: true }
    )
  );

  await assertSucceeds(aliceDb.collection("device_links").doc(code).get());
  await assertFails(bobDb.collection("device_links").doc(code).get());
  await assertFails(
    bobDb.collection("device_links").doc(code).set(
      {
        code,
        ownerUid: ALICE_UID,
        status: "PAIRED",
        updatedAt: pastTs(),
      },
      { merge: true }
    )
  );
});

// --- 8. Age-Gated Parental Controls (/parent_links) ---
test("Parental control link allows child telemetry sync, parent claim & limit update, and blocks outsiders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const childDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const parentDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  const linkCode = "EBN-4P9K2";
  const ts = pastTs();

  await assertFails(unauthDb.collection("parent_links").doc(linkCode).get());

  // Child syncs minor telemetry snapshot
  await assertSucceeds(
    childDb.collection("parent_links").doc(linkCode).set(
      {
        linkCode,
        childUid: ALICE_UID,
        childAge: 13,
        dailyLimitMinutes: 60,
        usedMinutesToday: 12,
        contactsJson: "[]",
        screensJson: "[]",
        updatedAt: ts,
      },
      { merge: true }
    )
  );

  // Parent claims the unclaimed parentUid slot with the exact pairing code
  await assertSucceeds(
    parentDb.collection("parent_links").doc(linkCode).set(
      {
        linkCode,
        parentUid: BOB_UID,
        updatedAt: pastTs(),
      },
      { merge: true }
    )
  );

  // Parent reads supervised child snapshot & updates daily limit
  await assertSucceeds(parentDb.collection("parent_links").doc(linkCode).get());
  await assertSucceeds(
    parentDb.collection("parent_links").doc(linkCode).set(
      {
        parentUid: BOB_UID,
        dailyLimitMinutes: 90,
        updatedAt: pastTs(),
      },
      { merge: true }
    )
  );

  // Eve cannot read, list, hijack parentUid, or modify limits
  await assertFails(eveDb.collection("parent_links").doc(linkCode).get());
  await assertFails(eveDb.collection("parent_links").get());
  await assertFails(
    eveDb.collection("parent_links").doc(linkCode).set(
      {
        linkCode,
        parentUid: EVE_UID,
        updatedAt: pastTs(),
      },
      { merge: true }
    )
  );
  await assertFails(
    eveDb.collection("parent_links").doc(linkCode).set(
      {
        dailyLimitMinutes: 15,
        updatedAt: pastTs(),
      },
      { merge: true }
    )
  );
});

// --- 9. User Blocking, Safety Reports & KVKK Article 11 Requests ---
test("Users can block/unblock contacts, submit abuse reports, and file KVKK requests with strict owner isolation", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTs();

  await assertFails(unauthDb.collection("user_reports").doc("rep_unauth").get());
  await assertFails(unauthDb.collection("kvkk_requests").doc("kvkk_unauth").get());

  // Alice blocks Bob; cannot block herself; Bob cannot read/edit Alice's blocklist
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("blocked").doc(ALICE_UID).set({
      blockedUid: ALICE_UID,
      displayName: "Self",
      createdAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("blocked").doc(BOB_UID).set({
      blockedUid: BOB_UID,
      displayName: "Bob",
      handle: "bob_01",
      createdAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("blocked").doc(BOB_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("blocked").doc(BOB_UID).get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("blocked").doc(BOB_UID).delete());

  // Alice reports Bob; Bob cannot read or spoof reporterUid
  await assertFails(
    bobDb.collection("user_reports").doc("rep_spoof").set({
      id: "rep_spoof",
      reporterUid: ALICE_UID,
      targetUid: EVE_UID,
      reason: "Spoof",
      createdAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("user_reports").doc("rep_01").set({
      id: "rep_01",
      reporterUid: ALICE_UID,
      targetUid: BOB_UID,
      targetName: "Bob",
      conversationId: "dm_alice_bob",
      reason: "Spam veya İstenmeyen Mesajlar",
      details: "Rahatsız edici mesajlar",
      createdAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("user_reports").doc("rep_01").get());
  await assertFails(bobDb.collection("user_reports").doc("rep_01").get());

  // Alice files KVKK Article 11 request; invalid type fails; Bob cannot read Alice's request
  await assertFails(
    aliceDb.collection("kvkk_requests").doc("kvkk_bad").set({
      id: "kvkk_bad",
      uid: ALICE_UID,
      requestType: "INVALID_TYPE",
      details: "Test",
      status: "SUBMITTED",
      createdAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("kvkk_requests").doc("kvkk_01").set({
      id: "kvkk_01",
      uid: ALICE_UID,
      requestType: "ACCESS",
      details: "Madde 11 kapsamında veri erişim talebi",
      contactEmail: "alice@example.com",
      status: "SUBMITTED",
      createdAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("kvkk_requests").doc("kvkk_01").get());
  await assertFails(bobDb.collection("kvkk_requests").doc("kvkk_01").get());
  await assertSucceeds(aliceDb.collection("kvkk_requests").doc("kvkk_01").delete());
});

// --- 10. 1.5 GB+ WebRTC File Transfer Signaling (/file_transfers) & Upgraded 600KB Chunks ---
test("Large 1.5 GB+ file transfer signaling and upgraded chunk rules work for participants and block outsiders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  const convId = `dm_${ALICE_UID}_${BOB_UID}`;
  const transferId = "xfer_alice_bob_15gb";
  const ts = pastTs();
  const onePointFiveGbBytes = 1610612736; // 1.5 GiB

  await assertFails(unauthDb.collection("file_transfers").doc(transferId).get());

  // Setup conversation + 1.5 GB P2P message + 800 KB Base64 chunk allowance check
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      id: convId,
      title: "Alice & Bob",
      isGroup: false,
      createdBy: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: { [ALICE_UID]: "Alice", [BOB_UID]: "Bob" },
      lastMessage: "[VIDEO] tatil_4k.mp4 (1.50 GB)",
      lastSenderId: ALICE_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_big").set({
      id: "msg_big",
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      participantIds: [ALICE_UID, BOB_UID],
      text: "[VIDEO] tatil_4k.mp4 (1.50 GB)",
      time: "15:10",
      clientTimestampMs: 1700000001000,
      isVoiceNote: false,
      voiceDurationSeconds: 0,
      attachmentType: "VIDEO",
      attachmentName: "tatil_4k.mp4",
      attachmentSizeBytes: onePointFiveGbBytes,
      transferMode: "P2P_WEBRTC",
      chunkCount: 0,
      isDeletedForEveryone: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Upgraded 600KB raw (~800KB Base64) chunk allowed up to 920000 chars, oversized rejected
  const largeBase64 = "A".repeat(820000);
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_big")
      .collection("chunks").doc("chunk_0").set({
        index: 0,
        totalChunks: 1,
        senderId: ALICE_UID,
        participantIds: [ALICE_UID, BOB_UID],
        dataBase64: largeBase64,
        createdAt: ts,
      })
  );
  await assertFails(
    aliceDb.collection("conversations").doc(convId).collection("messages").doc("msg_big")
      .collection("chunks").doc("chunk_oversized").set({
        index: 1,
        totalChunks: 2,
        senderId: ALICE_UID,
        participantIds: [ALICE_UID, BOB_UID],
        dataBase64: "A".repeat(950000),
        createdAt: ts,
      })
  );

  // Bob requests WebRTC DataChannel stream from Alice (owner)
  await assertSucceeds(
    bobDb.collection("file_transfers").doc(transferId).set({
      id: transferId,
      requesterUid: BOB_UID,
      ownerUid: ALICE_UID,
      participantIds: [BOB_UID, ALICE_UID],
      conversationId: convId,
      messageId: "msg_big",
      fileId: "att_bigvideo01",
      fileName: "tatil_4k.mp4",
      fileSizeBytes: onePointFiveGbBytes,
      status: "REQUESTED",
      offerSdp: "v=0\r\no=- 111 2 IN IP4 127.0.0.1\r\n",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    bobDb.collection("file_transfers").doc(transferId).collection("requesterCandidates").doc("ice_req_0").set({
      senderId: BOB_UID,
      participantIds: [BOB_UID, ALICE_UID],
      sdpMid: "data",
      sdpMLineIndex: 0,
      sdp: "candidate:1 1 UDP 2122252543 192.168.1.10 50000 typ host",
      createdAt: ts,
    })
  );

  // Alice answers and adds owner ICE candidate
  await assertSucceeds(
    aliceDb.collection("file_transfers").doc(transferId).update({
      status: "ANSWERED",
      answerSdp: "v=0\r\no=- 222 2 IN IP4 127.0.0.1\r\n",
      updatedAt: pastTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("file_transfers").doc(transferId).collection("ownerCandidates").doc("ice_own_0").set({
      senderId: ALICE_UID,
      participantIds: [BOB_UID, ALICE_UID],
      sdpMid: "data",
      sdpMLineIndex: 0,
      sdp: "candidate:2 1 UDP 2122252543 198.51.100.4 51000 typ srflx",
      createdAt: ts,
    })
  );

  // Eve is blocked from reading or hijacking transfer session or ICE
  await assertFails(eveDb.collection("file_transfers").doc(transferId).get());
  await assertFails(
    eveDb.collection("file_transfers").doc(transferId).update({ status: "FAILED", updatedAt: pastTs() })
  );
  await assertFails(
    eveDb.collection("file_transfers").doc(transferId).collection("ownerCandidates").doc("ice_spoof").set({
      senderId: EVE_UID,
      participantIds: [BOB_UID, ALICE_UID],
      sdpMid: "data",
      sdpMLineIndex: 0,
      sdp: "candidate:9 1 UDP 1 1.2.3.4 9999 typ host",
      createdAt: ts,
    })
  );
});

// --- 14. Parental Controls Subcollection (/users/{uid}/parental_controls/config) ---
test("Owner can manage Firebase parental_controls verification & restrictions, while outsiders are denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = pastTs();

  const refAlice = aliceDb.collection("users").doc(ALICE_UID).collection("parental_controls").doc("config");
  const refAsBob = bobDb.collection("users").doc(ALICE_UID).collection("parental_controls").doc("config");

  await assertSucceeds(
    refAlice.set({
      uid: ALICE_UID,
      parentEmail: "ebeveyn@example.com",
      isVerified: false,
      restrictionsEnabled: false,
      blockStrangerDms: true,
      forceProfanityFilter: true,
      dailyLimitMinutes: 60,
      codeHash: "abc123salt_hash",
      codeSalt: "salt_val",
      expiresAtMs: Date.now() + 600000,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Verification completes and activates restrictions
  await assertSucceeds(
    refAlice.update({
      isVerified: true,
      restrictionsEnabled: true,
      codeHash: "",
      codeSalt: "",
      expiresAtMs: 0,
      updatedAt: pastTs(),
    })
  );

  // Reading back own config succeeds
  await assertSucceeds(refAlice.get());

  // Outsider / unauthenticated cannot read, update, or tamper
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("parental_controls").doc("config").get());
  await assertFails(refAsBob.get());
  await assertFails(refAsBob.update({ restrictionsEnabled: false, updatedAt: pastTs() }));

  // UID tampering or invalid daily limit fails
  await assertFails(
    refAlice.update({
      dailyLimitMinutes: 9999,
      updatedAt: pastTs(),
    })
  );
});

// --- 15. Real-Time Live Location Sharing (/conversations/{cid}/live_locations/{locId}) ---
test("Conversation participants can read live locations, only sender can stream/stop, outsiders blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();
  const ts = pastTs();
  const convId = "dm_alice_bob_loc";
  const locId = "loc_alice_1";

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      id: convId,
      title: "Alice & Bob",
      isGroup: false,
      createdBy: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: { [ALICE_UID]: "Alice", [BOB_UID]: "Bob" },
      lastMessage: "Konum açılıyor",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("live_locations").doc(locId).set({
      id: locId,
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      participantIds: [ALICE_UID, BOB_UID],
      latitude: 41.0082,
      longitude: 28.9784,
      accuracyMeters: 12.5,
      label: "Canlı Konumum",
      isActive: true,
      expiresAtMs: Date.now() + 900000,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Bob (participant) can query live locations with participantIds filter
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("live_locations")
      .where("participantIds", "array-contains", BOB_UID)
      .get()
  );

  // Sender Alice updates coordinates & stops sharing
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).collection("live_locations").doc(locId).update({
      latitude: 41.0125,
      longitude: 28.9810,
      accuracyMeters: 8.0,
      isActive: false,
      updatedAt: pastTs(),
    })
  );

  // Peer Bob cannot spoof or overwrite Alice's live location
  await assertFails(
    bobDb.collection("conversations").doc(convId).collection("live_locations").doc(locId).update({
      latitude: 0.0,
      longitude: 0.0,
      updatedAt: pastTs(),
    })
  );

  // Outsider Eve cannot read doc, list all live locations, or inject live location
  await assertFails(
    eveDb.collection("conversations").doc(convId).collection("live_locations").doc(locId).get()
  );
  await assertFails(
    eveDb.collection("conversations").doc(convId).collection("live_locations").get()
  );
  await assertFails(
    eveDb.collection("conversations").doc(convId).collection("live_locations").doc("loc_eve").set({
      id: "loc_eve",
      conversationId: convId,
      senderId: EVE_UID,
      senderName: "Eve",
      participantIds: [ALICE_UID, BOB_UID],
      latitude: 40.0,
      longitude: 29.0,
      accuracyMeters: 5.0,
      label: "Spoof",
      isActive: true,
      expiresAtMs: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Out-of-bounds coordinates rejected
  await assertFails(
    aliceDb.collection("conversations").doc(convId).collection("live_locations").doc("loc_bad_coord").set({
      id: "loc_bad_coord",
      conversationId: convId,
      senderId: ALICE_UID,
      senderName: "Alice",
      participantIds: [ALICE_UID, BOB_UID],
      latitude: 120.0,
      longitude: 28.0,
      accuracyMeters: 5.0,
      label: "Bad",
      isActive: true,
      expiresAtMs: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

// --- 16. Analyzable User Feedback, Suggestions & Bug Reports (/feedback_reports/{reportId}) ---
test("Authenticated users can create, query, update, and KVKK-delete own feedback reports while outsiders are denied", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTs();
  const reportId = "fb_alice_bug_1";

  await assertFails(unauthDb.collection("feedback_reports").doc(reportId).get());

  // Invalid category or rating fails
  await assertFails(
    aliceDb.collection("feedback_reports").doc("fb_bad").set({
      id: "fb_bad",
      uid: ALICE_UID,
      category: "UNKNOWN_CAT",
      severity: "HIGH",
      rating: 6,
      moduleTag: "GEN AI",
      title: "Bad",
      description: "Test",
      status: "OPEN",
      appVersion: "1.0.0",
      osVersion: "Android SDK 35",
      clientTimestampMs: 1700000002000,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid structured feedback report succeeds
  await assertSucceeds(
    aliceDb.collection("feedback_reports").doc(reportId).set({
      id: reportId,
      uid: ALICE_UID,
      category: "BUG",
      severity: "HIGH",
      rating: 4,
      moduleTag: "Sohbet & Mesajlar",
      title: "Uzun video kaydırırken küçük takılma",
      description: "Galeride 4K video kaydırılırken geçici yüklenme görüldü.",
      status: "OPEN",
      appVersion: "1.0.0",
      osVersion: "Android SDK 35",
      clientTimestampMs: 1700000002000,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Owner can list/read and update status
  await assertSucceeds(aliceDb.collection("feedback_reports").where("uid", "==", ALICE_UID).get());
  await assertSucceeds(
    aliceDb.collection("feedback_reports").doc(reportId).update({
      status: "RESOLVED",
      rating: 5,
      updatedAt: pastTs(),
    })
  );

  // Outsider Bob cannot read, list Alice's reports, update, or delete
  await assertFails(bobDb.collection("feedback_reports").doc(reportId).get());
  await assertFails(bobDb.collection("feedback_reports").where("uid", "==", ALICE_UID).get());
  await assertFails(
    bobDb.collection("feedback_reports").doc(reportId).update({
      status: "OPEN",
      updatedAt: pastTs(),
    })
  );
  await assertFails(bobDb.collection("feedback_reports").doc(reportId).delete());

  // Owner KVKK erasure deletes report
  await assertSucceeds(aliceDb.collection("feedback_reports").doc(reportId).delete());
});


