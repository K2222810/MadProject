package com.example.madproject.sampledata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey
    val alertId: String,
    val userId: String,        // Who sent the alert
    val username: String,      // Username of sender (for display)
    val message: String,       // Alert message
    val location: String,      // Location description or coordinates
    val timestamp: Long,       // When the alert was sent
    val isActive: Boolean = true  // Whether the alert is still active
)