package com.kobrakid.retroachievements.database

import androidx.room.*

@Dao
interface ConsoleDao {
    @get:Query("SELECT * FROM console")
    val consoleList: List<Console>

    @Query("SELECT * FROM console WHERE id == :consoleID")
    fun getConsoleWithID(consoleID: String): List<Console>

    @Query("DELETE FROM console")
    fun clearTable()

    @Insert
    fun insertConsole(console: Console)

    @Update
    fun updateConsole(console: Console)

    @Delete
    fun deleteConsole(console: Console)
}