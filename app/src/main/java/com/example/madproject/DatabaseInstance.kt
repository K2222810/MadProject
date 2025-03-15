package com.example.madproject

import android.content.Context
import androidx.room.Room

object DatabaseInstance {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            INSTANCE = instance
            instance
        }
    }
}