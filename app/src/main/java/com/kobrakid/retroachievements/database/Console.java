package com.kobrakid.retroachievements.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "console")
public class Console {

    @PrimaryKey
    @ColumnInfo(name = "ID")
    private int id;
    @ColumnInfo(name = "ConsoleName")
    private String consoleName;
    @ColumnInfo(name = "GameCount")
    private int gameCount;

    public Console(int id, String consoleName, int gameCount) {
        this.id = id;
        this.consoleName = consoleName;
        this.gameCount = gameCount;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConsoleName() {
        return this.consoleName;
    }

    public void setConsoleName(String consoleName) {
        this.consoleName = consoleName;
    }

    public int getGameCount() {
        return this.gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    @NonNull
    @Override
    public String toString() {
        return getConsoleName() + " | ID: " + getId() + ", Count: " + getGameCount();
    }
}
