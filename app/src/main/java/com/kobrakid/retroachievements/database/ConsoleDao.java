package com.kobrakid.retroachievements.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
