package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) LIKE LOWER('%' || :query || '%') OR LOWER(fullName) LIKE LOWER('%' || :query || '%') LIMIT 30")
    suspend fun searchUsers(query: String): List<UserEntity>

    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT 50")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET passwordHash = :passwordHash, salt = :salt WHERE LOWER(email) = LOWER(:email)")
    suspend fun updatePassword(email: String, passwordHash: String, salt: String): Int

    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE id = :id")
    suspend fun updateLastLogin(id: Long, timestamp: Long)

    @Query("UPDATE users SET fullName = :fullName WHERE id = :id")
    suspend fun updateUserFullName(id: Long, fullName: String): Int

    @Insert
    suspend fun insertResetCode(code: PasswordResetCode): Long

    @Query("SELECT * FROM password_reset_codes WHERE LOWER(email) = LOWER(:email) AND code = :code AND isUsed = 0 AND expiresAt > :currentTime ORDER BY id DESC LIMIT 1")
    suspend fun getValidResetCode(email: String, code: String, currentTime: Long): PasswordResetCode?

    @Query("UPDATE password_reset_codes SET isUsed = 1 WHERE id = :id")
    suspend fun markResetCodeAsUsed(id: Long)

    @Query("UPDATE password_reset_codes SET isUsed = 1 WHERE LOWER(email) = LOWER(:email)")
    suspend fun invalidateAllResetCodesForEmail(email: String)
}
