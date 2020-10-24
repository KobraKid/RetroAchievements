package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class MainViewModel : ViewModel() {

    val user: LiveData<User> = MutableLiveData()

    fun setUsername(username: String?) {
        (user as MutableLiveData).value = User(username ?: "")
        if (username != null) {
            CoroutineScope(IO).launch {
                RetroAchievementsApi.getInstance().GetUserRankAndScore(username) { parseRankScore(it) }
            }
        }
    }

    private fun setRank(r: String) {
        (user as MutableLiveData).value = User(user.value).apply { rank = r }
    }

    private fun setScore(s: String) {
        (user as MutableLiveData).value = User(user.value).apply { totalPoints = s }
    }

    private suspend fun parseRankScore(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_RANK_AND_SCORE -> {
                withContext(Default) {
                    try {
                        val reader = JSONObject(response.second)
                        withContext(Main) {
                            setRank(reader.getString("Rank"))
                            setScore(reader.getString("Score"))
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse user rank/score: ${response.second}", e)
                    } finally {

                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + MainViewModel::class.java.simpleName
    }
}