package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goals")
data class DailyFocusGoalEntity(
    @PrimaryKey
    val dateString: String, // format: "YYYY-MM-DD"
    val completedMinutes: Int,
    val goalMinutes: Int,
    val isGoalMet: Boolean
)
