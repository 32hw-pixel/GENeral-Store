package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val userDao: UserDao) {

    suspend fun seedDemoUserIfEmpty() = withContext(Dispatchers.IO) {
        // Yapay/sahte kişi profilleri kaldırıldı. Rehber yalnızca kullanıcı tarafından oluşturulan veya eklenen gerçek profilleri içerir.
    }

    suspend fun signUp(
        username: String,
        fullName: String,
        email: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().removePrefix("@").lowercase()
        val trimmedEmail = email.trim()
        val trimmedName = fullName.trim()

        if (cleanUsername.length < 3 || cleanUsername.length > 20 || !cleanUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return@withContext Result.failure(
                IllegalArgumentException("Kullanıcı adı 3-20 karakter uzunluğunda olmalı ve sadece harf, rakam ve alt çizgi (_) içermelidir.")
            )
        }

        if (trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Lütfen adınızı ve soyadınızı girin."))
        }

        val effectiveEmail = if (trimmedEmail.isBlank()) {
            "${cleanUsername}@general.local"
        } else {
            trimmedEmail
        }

        if (effectiveEmail != "${cleanUsername}@general.local" && !SecurityUtils.isValidEmail(effectiveEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Lütfen geçerli bir e-posta adresi girin veya alanı boş bırakın."))
        }

        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Şifre en az 6 karakter olmalıdır."))
        }

        if (effectiveEmail != "${cleanUsername}@general.local") {
            val existingByEmail = userDao.getUserByEmail(effectiveEmail)
            if (existingByEmail != null) {
                return@withContext Result.failure(IllegalStateException("Bu e-posta adresiyle kayıtlı bir hesap zaten var."))
            }
        }

        val existingByUsername = userDao.getUserByUsername(cleanUsername)
        if (existingByUsername != null) {
            return@withContext Result.failure(IllegalStateException("Bu kullanıcı adı (@$cleanUsername) daha önce alınmış. Lütfen başka bir kullanıcı adı seçin."))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val newUser = UserEntity(
            username = cleanUsername,
            fullName = trimmedName,
            email = trimmedEmail,
            passwordHash = passwordHash,
            salt = salt,
            bio = "GENeral Chat Kullanıcısı",
            avatarEmoji = "🛡️",
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )

        val insertedId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = insertedId)
        Result.success(createdUser)
    }

    suspend fun signUp(fullName: String, email: String, password: String): Result<UserEntity> {
        val defaultUsername = email.substringBefore("@").replace(Regex("[^a-zA-Z0-9_]"), "_").take(15)
        return signUp(defaultUsername, fullName, email, password)
    }

    private val failedAttemptsByIdentifier = java.util.concurrent.ConcurrentHashMap<String, Pair<Int, Long>>()

    suspend fun signIn(identifier: String, password: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            val clean = identifier.trim()
            if (clean.isBlank() || password.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Lütfen e-posta / kullanıcı adınızı ve şifrenizi girin."))
            }

            val rateKey = clean.lowercase()
            val nowMs = System.currentTimeMillis()
            val prevState = failedAttemptsByIdentifier[rateKey]
            if (prevState != null && prevState.second > nowMs) {
                val waitSec = ((prevState.second - nowMs) / 1000L).coerceAtLeast(1L)
                return@withContext Result.failure(IllegalStateException("Çok fazla hatalı giriş denemesi. Lütfen $waitSec saniye bekleyin."))
            }

            // Check if identifier is username or email
            val cleanUsername = clean.removePrefix("@").lowercase()
            val user = userDao.getUserByEmail(clean)
                ?: userDao.getUserByUsername(cleanUsername)
                ?: run {
                    recordFailedAttempt(rateKey, nowMs)
                    return@withContext Result.failure(IllegalArgumentException("Girdiğiniz e-posta veya kullanıcı adına ait bir hesap bulunamadı."))
                }

            val isValid = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
            if (!isValid) {
                val remaining = recordFailedAttempt(rateKey, nowMs)
                val msg = if (remaining <= 0) {
                    "5 kez hatalı şifre girildi. Güvenlik nedeniyle 30 saniye kilitlendi."
                } else {
                    "Hatalı şifre girdiniz. Kalan deneme hakkı: $remaining"
                }
                return@withContext Result.failure(IllegalArgumentException(msg))
            }

            failedAttemptsByIdentifier.remove(rateKey)
            userDao.updateLastLogin(user.id, nowMs)
            Result.success(user.copy(lastLoginAt = nowMs))
        }

    private fun recordFailedAttempt(rateKey: String, nowMs: Long): Int {
        val current = failedAttemptsByIdentifier[rateKey]
        val baseCount = if (current != null && current.second <= nowMs) current.first else 0
        val nextCount = baseCount + 1
        return if (nextCount >= 5) {
            failedAttemptsByIdentifier[rateKey] = 0 to (nowMs + 30_000L)
            0
        } else {
            failedAttemptsByIdentifier[rateKey] = nextCount to 0L
            5 - nextCount
        }
    }

    suspend fun searchUsers(query: String): List<UserEntity> = withContext(Dispatchers.IO) {
        val clean = query.trim().removePrefix("@")
        if (clean.isBlank()) {
            userDao.getAllUsers()
        } else {
            userDao.searchUsers(clean)
        }
    }

    suspend fun getUserByUsername(username: String): UserEntity? = withContext(Dispatchers.IO) {
        val clean = username.trim().removePrefix("@").lowercase()
        userDao.getUserByUsername(clean)
    }

    suspend fun requestPasswordReset(email: String): Result<String> =
        withContext(Dispatchers.IO) {
            val trimmedEmail = email.trim()

            if (!SecurityUtils.isValidEmail(trimmedEmail)) {
                return@withContext Result.failure(IllegalArgumentException("Lütfen geçerli bir e-posta adresi girin."))
            }

            val user = userDao.getUserByEmail(trimmedEmail)
                ?: return@withContext Result.failure(IllegalArgumentException("Bu e-posta adresiyle kayıtlı bir kullanıcı bulunamadı."))

            userDao.invalidateAllResetCodesForEmail(user.email)

            val resetCode = SecurityUtils.generateResetCode()
            val resetEntry = PasswordResetCode(
                email = user.email,
                code = resetCode,
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + (15 * 60 * 1000L), // 15 mins
                isUsed = false
            )
            userDao.insertResetCode(resetEntry)

            Result.success(resetCode)
        }

    suspend fun verifyResetCode(email: String, code: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val trimmedEmail = email.trim()
            val trimmedCode = code.trim()

            if (trimmedCode.length != 6) {
                return@withContext Result.failure(IllegalArgumentException("Lütfen 6 haneli doğrulama kodunu eksiksiz girin."))
            }

            val validCode = userDao.getValidResetCode(trimmedEmail, trimmedCode, System.currentTimeMillis())
            if (validCode == null) {
                return@withContext Result.failure(IllegalArgumentException("Geçersiz veya süresi dolmuş doğrulama kodu. Lütfen tekrar deneyin."))
            }

            Result.success(true)
        }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val trimmedEmail = email.trim()
            val trimmedCode = code.trim()

            if (newPassword.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("Yeni şifre en az 6 karakter olmalıdır."))
            }

            val validCode = userDao.getValidResetCode(trimmedEmail, trimmedCode, System.currentTimeMillis())
                ?: return@withContext Result.failure(IllegalArgumentException("Doğrulama kodunun süresi dolmuş. Lütfen yeni bir kod isteyin."))

            val newSalt = SecurityUtils.generateSalt()
            val newHash = SecurityUtils.hashPassword(newPassword, newSalt)

            val rowsUpdated = userDao.updatePassword(trimmedEmail, newHash, newSalt)
            if (rowsUpdated > 0) {
                userDao.markResetCodeAsUsed(validCode.id)
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Şifre güncellenirken bir hata oluştu."))
            }
        }

    suspend fun changePassword(userId: Long, currentPassword: String, newPassword: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            if (newPassword.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("Yeni şifre en az 6 karakter olmalıdır."))
            }

            val user = userDao.getUserByIdSync(userId)
                ?: return@withContext Result.failure(IllegalArgumentException("Kullanıcı bulunamadı."))

            val isCurrentValid = SecurityUtils.verifyPassword(currentPassword, user.salt, user.passwordHash)
            if (!isCurrentValid) {
                return@withContext Result.failure(IllegalArgumentException("Mevcut şifreniz hatalı."))
            }

            val newSalt = SecurityUtils.generateSalt()
            val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
            val updated = userDao.updatePassword(user.email, newHash, newSalt)
            if (updated > 0) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Şifre güncellenemedi."))
            }
        }

    suspend fun updateUserFullName(userId: Long, newFullName: String): Result<String> =
        withContext(Dispatchers.IO) {
            val clean = newFullName.trim().take(80)
            if (clean.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Görünen ad boş olamaz."))
            }
            val rows = userDao.updateUserFullName(userId, clean)
            if (rows > 0) {
                Result.success(clean)
            } else {
                Result.failure(IllegalStateException("Yerel profil güncellenemedi."))
            }
        }
}
