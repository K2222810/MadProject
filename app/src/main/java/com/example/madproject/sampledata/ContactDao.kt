package com.example.madproject.sampledata

import androidx.room.*

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<Contact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Query("DELETE FROM contacts WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}