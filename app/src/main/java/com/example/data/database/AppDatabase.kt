package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ChallengeDao
import com.example.data.dao.ChatDao
import com.example.data.dao.MatchDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.UserDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DailyChallengeEntity
import com.example.data.model.InGameNotificationEntity
import com.example.data.model.MatchHistoryEntity
import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        MatchHistoryEntity::class,
        ChatMessageEntity::class,
        DailyChallengeEntity::class,
        InGameNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun matchDao(): MatchDao
    abstract fun chatDao(): ChatDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cosmo_rush_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
