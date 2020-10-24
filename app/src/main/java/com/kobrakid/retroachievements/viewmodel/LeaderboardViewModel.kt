package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Leaderboard
import com.kobrakid.retroachievements.model.LeaderboardParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class LeaderboardViewModel : ViewModel() {

    val leaderboardCount: LiveData<Int> = MutableLiveData()
    val currentLeaderboard: LiveData<Int> = MutableLiveData()
    val participants: LiveData<List<LeaderboardParticipant>> = MutableLiveData()
    val loading: LiveData<Boolean> = MutableLiveData()

    fun setLeaderboard(leaderboard: Leaderboard) {
        (loading as MutableLiveData).value = true
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.getInstance().GetLeaderboard(leaderboard.id, leaderboard.numResults) { parseLeaderboard(it) }
        }
    }

    private suspend fun parseLeaderboard(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_LEADERBOARD -> {
                withContext(Dispatchers.Default) {
                    val users = mutableListOf<LeaderboardParticipant>()
                    val document = Jsoup.parse(response.second)
                    val userData = document.select("td.lb_user")
                    val resultData = document.select("td.lb_result")
                    val dateData = document.select("td.lb_date")
                    withContext(Main) { (leaderboardCount as MutableLiveData).value = userData.size }
                    for (i in userData.indices) {
                        withContext(Main) { (currentLeaderboard as MutableLiveData).value = i }
                        users.add(LeaderboardParticipant(
                                userData[i].text(),
                                resultData[i].text(),
                                dateData[i].text()))
                    }
                    withContext(Main) {
                        (participants as MutableLiveData).value = users
                        (loading as MutableLiveData).value = false
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + LeaderboardViewModel::class.java.simpleName
    }

}