package com.example.madproject.sampledata
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey
    val contactId: String,
    val userId: String,
    val contactUserId: String,
    val label: String,
    val dateCreated: Long
)