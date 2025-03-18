package com.example.madproject.sampledata

import androidx.room.*

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations")
    suspend fun getAllLocations(): List<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    @Query("DELETE FROM locations WHERE LocationID = :locationId")
    suspend fun deleteLocationById(locationId: String)
}