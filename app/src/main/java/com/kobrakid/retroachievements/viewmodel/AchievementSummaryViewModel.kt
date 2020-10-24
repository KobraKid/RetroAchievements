package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Achievement
import com.kobrakid.retroachievements.model.GameSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class AchievementSummaryViewModel : ViewModel() {

    // Holds the list of achievements
    val achievements: LiveData<List<Achievement>> = MutableLiveData()

    // Holds summary data about a game's achievements
    val game: LiveData<GameSummary> = MutableLiveData()

    val loading: LiveData<Boolean> = MutableLiveData()

    fun getGameInfoForUser(user: String?, id: String?) {
        (loading as MutableLiveData).value = true
        CoroutineScope(IO).launch {
            RetroAchievementsApi.getInstance().GetGameInfoAndUserProgress(
                    user ?: "", id ?: "0") { parseGameInfoAndUserProgress(it) }
        }
    }

    private suspend fun parseGameInfoAndUserProgress(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                withContext(Default) {
                    val gameSummary = GameSummary(numDistinctCasual = JSONObject(response.second).getString("NumDistinctPlayersCasual").toInt())
                    val achievementList = mutableListOf<Achievement>()
                    try {
                        val achievements = JSONObject(response.second).getJSONObject("Achievements")
                        val displayOrder = mutableListOf<Int>()
                        val displayOrderEarned = mutableListOf<Int>()
                        var count: Int
                        var dateEarned: String
                        var earnedHC: Boolean
                        achievements.keys().forEach { id ->
                            val achievement = achievements.getJSONObject(id)
                            when {
                                achievement.has("DateEarnedHardcore") -> {
                                    gameSummary.numAchievementsEarnedHC++
                                    gameSummary.numAchievementsEarned++
                                    gameSummary.earnedPoints += 2 * achievement.getString("Points").toInt()
                                    gameSummary.earnedTruePoints += achievement.getString("TrueRatio").toInt()
                                    dateEarned = achievement.getString("DateEarnedHardcore")
                                    displayOrderEarned.add(achievement.getString("DisplayOrder").toInt())
                                    displayOrderEarned.sort()
                                    count = displayOrderEarned.indexOf(achievement.getString("DisplayOrder").toInt())
                                    earnedHC = true
                                }
                                achievement.has("DateEarned") -> {
                                    gameSummary.numAchievementsEarned++
                                    gameSummary.earnedPoints += achievement.getString("Points").toInt()
                                    gameSummary.earnedTruePoints += achievement.getString("TrueRatio").toInt()
                                    dateEarned = achievement.getString("DateEarned")
                                    displayOrderEarned.add(achievement.getString("DisplayOrder").toInt())
                                    displayOrderEarned.sort()
                                    count = displayOrderEarned.indexOf(achievement.getString("DisplayOrder").toInt())
                                    earnedHC = false
                                }
                                else -> {
                                    dateEarned = ""
                                    displayOrder.add(achievement.getString("DisplayOrder").toInt())
                                    displayOrder.sort()
                                    count = displayOrder.indexOf(achievement.getString("DisplayOrder").toInt()) + displayOrderEarned.size
                                    earnedHC = false
                                }
                            }
                            if (dateEarned.isEmpty()) {
                                dateEarned = "NoDate:$count"
                                earnedHC = false
                            }
                            if (count == 0) {
                                count = gameSummary.totalAchievements
                            }
                            achievementList.add(Achievement(
                                    id,
                                    achievement.getString("BadgeName"),
                                    achievement.getString("Title"),
                                    achievement.getString("Points"),
                                    achievement.getString("TrueRatio"),
                                    achievement.getString("Description"),
                                    dateEarned,
                                    earnedHC,
                                    achievement.getString("NumAwarded"),
                                    achievement.getString("NumAwardedHardcore"),
                                    achievement.getString("Author"),
                                    achievement.getString("DateCreated"),
                                    achievement.getString("DateModified")))
                            gameSummary.totalAchievements++
                            gameSummary.totalPoints += achievement.getString("Points").toInt()
                            gameSummary.totalTruePoints += achievement.getString("TrueRatio").toInt()
                        }
                    } catch (e: JSONException) {
                        if (e.toString().contains("Value [] at Achievements of type org.json.JSONArray cannot be converted to JSONObject"))
                            Log.d(TAG, "This game has no achievements")
                        else
                            Log.e(TAG, response.second, e)
                    } finally {
                        withContext(Main) {
                            (game as MutableLiveData).value = gameSummary
                            (achievements as MutableLiveData).value = achievementList
                            (loading as MutableLiveData).value = false
                        }
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + AchievementSummaryViewModel::class.java.simpleName
    }
}