package com.kobrakid.retroachievements.database

import androidx.room.*
import com.kobrakid.retroachievements.model.Console

@Dao
interface ConsoleDao {
    @get:Query("SELECT * FROM console")
    val consoleList: List<Console>

    @get:Query("SELECT * FROM console WHERE NumGames > 0")
    val nonEmptyConsoles: List<Console>

    @Query("SELECT * FROM console WHERE id == :consoleID")
    fun getConsoleWithID(consoleID: String): List<Console>

    @Query("DELETE FROM console")
    fun clearTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConsole(console: Console)

    @Update
    fun updateConsole(console: Console)

    @Delete
    fun deleteConsole(console: Console)
}