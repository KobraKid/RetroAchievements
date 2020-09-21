package com.kobrakid.retroachievements.viewmodel

import android.content.Context
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

    private val _consoleGamesList = MutableLiveData<List<Game?>>().apply { value = listOf() }
    val consoleGamesList: LiveData<List<Game?>> get() = _consoleGamesList
    val loading = MutableLiveData(true)
    private var consoleID = ""

    private val games = mutableListOf<Game>()

    suspend fun setConsoleID(context: Context?, id: String, forceRefresh: Boolean = false) {
        consoleID = id
        // Prevent re-initialization
        if (!forceRefresh && consoleGamesList.value?.isNotEmpty() == true) {
            loading.value = false
            return
        }
        loading.value = true
        val db = context?.let { RetroAchievementsDatabase.getInstance(context) }
        if (!forceRefresh && withContext(IO) { db?.gameDao()?.getGamesFromConsoleByID(consoleID)?.isNotEmpty() } == true) {
            _consoleGamesList.value = withContext(IO) { db?.gameDao()?.getGamesFromConsoleByID(consoleID) }
            loading.value = false
        } else {
            CoroutineScope(IO).launch {
                RetroAchievementsApi.GetGameList(context, consoleID) { parseGameList(context, it) }
            }
        }
    }

    private suspend fun parseGameList(context: Context?, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
                    for (i in 0 until reader.length()) {
                        val game = Game(
                                reader.getJSONObject(i).getString("ID"),
                                reader.getJSONObject(i).getString("Title"),
                                reader.getJSONObject(i).getString("ConsoleID"),
                                reader.getJSONObject(i).getString("ConsoleName"),
                                reader.getJSONObject(i).getString("ImageIcon"))
                        withContext(IO) {
                            if (db?.gameDao()?.getGameWithID(game.id)?.isNotEmpty() == true) {
                                db.gameDao()?.updateGame(game)
                            } else {
                                db?.gameDao()?.insertGame(game)
                            }
                        }
                        games.add(game)
                    }
                    withContext(Main) {
                        _consoleGamesList.value = games
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