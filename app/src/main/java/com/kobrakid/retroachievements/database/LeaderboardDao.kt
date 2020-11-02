package com.kobrakid.retroachievements.database

import androidx.room.*

@Dao
interface LeaderboardDao {

    @Query("SELECT * FROM leaderboard WHERE id == :id")
    fun getLeaderboardWithID(id: String): List<Leaderboard>

    @Query("SELECT * FROM leaderboard WHERE gameId == :gameID")
    fun getLeaderboardsForGameWithID(gameID: String): List<Leaderboard>

    @Query("DELETE FROM leaderboard")
    fun clearTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLeaderboard(leaderboard: Leaderboard?)

    @Update
    fun updateLeaderboard(leaderboard: Leaderboard?)

    @Delete
    fun deleteLeaderboard(leaderboard: Leaderboard?)
}