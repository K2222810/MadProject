package com.example.madproject.sampledata

import androidx.room.*

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities")
    suspend fun getAllActivities(): List<Activity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Query("DELETE FROM activities WHERE activityId = :activityId")
    suspend fun deleteActivityById(activityId: String)
}