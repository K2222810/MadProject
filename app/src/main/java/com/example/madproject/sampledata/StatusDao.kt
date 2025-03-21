package com.example.madproject.sampledata

import androidx.room.*

@Dao
interface StatusDao {
    @Query("SELECT * FROM statuses")
    suspend fun getAllStatuses(): List<Status>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: Status)

    @Query("DELETE FROM statuses WHERE statusId = :statusId")
    suspend fun deleteStatusById(statusId: String)
}