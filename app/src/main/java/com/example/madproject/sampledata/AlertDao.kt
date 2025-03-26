package com.example.madproject.sampledata

import androidx.room.*

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    suspend fun getAllAlerts(): List<Alert>

    @Query("SELECT * FROM alerts WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAlertsForUser(userId: String): List<Alert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert)

    @Query("UPDATE alerts SET isActive = :isActive WHERE alertId = :alertId")
    suspend fun updateAlertStatus(alertId: String, isActive: Boolean)

    @Query("DELETE FROM alerts WHERE alertId = :alertId")
    suspend fun deleteAlert(alertId: String)
}