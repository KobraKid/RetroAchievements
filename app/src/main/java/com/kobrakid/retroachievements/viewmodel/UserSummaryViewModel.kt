package com.kobrakid.retroachievements.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.UserSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class UserSummaryViewModel : ViewModel() {
    private var username: String = ""
    private val _userSummaryState = MutableLiveData(UserSummary())
    val userSummaryState: LiveData<UserSummary> = _userSummaryState

    fun setUsername(context: Context?, username: String) {
        this.username = username
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.GetUserSummary(context, username, 5) { parseUserSummary(context, it) }
        }
    }

    private suspend fun parseUserSummary(context: Context?, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                try {
                    withContext(Main) {
                        val reader = JSONObject(response.second)
                        val motto = "\"${reader.getString("Motto")}\""
                        context?.let {
                            _userSummaryState.value = UserSummary(
                                    username,
                                    it.getString(R.string.user_rank, reader.getString("Rank")),
                                    if (motto.length > 2) motto else "",
                                    it.getString(R.string.user_points, reader.getString("TotalPoints")),
                                    it.getString(R.string.user_points, reader.getString("TotalTruePoints")),
                                    it.getString(R.string.user_ratio, String.format("%.2f", reader.getString("TotalTruePoints").toFloat().div(reader.getString("TotalPoints").toFloat()))),
                                    it.getString(R.string.user_joined, reader.getString("MemberSince")))
                        }
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