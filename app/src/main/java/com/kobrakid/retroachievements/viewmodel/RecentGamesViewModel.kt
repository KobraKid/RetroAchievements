package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.GameSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class RecentGamesViewModel : ViewModel() {

    val loading: LiveData<Boolean> = MutableLiveData()
    val recentGames: LiveData<List<GameSummary>> = MutableLiveData(mutableListOf())
    private var offset = 0
    private val gamesPerApiCall = 15

    /**
     * Adds recent games to the view's recyclerview. Tries to catch the user reaching end of list
     * early and append games past the screen incrementally.
     *
     * @param scrollPosition The last visible item's index within the scrollview
     */
    fun getRecentGames(username: String?, itemCount: Int, scrollPosition: Int) {
        // Only get more games if there are none, or if the last request has completed and the user is scrolling near the end
        if ((scrollPosition == 0 && itemCount == 0) || (scrollPosition >= offset - gamesPerApiCall && itemCount == offset)) {
            (loading as MutableLiveData).value = true
            CoroutineScope(Dispatchers.IO).launch {
                RetroAchievementsApi.getInstance().GetUserRecentlyPlayedGames(username
                        ?: "", gamesPerApiCall, offset) { parseRecentlyPlayedGames(it) }
                offset += gamesPerApiCall
            }
        }
    }

    fun onRefresh() {
        offset = 0
        ((recentGames as MutableLiveData).value as MutableList).clear()
    }

    private suspend fun parseRecentlyPlayedGames(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_RECENTLY_PLAYED_GAMES -> {
                val games = mutableListOf<GameSummary>()
                try {
                    val reader = JSONArray(response.second)
                    for (i in 0 until reader.length()) {
                        games.add(GameSummary(
                                id = reader.getJSONObject(i).getString("GameID"),
                                title = reader.getJSONObject(i).getString("Title"),
                                imageIcon = reader.getJSONObject(i).getString("ImageIcon"),
                        ).apply {
                            setNumAchievementsEarned(reader.getJSONObject(i).getString("NumAchieved"))
                            setTotalAchievements(reader.getJSONObject(i).getString("NumPossibleAchievements"))
                            setEarnedPoints(reader.getJSONObject(i).getString("ScoreAchieved"))
                            setTotalPoints(reader.getJSONObject(i).getString("PossibleScore"))
                        })
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Failed to parse recenntly played games", e)
                } finally {
                    withContext(Main) {
                        (recentGames as MutableLiveData).value = (recentGames.value as MutableList).apply { addAll(games) }
                        (loading as MutableLiveData).value = false
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + RecentGamesViewModel::class.java.simpleName
    }

}