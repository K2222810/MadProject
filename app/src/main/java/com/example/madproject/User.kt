package com.example.madproject
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate


@Entity(tableName = "users")
data class User(
    @PrimaryKey val userName: String,
    val contact: String,
    val activity: String = "Other",
    val publishDate: LocalDate? = null, // Keep as LocalDate
    val location: Int = 0,
    val status: Int = 0,
    val position: Int = 0


)

