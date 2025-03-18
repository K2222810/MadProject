package com.example.madproject.sampledata
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statuses")
data class Status(
    @PrimaryKey
    val statusId: String,
    val name: String,
    val order: Int = 0
)