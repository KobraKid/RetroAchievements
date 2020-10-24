package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.Game
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class ConsoleGamesViewModel : ViewModel() {

    val consoleGamesList: LiveData<List<Game?>> = MutableLiveData()
    val loading = MutableLiveData(true)
    private var consoleID = ""

    private val games = mutableListOf<Game>()

    suspend fun setConsoleID(id: String, forceRefresh: Boolean = false) {
        consoleID = id
        // Prevent re-initialization
        if (!forceRefresh && consoleGamesList.value?.isNotEmpty() == true) {
            loading.value = false
            return
        }
        loading.value = true
        val db = RetroAchievementsDatabase.getInstance()
        if (!forceRefresh && withContext(IO) { db.gameDao().getGamesFromConsoleByID(consoleID).isNotEmpty() }) {
            (consoleGamesList as MutableLiveData).value = withContext(IO) { db.gameDao().getGamesFromConsoleByID(consoleID) }
            loading.value = false
        } else {
            CoroutineScope(IO).launch {
                RetroAchievementsApi.getInstance().GetGameList(consoleID) { parseGameList(it) }
            }
        }
    }

    private suspend fun parseGameList(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    val db = RetroAchievementsDatabase.getInstance()
                    for (i in 0 until reader.length()) {
                        val game = Game(
                                reader.getJSONObject(i).getString("ID"),
                                reader.getJSONObject(i).getString("Title"),
                                reader.getJSONObject(i).getString("ConsoleID"),
                                reader.getJSONObject(i).getString("ConsoleName"),
                                reader.getJSONObject(i).getString("ImageIcon"))
                        withContext(IO) {
                            if (db.gameDao().getGameWithID(game.id).isNotEmpty()) {
                                db.gameDao().updateGame(game)
                            } else {
                                db.gameDao().insertGame(game)
                            }
                        }
                        games.add(game)
                    }
                    withContext(Main) {
                        (consoleGamesList as MutableLiveData).value = games
                        loading.value = false
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse game list", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + ConsoleGamesViewModel::class.java.simpleName
    }
}