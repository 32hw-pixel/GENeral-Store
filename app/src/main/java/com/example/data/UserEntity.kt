package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String = "",
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val bio: String = "GENeral Chat Siber Ağ Üyesi",
    val avatarEmoji: String = "🛡️",
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)
