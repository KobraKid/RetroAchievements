package com.kobrakid.retroachievements.database

import androidx.room.*

@Dao
interface AchievementDao {
    @get:Query("SELECT * FROM achievement")
    val achievementList: List<Achievement>

    @Query("SELECT * FROM achievement WHERE AchievementID == :achievementID")
    fun getAchievementWithID(achievementID: String): List<Achievement>

    @Query("SELECT * FROM achievement WHERE ID == :id")
    fun getAchievementsForGameWithId(id: String): List<Achievement>

    @Query("DELETE FROM achievement")
    fun clearTable()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAchievement(achievement: Achievement)

    @Update
    fun updateAchievement(achievement: Achievement)

    @Delete
    fun deleteAchievement(achievement: Achievement)
}