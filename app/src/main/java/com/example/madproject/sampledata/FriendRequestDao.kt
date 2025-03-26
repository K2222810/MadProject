package com.example.madproject.sampledata

import androidx.room.*

@Dao
interface FriendRequestDao {
    @Query("SELECT * FROM friend_requests")
    suspend fun getAllFriendRequests(): List<FriendRequest>

    @Query("SELECT * FROM friend_requests WHERE receiverId = :userId AND status = 'PENDING'")
    suspend fun getPendingRequestsForUser(userId: String): List<FriendRequest>

    @Query("SELECT * FROM friend_requests WHERE senderId = :userId")
    suspend fun getSentRequestsForUser(userId: String): List<FriendRequest>

    @Query("SELECT * FROM friend_requests WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1)")
    suspend fun getRequestsBetweenUsers(userId1: String, userId2: String): List<FriendRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendRequest(friendRequest: FriendRequest)

    @Query("UPDATE friend_requests SET status = :status WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String)

    @Query("DELETE FROM friend_requests WHERE requestId = :requestId")
    suspend fun deleteFriendRequest(requestId: String)
}