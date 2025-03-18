package com.example.madproject.sampledata
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "positions")
data class Position(
    @PrimaryKey
    val positionId: String,
    val activityId: String,
    val activityName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)