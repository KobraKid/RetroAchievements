package com.kobrakid.retroachievements.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Console::class, Game::class, Achievement::class], exportSchema = false, version = 5)
abstract class RetroAchievementsDatabase : RoomDatabase() {
    abstract fun consoleDao(): ConsoleDao
    abstract fun gameDao(): GameDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        private const val DB_NAME = "ra_db"
        private var instance: RetroAchievementsDatabase? = null
        private var TAG = RetroAchievementsDatabase::class.java.simpleName

        @Synchronized
        fun getInstance(context: Context? = null): RetroAchievementsDatabase {
            if (instance == null) {
                if (context == null) {
                    throw IllegalStateException("$TAG: ${RetroAchievementsDatabase::class.java.simpleName} not initialized")
                }
                instance = Room.databaseBuilder(
                        context.applicationContext,
                        RetroAchievementsDatabase::class.java, DB_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return instance!!
        }
    }
}