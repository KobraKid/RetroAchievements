package com.kobrakid.retroachievements.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Console.class, Game.class}, exportSchema = false, version = 3)
public abstract class RetroAchievementsDatabase extends RoomDatabase {
    private static final String DB_NAME = "ra_db";
    private static RetroAchievementsDatabase instance;

    public static synchronized RetroAchievementsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), RetroAchievementsDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    public abstract ConsoleDao consoleDao();

    public abstract GameDao gameDao();

}
