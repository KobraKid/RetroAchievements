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

    //    private val _consoleList =
    val consoleList: LiveData<List<Console?>> = MutableLiveData()
    val gameList: LiveData<List<Game?>> = MutableLiveData()
    val loading = MutableLiveData(true)

    private var hideEmptyConsoles = false

    // Keep track of each console that needs to be parsed
    private var consoleQueue = Collections.synchronizedList(mutableListOf<String>())

    // Keep track of each console that has been parsed
    private var parsedConsoles = Collections.synchronizedList(mutableListOf<Console>())

    // Keep track of each console that has been parsed and skipped (empty)
    private var skippedConsoles = Collections.synchronizedList(mutableListOf<Console>())

    suspend fun init(context: Context?, forceRefresh: Boolean = false) {
        // Check to see if empty consoles should be hidden
        hideEmptyConsoles = context
                ?.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                ?.getBoolean(context.getString(R.string.empty_console_hide_setting), false)
                ?: false

        // Prevent re-initialization
        if (!forceRefresh && consoleList.value?.isNotEmpty() == true) {
            loading.value = false
            return
        }

        // Check if database is aleady populated
        loading.value = true
        val db = RetroAchievementsDatabase.getInstance()
        if (!forceRefresh && withContext(IO) { db.consoleDao().consoleList.isNotEmpty() }) {
            (consoleList as MutableLiveData).value = withContext(IO) {
                if (hideEmptyConsoles) db.consoleDao().nonEmptyConsoles
                else db.consoleDao().consoleList
            }
            (gameList as MutableLiveData).value = withContext(IO) { db.gameDao().gameList }
            loading.value = false
        } else {
            CoroutineScope(IO).launch {
                db.consoleDao().clearTable()
                db.gameDao().clearTable()
                RetroAchievementsApi.getInstance().GetConsoleIDs { parseConsoles(it) }
            }
        }
    }

    private suspend fun parseConsoles(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
                try {
                    val reader = JSONArray(response.second)
                    for (i in 0 until reader.length()) {
                        val id = reader.getJSONObject(i).getString("ID")
                        val name = reader.getJSONObject(i).getString(("Name"))
                        consoleQueue.add(name)
                        withContext(IO) {
                            RetroAchievementsDatabase.getInstance().consoleDao().insertConsole(Console(id, name))
                            RetroAchievementsApi.getInstance().GetGameList(id) { parseGameList(id, name, it) }
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse console IDs", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private suspend fun parseGameList(consoleID: String, consoleName: String, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    val console = Console(consoleID, consoleName, reader.length())
                    for (i in 0 until reader.length()) {
                        reader.getJSONObject(i).let {
                            val id = it.getString("ID")
                            val title = it.getString("Title")
                            val imageIcon = it.getString("ImageIcon")
                            withContext(IO) {
                                RetroAchievementsDatabase.getInstance().gameDao().insertGame(Game(id, title, consoleID, consoleName, imageIcon))
                            }
                        }
                    }
                    withContext(IO) {
                        RetroAchievementsDatabase.getInstance().consoleDao().updateConsole(console)
                    }
                    // skip displaying this console if it is empty and the user is hiding empty consoles
                    if (hideEmptyConsoles && console.games == 0) {
                        skippedConsoles.add(console)
                    } else {
                        parsedConsoles.add(console)
                    }
                    // check if every console has been parsed
                    if (skippedConsoles.size + parsedConsoles.size == consoleQueue.size) {
                        withContext(Main) {
                            (consoleList as MutableLiveData).value = parsedConsoles
                            (gameList as MutableLiveData).value = withContext(IO) { RetroAchievementsDatabase.getInstance().gameDao().gameList }
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
    }
}