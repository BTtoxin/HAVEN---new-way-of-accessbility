package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val pin: String, // 4-digit security PIN for local authentication
    val nickname: String,
    val avatarColorHex: String = "#FF4500", // customization choice
    val createdAt: Long = System.currentTimeMillis()
)
