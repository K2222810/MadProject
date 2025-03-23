package com.example.madproject.sampledata
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("UserID") val userId: String,
    @SerializedName("UserFirstname") val firstName: String?,
    @SerializedName("UserLastname") val lastName: String?,
    @SerializedName("UserPhone") val phone: String?,
    @SerializedName("UserUsername") val username: String?,
    @SerializedName("UserPassword") val password: String?,
    @SerializedName("UserLatitude") val latitude: Double?,
    @SerializedName("UserLongitude") val longitude: Double?,
    @SerializedName("UserTimestamp") val timestamp: Long?
)