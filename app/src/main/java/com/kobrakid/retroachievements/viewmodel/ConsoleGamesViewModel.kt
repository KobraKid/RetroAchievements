package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.Game
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import com.kobrakid.retroachievements.model.IGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class ConsoleGamesViewModel : ViewModel() {

    private var consoleID = ""
    private val games = mutableListOf<Game>()
    private val _consoleGamesList = MutableLiveData<List<IGame>>()
    private val _loading = MutableLiveData<Boolean>()

    val consoleGamesList: LiveData<List<IGame>> get() = _consoleGamesList
    val loading: LiveData<Boolean> get() = _loading

    suspend fun setConsoleID(id: String, forceRefresh: Boolean = false) {
        consoleID = id
        // Prevent re-initialization
        if (!forceRefresh && consoleGamesList.value?.isNotEmpty() == true) {
            _loading.value = false
            return
        }
        _loading.value = true
        val db = RetroAchievementsDatabase.getInstance()
        if (!forceRefresh && withContext(IO) { db.gameDao().getGamesFromConsoleByID(consoleID).isNotEmpty() }) {
            _consoleGamesList.value = withContext(IO) { db.gameDao().getGamesFromConsoleByID(consoleID) }
            _loading.value = false
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
                                id = reader.getJSONObject(i).getString("ID"),
                                title = reader.getJSONObject(i).getString("Title"),
                                consoleID = reader.getJSONObject(i).getString("ConsoleID"),
                                consoleName = reader.getJSONObject(i).getString("ConsoleName"),
                                imageIcon = reader.getJSONObject(i).getString("ImageIcon"))
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
                        _consoleGamesList.value = games
                        _loading.value = false
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