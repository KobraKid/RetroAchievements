package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RankingsViewModel : ViewModel() {

    private lateinit var user: String
    val users = MutableLiveData<List<User>>(mutableListOf())

    fun getTopUsers(user: String?) {
        this.user = user ?: ""
        if (users.value?.isEmpty() == true) {
            CoroutineScope(Dispatchers.IO).launch {
                RetroAchievementsApi.getInstance().GetTopTenUsers { parseTopTenUsers(it) }
            }
        }
    }

    private suspend fun parseTopTenUsers(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_TOP_TEN_USERS -> {
                try {
                    val reader = JSONArray(response.second)
                    var loggedInUserIncluded = false
                    for (i in 0 until reader.length()) {
                        if ((reader[i] as JSONObject).getString("1") == user)
                            loggedInUserIncluded = true
                        withContext(Main) {
                            users.value = users.value?.plus(User(
                                    rank = (i + 1).toString(),
                                    username = (reader[i] as JSONObject).getString("1"),
                                    totalPoints = (reader[i] as JSONObject).getString("2"),
                                    totalTruePoints = (reader[i] as JSONObject).getString("3")))
                        }
                    }
                    // Adjust for logged in user not being in the Top Ten
                    if (!loggedInUserIncluded) {
                        withContext(Dispatchers.IO) {
                            RetroAchievementsApi.getInstance().GetUserSummary(user, 0) { parseTopTenUsers(it) }
                        }
                    }

                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse top ten users", e)
                }
            }
            RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                try {
                    val reader = JSONObject(response.second)
                    withContext(Main) {
                        users.value = users.value?.plus(User(
                                rank = reader.getString("Rank"),
                                username = user,
                                totalPoints = reader.getString("TotalPoints"),
                                totalTruePoints = reader.getString("TotalTruePoints")))
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse user summary", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + RankingsViewModel::class.java.simpleName
    }
}