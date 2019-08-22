package com.kobrakid.retroachievements.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "game")
public class Game {

    @PrimaryKey
    @ColumnInfo(name = "ID")
    private int id;
    @ColumnInfo(name = "GameName")
    private String gameName;
    @ColumnInfo(name = "AchievementCount")
    private int achievementCount;

    public Game(int id, String gameName, int achievementCount) {
        this.id = id;
        this.gameName = gameName;
        this.achievementCount = achievementCount;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGameName() {
        return this.gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getAchievementCount() {
        return this.achievementCount;
    }

    public void setAchievementCount(int achievementCount) {
        this.achievementCount = achievementCount;
    }

    @Override
    public String toString() {
        return getGameName() + " | ID: " + getId() + ", # Achievements " + getAchievementCount();
    }

}
