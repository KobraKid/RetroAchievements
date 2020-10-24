package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

class GameDetailsViewModel : ViewModel() {

    val game = MutableLiveData<Game>()
    private lateinit var id: String

    fun getGameInfoForUser(user: String?, id: String?) {
        this.id = id ?: "0"
        // TODO Linked hashes requires login
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.getInstance().GetGameInfoAndUserProgress(user, id
                    ?: "0") { parseGameInfoUserProgress(it) }
        }
    }

    private suspend fun parseGameInfoUserProgress(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                withContext(Dispatchers.Default) {
                    try {
                        val reader = JSONObject(response.second)
                        val title = Jsoup.parse(reader.getString("Title").trim { it <= ' ' }).text().let { title ->
                            if (title.contains(", The"))
                                "The " + title.indexOf(", The").let {
                                    title.substring(0, it) + title.substring(it + 5)
                                }
                            else title
                        }
                        val developer = reader.getString("Developer").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                        val publisher = reader.getString("Publisher").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                        val genre = reader.getString("Genre").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                        val released = reader.getString("Released").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                        withContext(Main) {
                            game.value = Game(
                                    this@GameDetailsViewModel.id,
                                    reader.getString("ConsoleName"),
                                    reader.getString("ImageIcon"),
                                    title, developer, publisher, genre, released,
                                    reader.getString("ForumTopicID")
                            )
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "unable to parse game details", e)
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + GameDetailsViewModel::class.java.simpleName
    }
}