package com.kobrakid.retroachievements.model

import android.util.Log
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class ConsoleList {

    companion object {

        suspend fun getConsoles(hideEmptyConsoles: Boolean, callback: suspend (List<IConsole>) -> Unit) {
            // get consoles from db
            callback(
                    if (hideEmptyConsoles) RetroAchievementsDatabase.getInstance().consoleDao().nonEmptyConsoles
                    else RetroAchievementsDatabase.getInstance().consoleDao().consoleList)
            // update list from network
            CoroutineScope(IO).launch {
                RetroAchievementsApi.getInstance().GetConsoleIDs(ConsoleList::class.java) { parseConsoles(it, hideEmptyConsoles, callback) }
            }
        }

        private suspend fun parseConsoles(response: Pair<RetroAchievementsApi.RESPONSE, String>, hideEmptyConsoles: Boolean, callback: suspend (List<IConsole>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
                    try {
                        val reader = JSONArray(response.second)
                        val consoles = mutableListOf<IConsole>()
                        val hiddenConsoles = mutableListOf<IConsole>()
                        for (i in 0 until reader.length()) {
                            val id = reader.getJSONObject(i).getString("ID")
                            val name = reader.getJSONObject(i).getString(("Name"))
                            withContext(IO) {
                                RetroAchievementsDatabase.getInstance().consoleDao().insertConsole(Console(
                                        id, name, RetroAchievementsDatabase.getInstance().consoleDao().getConsoleWithID(id).let { if (it.isNotEmpty()) it[0].games else 0 }))
                                RetroAchievementsApi.getInstance().GetGameList(id) {
                                    parseGameList(it, id, name, consoles, hiddenConsoles, reader.length(), hideEmptyConsoles, callback)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse console IDs", e)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private suspend fun parseGameList(
                response: Pair<RetroAchievementsApi.RESPONSE, String>,
                consoleID: String,
                consoleName: String,
                consoles: MutableList<IConsole>,
                hiddenConsoles: MutableList<IConsole>,
                consoleCount: Int,
                hideEmptyConsoles: Boolean,
                callback: suspend (List<IConsole>) -> Unit) {
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
                                    RetroAchievementsDatabase.getInstance().gameDao().insertGame(Game(
                                            id = id,
                                            title = title,
                                            consoleID = consoleID,
                                            consoleName = consoleName,
                                            imageIcon = imageIcon))
                                }
                            }
                        }
                        withContext(IO) {
                            RetroAchievementsDatabase.getInstance().consoleDao().updateConsole(console)
                        }
                        // skip displaying this console if it is empty and the user is hiding empty consoles
                        if (hideEmptyConsoles && console.games == 0) {
                            hiddenConsoles.add(console)
                        } else {
                            consoles.add(console)
                        }
                        if (consoles.size + hiddenConsoles.size == consoleCount) {
                            callback(consoles)
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse game list", e)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + ConsoleList::class.java.simpleName
    }
}