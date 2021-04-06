package com.kobrakid.retroachievements.model

import android.util.Log
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
 * Summarizes the current user's progress towards this game.
 *
 * @property id The game's id
 * @property title The game's title
 * @property imageIcon The game's image icon
 * @property numDistinctPlayersCasual The number of distinct casual players that have attempted this game
 * @property numAwardedToUser The number of achievements the user has earned
 * @property numAwardedToUserHardcore The numeber of achievements the user has earned in hardcore mode
 * @property numAchievements The total number of achievements this game has
 * @property earnedPoints The number of points the user has earned
 * @property totalPoints The total number of points this game is worth
 * @property earnedTruePoints The number of true points the user has earned
 * @property totalTruePoints The total number of true points this game is worth
 */
data class GameProgress(
        override var id: String = "0",
        override var title: String = "",
        override var consoleID: String = "0",
        override var forumTopicID: String = "0",
        override var flags: Int = 0,
        override var imageIcon: String = "",
        override var imageTitle: String = "",
        override var imageIngame: String = "",
        override var imageBoxArt: String = "",
        override var publisher: String = "",
        override var developer: String = "",
        override var genre: String = "",
        override var released: String = "",
        override var isFinal: Boolean = true,
        override var consoleName: String = "",
        override var richPresencePatch: String = "",
        override var numAchievements: Int = 0,
        override var numDistinctPlayersCasual: Int = 0,
        override var numDistinctPlayersHardcore: Int = 0,
        override var numAwardedToUser: Int = 0,
        override var numAwardedToUserHardcore: Int = 0,
        override var userCompletion: String = "",
        override var userCompletionHardcore: String = "",
        var earnedPoints: Int = 0,
        var totalPoints: Int = 0,
        var earnedTruePoints: Int = 0,
        var totalTruePoints: Int = 0) : IGame {

    companion object {
        suspend fun getAchievementsForGame(user: String?, id: String?, callback: suspend (List<IAchievement>) -> Unit) {
            if (user?.isNotEmpty() == true && id?.isNotEmpty() == true) {
                // get game from db
                callback(RetroAchievementsDatabase.getInstance().achievementDao().getAchievementsForGameWithId(id))
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
                            val gameID = JSONObject(response.second).getString("ID")
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
                                        id = gameID,
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
                                    RetroAchievementsDatabase.getInstance().achievementDao().insertAchievement(it)
                                }
                            }
                            callback(achievementList)
                        }
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + GameProgress::class.java.simpleName
    }

}