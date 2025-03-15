package com.example.madproject

import androidx.room.*


@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE userName = :title") // Changed from name to title
    suspend fun deleteUserByTitle(title: String)
}