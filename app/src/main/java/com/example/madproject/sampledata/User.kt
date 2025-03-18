package com.example.madproject.sampledata
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val username: String,
    val password: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
