package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_reset_codes")
data class PasswordResetCode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val code: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (15 * 60 * 1000), // 15 minutes validity
    val isUsed: Boolean = false
)
