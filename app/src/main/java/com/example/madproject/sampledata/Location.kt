package com.example.madproject.sampledata
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "locations")
data class Location(
    @PrimaryKey
    val locationId: String,
    val name: String,
    val description: String,
    val address: String,
    val postcode: String,
    val latitude: Double,
    val longitude: Double
)