package com.example.util

import android.content.Context
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Real End-to-End Encryption (E2EE) Engine for User-to-User Messaging.
 *
 * Protocol:
 * - Identity / Agreement Keypair: ECDH P-256 (secp256r1) generated on-device; private key NEVER leaves device.
 * - Symmetric Cipher: AES-256-GCM (128-bit authentication tag, 96-bit random IV per message).
 * - Server Storage Format: "E2EE:v1:<iv_base64>:<ciphertext_base64>" (server & 3rd parties only see ciphertext).
 */
object E2eeCryptoManager {

    private const val PREFS_KEYS = "general_chat_e2ee_keys"
    private const val PREFS_PEER_CACHE = "general_chat_e2ee_peer_cache"
    private const val KEY_PRIV = "ecdh_private_pkcs8"
    private const val KEY_PUB = "ecdh_public_x509"
    private const val PREFIX = "E2EE:v1:"
    private const val GCM_TAG_BITS = 128
    private const val GCM_IV_BYTES = 12

    private val secureRandom = SecureRandom()

    fun isE2eeCiphertext(text: String): Boolean = text.startsWith(PREFIX)

    @Synchronized
    fun getOrCreateKeyPair(context: Context): KeyPair {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_KEYS, Context.MODE_PRIVATE)
        val savedPriv = prefs.getString(KEY_PRIV, null)
        val savedPub = prefs.getString(KEY_PUB, null)
        if (!savedPriv.isNullOrBlank() && !savedPub.isNullOrBlank()) {
            try {
                val kf = KeyFactory.getInstance("EC")
                val priv = kf.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(savedPriv, Base64.NO_WRAP)))
                val pub = kf.generatePublic(X509EncodedKeySpec(Base64.decode(savedPub, Base64.NO_WRAP)))
                return KeyPair(pub, priv)
            } catch (_: Exception) {
                // Regenerate if stored key material is corrupted
            }
        }
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"), secureRandom)
        val pair = kpg.generateKeyPair()
        prefs.edit()
            .putString(KEY_PRIV, Base64.encodeToString(pair.private.encoded, Base64.NO_WRAP))
            .putString(KEY_PUB, Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP))
            .apply()
        return pair
    }

    fun getOrCreateMyPublicKeyBase64(context: Context): String {
        val pair = getOrCreateKeyPair(context)
        return Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP)
    }

    fun cachePeerPublicKey(context: Context, peerUid: String, publicKeyB64: String) {
        if (peerUid.isBlank() || publicKeyB64.isBlank()) return
        context.applicationContext.getSharedPreferences(PREFS_PEER_CACHE, Context.MODE_PRIVATE)
            .edit()
            .putString("peer_$peerUid", publicKeyB64.trim())
            .apply()
    }

    fun getCachedPeerPublicKey(context: Context, peerUid: String): String? {
        if (peerUid.isBlank()) return null
        return context.applicationContext.getSharedPreferences(PREFS_PEER_CACHE, Context.MODE_PRIVATE)
            .getString("peer_$peerUid", null)
            ?.takeIf { it.isNotBlank() }
    }

    fun getKeyFingerprint(publicKeyB64: String): String {
        if (publicKeyB64.isBlank()) return "Bekleniyor"
        return try {
            val raw = Base64.decode(publicKeyB64, Base64.NO_WRAP)
            val digest = MessageDigest.getInstance("SHA-256").digest(raw)
            digest.take(8).joinToString("") { "%02X".format(it) }
                .chunked(4)
                .joinToString("-")
        } catch (_: Exception) {
            "Doğrulanamadı"
        }
    }

    fun encrypt(
        context: Context,
        conversationId: String,
        plaintext: String,
        peerPublicKeyB64: String
    ): String? {
        if (plaintext.isBlank() || peerPublicKeyB64.isBlank()) return null
        return try {
            val myPair = getOrCreateKeyPair(context)
            val peerPub = decodePublicKey(peerPublicKeyB64)
            encryptWithKeys(myPair.private, peerPub, conversationId, plaintext)
        } catch (_: Exception) {
            null
        }
    }

    fun decrypt(
        context: Context,
        conversationId: String,
        payload: String,
        peerPublicKeyB64: String
    ): String? {
        if (!isE2eeCiphertext(payload) || peerPublicKeyB64.isBlank()) return null
        return try {
            val myPair = getOrCreateKeyPair(context)
            val peerPub = decodePublicKey(peerPublicKeyB64)
            decryptWithKeys(myPair.private, peerPub, conversationId, payload)
        } catch (_: Exception) {
            null
        }
    }

    internal fun encryptWithKeys(
        myPrivateKey: PrivateKey,
        peerPublicKey: PublicKey,
        conversationId: String,
        plaintext: String
    ): String {
        val aesKey = deriveConversationAesKey(myPrivateKey, peerPublicKey, conversationId)
        val iv = ByteArray(GCM_IV_BYTES)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        cipher.updateAAD(conversationId.toByteArray(Charsets.UTF_8))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val ctB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        return "$PREFIX$ivB64:$ctB64"
    }

    internal fun decryptWithKeys(
        myPrivateKey: PrivateKey,
        peerPublicKey: PublicKey,
        conversationId: String,
        payload: String
    ): String? {
        if (!isE2eeCiphertext(payload)) return null
        val body = payload.removePrefix(PREFIX)
        val parts = body.split(":", limit = 2)
        if (parts.size != 2) return null
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
        if (iv.size != GCM_IV_BYTES || ciphertext.isEmpty()) return null
        val aesKey = deriveConversationAesKey(myPrivateKey, peerPublicKey, conversationId)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        cipher.updateAAD(conversationId.toByteArray(Charsets.UTF_8))
        val plainBytes = cipher.doFinal(ciphertext)
        return String(plainBytes, Charsets.UTF_8)
    }

    private fun decodePublicKey(publicKeyB64: String): PublicKey {
        val decoded = Base64.decode(publicKeyB64.trim(), Base64.NO_WRAP)
        return KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(decoded))
    }

    private fun deriveConversationAesKey(
        myPrivateKey: PrivateKey,
        peerPublicKey: PublicKey,
        conversationId: String
    ): SecretKey {
        val agreement = KeyAgreement.getInstance("ECDH")
        agreement.init(myPrivateKey)
        agreement.doPhase(peerPublicKey, true)
        val sharedSecret = agreement.generateSecret()

        val md = MessageDigest.getInstance("SHA-256")
        md.update(sharedSecret)
        md.update(":general_e2ee_v1:".toByteArray(Charsets.UTF_8))
        md.update(conversationId.toByteArray(Charsets.UTF_8))
        val keyBytes = md.digest()
        return SecretKeySpec(keyBytes, "AES")
    }

    fun clearAll(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS_KEYS, Context.MODE_PRIVATE).edit().clear().apply()
        context.applicationContext.getSharedPreferences(PREFS_PEER_CACHE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
