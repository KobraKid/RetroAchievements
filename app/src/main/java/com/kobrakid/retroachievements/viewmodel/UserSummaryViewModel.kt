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
import org.json.JSONException
import org.json.JSONObject

class UserSummaryViewModel : ViewModel() {

    private var username: String = ""
    private val _userState = MutableLiveData<User>()

    val userState: LiveData<User> get() = _userState

    fun setUsername(username: String) {
        if (username != this.username) {
            this.username = username
            CoroutineScope(Dispatchers.IO).launch {
                RetroAchievementsApi.getInstance().GetUserSummary(username, 5) { parseUserSummary(it) }
            }
        }
    }

    private suspend fun parseUserSummary(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                try {
                    val reader = JSONObject(response.second)
                    val motto = "\"${reader.getString("Motto")}\""
                    withContext(Main) {
                        _userState.value = User(
                                username = username,
                                rank = reader.getString("Rank"),
                                motto = if (motto.length > 2) motto else "",
                                totalPoints = reader.getString("TotalPoints"),
                                totalTruePoints = reader.getString("TotalTruePoints"),
                                memberSince = reader.getString("MemberSince"))
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Unable to parse user summary", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + UserSummaryViewModel::class.java.simpleName
    }
}