package com.example.madproject.sampledata

import android.content.Context
import androidx.room.*
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Contact::class,
        Activity::class,
        Location::class,
        Status::class,
        Position::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun activityDao(): ActivityDao
    abstract fun locationDao(): LocationDao
    abstract fun statusDao(): StatusDao
    abstract fun positionDao(): PositionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "staysafe.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}