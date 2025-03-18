package com.example.madproject.sampledata

import android.content.Context
import androidx.room.*
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1)
@TypeConverters(Converters::class) // Add this annotation
abstract class AppDatabase : RoomDatabase() {
    abstract fun UserDao(): UserDao

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "user-database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}