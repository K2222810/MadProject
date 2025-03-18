package com.example.madproject.sampledata
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey
     val activityId: String,
     val name: String,
     val userId: String,
     val username: String,
     val description: String,
     val fromLocationId: String,
     val fromLocationName: String,
     val leaveTime: Long,
     val toLocationId: String,
     val toLocationName: String,
     val arriveTime: Long,
     val statusId: String,
     val statusName: String
)