package com.kobrakid.retroachievements.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ConsoleDao {

    @Query("SELECT * FROM console")
    List<Console> getConsoleList();

    @Query("SELECT * FROM console WHERE id == :consoleID")
    List<Console> getConsoleWithID(int consoleID);

    @Query("DELETE FROM console")
    void clearTable();

    @Insert
    void insertConsole(Console console);

    @Update
    void updateConsole(Console console);

    @Delete
    void deleteConsole(Console console);

}
