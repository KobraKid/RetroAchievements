package com.kobrakid.retroachievements.model

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * Represents a game's summary information.
 * Summarizes the current user's progress towards this game.
 *
 * @property id The game's id
 * @property title The game's title
 * @property imageIcon The game's image icon
 * @property numDistinctCasual The number of distinct casual players that have attempted this game
 * @property numAchievementsEarned The number of achievements the user has earned
 * @property numAchievementsEarnedHardcore The numeber of achievements the user has earned in hardcore mode
 * @property totalAchievements The total number of achievements this game has
 * @property earnedPoints The number of points the user has earned
 * @property totalPoints The total number of points this game is worth
 * @property earnedTruePoints The number of true points the user has earned
 * @property totalTruePoints The total number of true points this game is worth
 */
data class GameSummary(
        var id: String = "0",
        var title: String = "",
        var imageIcon: String = "",
        var numDistinctCasual: Int = 0,
        var numAchievementsEarned: Int = 0,
        var numAchievementsEarnedHardcore: Int = 0,
        var totalAchievements: Int = 0,
        var earnedPoints: Int = 0,
        var totalPoints: Int = 0,
        var earnedTruePoints: Int = 0,
        var totalTruePoints: Int = 0) {

    // Utility functions for setting int values from parsed strings

    fun setNumAchievementsEarned(points: String) {
        numAchievementsEarned = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setNumAchievementsEarnedHC(points: String) {
        numAchievementsEarnedHardcore = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setTotalAchievements(points: String) {
        totalAchievements = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setEarnedPoints(points: String) {
        earnedPoints = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setTotalPoints(points: String) {
        totalPoints = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setEarnedTruePoints(points: String) {
        earnedTruePoints = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setTotalTruePoints(points: String) {
        totalTruePoints = if (points.isDigitsOnly()) points.toInt() else 0
    }

    companion object {
        suspend fun getAchievementsForGame(user: String?, id: String?, callback: suspend (List<IAchievement>) -> Unit) {
            if (user?.isNotEmpty() == true && id?.isNotEmpty() == true) {
                // get game from db
                RetroAchievementsDatabase.getInstance().achievementDao().getAchievementsForGameWithId(id).let {
                    if (it.isNotEmpty()) {
                        callback(it)
                    }
                }
                // update achievements from network
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.getInstance().GetGameInfoAndUserProgress(user, id) { parseGameInfoAndUserProgress(it, callback) }
                }
            } else {
                // no achievements can be returned
                callback(listOf())
            }
        }

        private suspend fun parseGameInfoAndUserProgress(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (List<IAchievement>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                    withContext(Default) {
                        val achievementList = mutableListOf<Achievement>()
                        try {
                            val achievements = JSONObject(response.second).getJSONObject("Achievements")
                            var dateEarned: String
                            var dateEarnedHardcore: String
                            achievements.keys().forEach { id ->
                                val achievement = achievements.getJSONObject(id)
                                when {
                                    achievement.has("DateEarnedHardcore") -> {
                                        dateEarned = achievement.getString("DateEarned")
                                        dateEarnedHardcore = achievement.getString("DateEarnedHardcore")
                                    }
                                    achievement.has("DateEarned") -> {
                                        dateEarned = achievement.getString("DateEarned")
                                        dateEarnedHardcore = ""
                                    }
                                    else -> {
                                        dateEarned = ""
                                        dateEarnedHardcore = ""
                                    }
                                }
                                achievementList.add(Achievement(
                                        achievementID = id,
                                        id = achievement.getString("ID"),
                                        numAwarded = achievement.getString("NumAwarded"),
                                        numAwardedHardcore = achievement.getString("NumAwardedHardcore"),
                                        title = achievement.getString("Title"),
                                        description = achievement.getString("Description"),
                                        points = achievement.getString("Points"),
                                        truePoints = achievement.getString("TrueRatio"),
                                        author = achievement.getString("Author"),
                                        dateModified = achievement.getString("DateModified"),
                                        dateCreated = achievement.getString("DateCreated"),
                                        badgeName = achievement.getString("BadgeName"),
                                        displayOrder = achievement.getString("DisplayOrder"),
                                        memAddr = achievement.getString("MemAddr"),
                                        dateEarned = dateEarned,
                                        dateEarnedHardcore = dateEarnedHardcore))
                            }
                        } catch (e: JSONException) {
                            if (e.toString().contains("Value [] at Achievements of type org.json.JSONArray cannot be converted to JSONObject"))
                                Log.d(TAG, "This game has no achievements")
                            else
                                Log.e(TAG, response.second, e)
                        } finally {
                            withContext(IO) {
                                achievementList.forEach {
                                    RetroAchievementsDatabase.getInstance().achievementDao().insertAchievement(Achievement.convertAchievementModelToDatabase(it))
                                }
                            }
                            callback(achievementList)
                        }
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + GameSummary::class.java.simpleName
    }

}