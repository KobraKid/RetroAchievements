package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
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

    private val _users = MutableLiveData<List<User>>()

    val users: LiveData<List<User>> get() = _users

    fun getTopUsers(currentUser: String?) {
        if (_users.value?.isEmpty() == true) {
            CoroutineScope(Dispatchers.IO).launch {
                RetroAchievementsApi.getInstance().GetTopTenUsers {
                    parseTopTenUsers(currentUser ?: "", it)
                }
            }
        }
    }

    private suspend fun parseTopTenUsers(currentUser: String, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_TOP_TEN_USERS -> {
                try {
                    val reader = JSONArray(response.second)
                    var loggedInUserIncluded = false
                    for (i in 0 until reader.length()) {
                        if ((reader[i] as JSONObject).getString("1") == currentUser)
                            loggedInUserIncluded = true
                        withContext(Main) {
                            _users.value = _users.value?.plus(User(
                                    rank = (i + 1).toString(),
                                    username = (reader[i] as JSONObject).getString("1"),
                                    totalPoints = (reader[i] as JSONObject).getString("2"),
                                    totalTruePoints = (reader[i] as JSONObject).getString("3")))
                        }
                    }
                    // Adjust for logged in user not being in the Top Ten
                    if (!loggedInUserIncluded) {
                        withContext(Dispatchers.IO) {
                            RetroAchievementsApi.getInstance().GetUserSummary(currentUser, 0) { parseTopTenUsers(currentUser, it) }
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
                        _users.value = _users.value?.plus(User(
                                rank = reader.getString("Rank"),
                                username = currentUser,
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