package com.kobrakid.retroachievements.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.Console
import com.kobrakid.retroachievements.database.Game
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.util.*

class ConsoleListViewModel : ViewModel() {

    private val _consoleList = MutableLiveData<List<Console?>>().apply { value = listOf() }
    val consoleList: LiveData<List<Console?>> get() = _consoleList
    private val _gameList = MutableLiveData<List<Game?>>().apply { value = listOf() }
    val gameList: LiveData<List<Game?>> get() = _gameList
    val loading = MutableLiveData(true)

    private var hideEmptyConsoles = false

    // Keep track of each console that needs to be parsed
    private var consoleQueue = Collections.synchronizedList(mutableListOf<String>())

    // Keep track of each console that has been parsed
    private var parsedConsoles = Collections.synchronizedList(mutableListOf<Console>())

    // Keep track of each console that has been parsed and skipped (empty)
    private var skippedConsoles = Collections.synchronizedList(mutableListOf<Console>())

    suspend fun init(context: Context?, refreshDatabase: Boolean = false) {
        // Check to see if empty consoles should be hidden
        hideEmptyConsoles = context
                ?.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                ?.getBoolean(context.getString(R.string.empty_console_hide_setting), false)
                ?: false

        // Prevent re-initialization
        if (!refreshDatabase && initialized) {
            loading.value = false
            return
        }

        // Check if database is aleady populated
        val db = context?.let { RetroAchievementsDatabase.getInstance(context) }
        if (!refreshDatabase && withContext(IO) { db?.consoleDao()?.consoleList?.isNotEmpty() } == true) {
            _consoleList.value = withContext(IO) { db?.consoleDao()?.consoleList }
            _gameList.value = withContext(IO) { db?.gameDao()?.gameList }
            loading.value = false
        } else {
            CoroutineScope(IO).launch {
                db?.consoleDao()?.clearTable()
                db?.gameDao()?.clearTable()
                RetroAchievementsApi.GetConsoleIDs(context) { parseConsoles(context, it) }
            }
        }

        initialized = true
    }

    private suspend fun parseConsoles(context: Context?, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
                try {
                    val reader = JSONArray(response.second)
                    val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
                    for (i in 0 until reader.length()) {
                        val id = reader.getJSONObject(i).getString("ID")
                        val name = reader.getJSONObject(i).getString(("Name"))
                        consoleQueue.add(name)
                        withContext(IO) {
                            db?.consoleDao()?.insertConsole(Console(id, name))
                            RetroAchievementsApi.GetGameList(context, id) { parseGameList(context, id, name, it) }
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse console IDs", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private suspend fun parseGameList(context: Context?, consoleID: String, consoleName: String, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
                    val console = Console(consoleID, consoleName)
                    for (i in 0 until reader.length()) {
                        reader.getJSONObject(i).let {
                            val id = it.getString("ID")
                            val title = it.getString("Title")
                            val imageIcon = it.getString("ImageIcon")
                            withContext(IO) {
                                db?.gameDao()?.insertGame(Game(id, title, imageIcon, consoleID, consoleName))
                            }
                        }
                    }
                    // skip displaying this console if it is empty and the user is hiding empty consoles
                    if (hideEmptyConsoles && reader.length() == 0) {
                        skippedConsoles.add(console)
                    } else {
                        parsedConsoles.add(console)
                    }
                    // check if every console has been parsed
                    if (skippedConsoles.size + parsedConsoles.size == consoleQueue.size) {
                        withContext(Main) {
                            _consoleList.value = parsedConsoles
                            _gameList.value = withContext(IO) { db?.gameDao()?.gameList }
                            loading.value = false
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse game list", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + ConsoleListViewModel::class.java.simpleName
        private var initialized = false
    }
}