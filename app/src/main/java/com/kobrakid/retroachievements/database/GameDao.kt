package com.kobrakid.retroachievements.database

import androidx.room.*

@Dao
interface GameDao {
    @get:Query("SELECT * FROM game")
    val gameList: List<Game?>?

    @Query("SELECT * FROM game WHERE id == :gameID")
    fun getGameWithID(gameID: Int): List<Game?>?

    @Query("SELECT * FROM game WHERE ConsoleID == :consoleID")
    fun getGamesFromConsoleByID(consoleID: String): List<Game?>?

    @Query("DELETE FROM game")
    fun clearTable()

    @Insert
    fun insertGame(game: Game?)

    @Update
    fun updateGame(game: Game?)

    @Delete
    fun deleteGame(game: Game?)
}