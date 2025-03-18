package com.example.madproject.sampledata

import androidx.room.*

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions")
    suspend fun getAllPositions(): List<Position>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: Position)

    @Query("DELETE FROM positions WHERE PositionID = :positionId")
    suspend fun deletePositionById(positionId: String)
}