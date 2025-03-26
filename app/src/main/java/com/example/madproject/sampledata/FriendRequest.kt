package com.example.madproject.sampledata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend_requests")
data class FriendRequest(
    @PrimaryKey
    val requestId: String,
    val senderId: String,       // User who sent the request
    val senderUsername: String, // Username of sender (for display)
    val receiverId: String,     // User who receives the request
    val status: String,         // "PENDING", "ACCEPTED", "REJECTED"
    val message: String,        // Optional message with the request
    val timestamp: Long         // When the request was sent
)