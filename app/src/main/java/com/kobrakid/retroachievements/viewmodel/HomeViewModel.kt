package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.GameSummary
import com.kobrakid.retroachievements.model.Mastery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

class HomeViewModel : ViewModel() {

    private val _masteries = MutableLiveData<List<Mastery>>()
    private val _recentGames = MutableLiveData<List<GameSummary>>()

    val masteries: LiveData<List<Mastery>> get() = _masteries
    val recentGames: LiveData<List<GameSummary>> get() = _recentGames

    fun setUser(username: String?) {
        if (username != null) {
            CoroutineScope(Dispatchers.IO).launch {
                RetroAchievementsApi.getInstance().GetUserWebProfile(username) { parseUserWebProfile(it) }
                RetroAchievementsApi.getInstance().GetUserSummary(username, NUM_RECENT_GAMES) { parseUserSummary(it) }
            }
        }
    }

    private suspend fun parseUserWebProfile(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_WEB_PROFILE -> {
                // Parse response on the Default thread
                withContext(Dispatchers.Default) {
                    val masteries = mutableListOf<Mastery>()
                    Jsoup.parse(response.second).select("div[class=trophyimage]").forEach {
                        val gameID = it.selectFirst("a[href]").attr("href")
                        if (gameID.length >= 6) {
                            val image = it.selectFirst("img[src]")
                            masteries.add(Mastery(
                                    id = gameID.substring(6),
                                    icon = image.attr("src"),
                                    mastered = image.className() == "goldimage"))
                        }
                    }
                    withContext(Main) { _masteries.value = masteries }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private suspend fun parseUserSummary(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                withContext(Dispatchers.Default) {
                    val recent = mutableListOf<GameSummary>()
                    try {
                        val reader = JSONObject(response.second)
                        val recentlyPlayed = reader.getJSONArray("RecentlyPlayed")
                        for (i in 0 until recentlyPlayed.length()) {
                            val gameObj = recentlyPlayed.getJSONObject(i)
                            val awards = reader.getJSONObject("Awarded").getJSONObject(gameObj.getString("GameID"))
                            val awarded = awards.getString("NumAchieved")
                            val awardedHC = awards.getString("NumAchievedHardcore")
                            recent.add(GameSummary(
                                    id = gameObj.getString("GameID"),
                                    title = gameObj.getString("Title"),
                                    imageIcon = gameObj.getString("ImageIcon")).apply {
                                setNumAchievementsEarned(awarded)
                                setNumAchievementsEarnedHC(awardedHC)
                                setTotalAchievements(awards.getString("NumPossibleAchievements"))
                                setEarnedPoints(
                                        if (awarded.let { if (it.isDigitsOnly()) it.toInt() else 0 } > awardedHC.let { if (it.isDigitsOnly()) it.toInt() else 0 })
                                            awards.getString("ScoreAchieved")
                                        else
                                            awards.getString("ScoreAchievedHardcore"))
                                setTotalPoints(awards.getString("PossibleScore"))
                            })
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, response.second, e)
                    } finally {
                        withContext(Main) { _recentGames.value = recent }
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + HomeViewModel::class.java.simpleName
        const val NUM_RECENT_GAMES = 5
    }
}