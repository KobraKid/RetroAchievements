package com.kobrakid.retroachievements.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Console::class, Game::class, Achievement::class], exportSchema = false, version = 4)
abstract class RetroAchievementsDatabase : RoomDatabase() {
    abstract fun consoleDao(): ConsoleDao
    abstract fun gameDao(): GameDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        private const val DB_NAME = "ra_db"
        private var instance: RetroAchievementsDatabase? = null

        @Synchronized
        fun getInstance(context: Context): RetroAchievementsDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, RetroAchievementsDatabase::class.java, DB_NAME).fallbackToDestructiveMigration().build()
            }
            return instance
        }
    }
}