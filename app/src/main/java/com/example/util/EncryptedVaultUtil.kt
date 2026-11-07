package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Base64
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class VaultExportResult(
    val file: File,
    val previewEncryptedText: String,
    val messageCount: Int,
    val timestamp: String,
    val checksum: String
)

object EncryptedVaultUtil {

    private fun deriveKey(passphrase: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(passphrase.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptTranscript(
        context: Context,
        chatTitle: String,
        messages: List<Pair<String, String>>, // sender, text
        passphrase: String = "GENERAL_CHAT_E2EE_VAULT_KEY"
    ): VaultExportResult {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val nowStr = dateFormat.format(Date())

        val rawBuilder = StringBuilder()
        rawBuilder.append("=== GENERAL CHAT DECRYPTED TRANSCRIPT ===\n")
        rawBuilder.append("Başlık: $chatTitle\n")
        rawBuilder.append("Tarih: $nowStr\n")
        rawBuilder.append("Mesaj Sayısı: ${messages.size}\n")
        rawBuilder.append("=========================================\n\n")

        messages.forEachIndexed { index, (sender, text) ->
            rawBuilder.append("[${index + 1}] $sender: $text\n")
        }

        val rawText = rawBuilder.toString()
        val keySpec = deriveKey(passphrase)

        val ivBytes = ByteArray(16)
        SecureRandom().nextBytes(ivBytes)
        val ivSpec = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encryptedBytes = cipher.doFinal(rawText.toByteArray(Charsets.UTF_8))

        val base64Cipher = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val base64Iv = Base64.encodeToString(ivBytes, Base64.NO_WRAP)

        val sha256Digest = MessageDigest.getInstance("SHA-256")
        val checksumBytes = sha256Digest.digest(encryptedBytes)
        val checksumHex = checksumBytes.joinToString("") { "%02x".format(it) }

        val vaultPayload = buildString {
            append("----- BEGIN GENERAL CHAT ENCRYPTED VAULT (AES-256) -----\n")
            append("PROTOCOL: GENERAL_CHAT_v7.7.3_E2EE\n")
            append("TIMESTAMP: $nowStr\n")
            append("CONVERSATION: $chatTitle\n")
            append("MESSAGE_COUNT: ${messages.size}\n")
            append("IV: $base64Iv\n")
            append("CHECKSUM_SHA256: $checksumHex\n")
            append("PAYLOAD:\n")
            append(base64Cipher)
            append("\n----- END GENERAL CHAT ENCRYPTED VAULT -----\n")
        }

        val vaultDir = File(context.filesDir, "vault").apply { mkdirs() }
        val fileName = "general_chat_vault_${System.currentTimeMillis()}.enc.txt"
        val vaultFile = File(vaultDir, fileName).apply {
            writeText(vaultPayload)
        }

        return VaultExportResult(
            file = vaultFile,
            previewEncryptedText = vaultPayload,
            messageCount = messages.size,
            timestamp = nowStr,
            checksum = checksumHex.take(16) + "..."
        )
    }

    fun shareEncryptedFile(context: Context, file: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "GENeral Chat Şifrelenmiş Sohbet Kaydı (.enc.txt)")
                putExtra(Intent.EXTRA_TEXT, file.readText())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Şifreli Sohbet Kaydını Dışa Aktar"))
        } catch (_: Exception) {}
    }
}
