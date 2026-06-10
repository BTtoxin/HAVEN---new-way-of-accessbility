package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val triggerType: String,
    val actionCount: Int,
    val enabled: Boolean,
    val actions: String = "[]" // JSON string of actions for the sequence
)
