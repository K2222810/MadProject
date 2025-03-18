package com.example.madproject.sampledata

import android.content.Context

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