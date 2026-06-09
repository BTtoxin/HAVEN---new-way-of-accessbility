package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_preferences",
    indices = [Index(value = ["userId", "prefKey"], unique = true)]
)
data class UserPreferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // matches user ID (-1 for Guest session)
    val prefKey: String,
    val prefValue: String,
    val updatedAt: Long = System.currentTimeMillis()
)
