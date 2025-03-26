package com.example.madproject.sampledata

import android.content.Context
import androidx.room.*
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        User::class,
        Contact::class,
        Activity::class,
        Location::class,
        Status::class,
        Position::class,
        FriendRequest::class  // Add FriendRequest entity
    ],
    version = 3, // Increase database version
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
    abstract fun friendRequestDao(): FriendRequestDao  // Add DAO for FriendRequests

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        // Migration from 2 to 3 for adding FriendRequest table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `friend_requests` (" +
                            "`requestId` TEXT NOT NULL, " +
                            "`senderId` TEXT NOT NULL, " +
                            "`senderUsername` TEXT NOT NULL, " +
                            "`receiverId` TEXT NOT NULL, " +
                            "`status` TEXT NOT NULL, " +
                            "`message` TEXT NOT NULL, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`requestId`))"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "staysafe.db"
                )
                    .addMigrations(MIGRATION_2_3)  // Add migration
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}