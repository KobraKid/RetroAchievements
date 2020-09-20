package com.kobrakid.retroachievements.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.Game
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import com.kobrakid.retroachievements.model.Console
import com.kobrakid.retroachievements.view.ui.ConsoleListFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.io.File

class ConsoleListViewModel : ViewModel() {

    private val _consoleList = MutableLiveData<List<Console>>().apply { value = listOf() }
    val consoleList: LiveData<List<Console>> get() = _consoleList
    private val _gameList = MutableLiveData<List<ConsoleListFragment.GameSuggestion>>().apply { value = listOf() }
    val gameList: LiveData<List<ConsoleListFragment.GameSuggestion>> get() = _gameList
    val loading = MutableLiveData(true)
    var gameSearch = ""

    private var hideEmptyConsoles = false
    private var gameListFileIsPopulated = false

    fun init(context: Context?) {
        // Check to see if empty consoles should be hidden
        hideEmptyConsoles = context
                ?.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                ?.getBoolean(context.getString(R.string.empty_console_hide_setting), false)
                ?: false

        // Check to see if the game list file exists & is populated
        gameListFileIsPopulated = if (File(context?.filesDir, "RALIST").exists())
            (context?.openFileInput("RALIST")?.bufferedReader()?.readLine()
                    ?: "").isNotEmpty() else false

//        if (gameListFileIsPopulated) { // If we already have a populated file, load from it
//            populateListsFromFile(context)
//        } else { // Otherwise, get each console and extract the list of games from them
        CoroutineScope(IO).launch {
            context?.let {
                RetroAchievementsDatabase.getInstance(it)?.consoleDao()?.clearTable()
                RetroAchievementsDatabase.getInstance(it)?.gameDao()?.clearTable()
            }
            RetroAchievementsApi.GetConsoleIDs(context) { parseConsoles(context, it) }
        }
//        }
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
                        withContext(IO) {
                            db?.consoleDao()?.insertConsole(com.kobrakid.retroachievements.database.Console(id, name))
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
                    val games = mutableListOf<ConsoleListFragment.GameSuggestion>()
//                    Log.i(TAG, "Parsing games for console $consoleName")
                    for (i in 0 until reader.length()) {
                        reader.getJSONObject(i).let {
                            val id = it.getString("ID")
                            val title = it.getString("Title")
                            val imageIcon = it.getString("ImageIcon")
                            games.add(ConsoleListFragment.GameSuggestion(id, title, consoleName))
                            withContext(IO) {
                                db?.gameDao()?.insertGame(Game(id, title, imageIcon, consoleID, consoleName))
                            }
                        }
                    }
                    // skip displaying this console if it is empty and the user is hiding empty consoles
                    if (hideEmptyConsoles && reader.length() == 0) return
                    withContext(Main) {
                        _consoleList.value = _consoleList.value?.plus(Console(consoleID, consoleName))
                        _gameList.value = _gameList.value?.plus(games)
                        loading.value = false
                    }
//                    Log.i(TAG, "Added console $consoleName to the consoleList ${consoleList.value}")
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse game list", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

//    private suspend fun parseConsoles(context: Context?, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
//        when (response.first) {
//            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
//            RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
//                try {
//                    val reader = JSONArray(response.second)
//
//                    // Loop once to add all consoles to view
//                    for (i in 0 until reader.length()) {
//                        parseGamesFromConsole(context, reader.getJSONObject(i).getString("ID"))
//                        consoleList.value?.add(Console(reader.getJSONObject(i).getString("ID"), reader.getJSONObject(i).getString("Name")))
//                    }
//                    // Loop twice if we wish to hide empty consoles
//                    if (hideEmptyConsoles) {
//                        val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
//                        db?.let {
//                            for (i in 0 until reader.length()) {
//                                val id = reader.getJSONObject(i).getString("ID")
//                                val name = reader.getJSONObject(i).getString("Name")
//                                parseConsoleHelper(it, id, name)
//                            }
//                        }
//                    }
//                } catch (e: JSONException) {
//                    Log.e(TAG, "Couldn't parse console IDs", e)
//                }
//            }
//            else -> Log.v(TAG, "${response.first}: ${response.second}")
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    private suspend fun parseConsoleHelper(db: RetroAchievementsDatabase, id: String, name: String) {
//        withContext(IO) {
//            val current = db.consoleDao()?.getConsoleWithID(id.toInt())
//            if (current?.isNotEmpty() == true && current[0]?.gameCount == 0)
//                consoleList.value?.removeIf { it.name == name }
//            loading.value = false
//        }
//    }
//
//    /**
//     * Fire off an API call to get the list of games for this console
//     *
//     * @param console The console to count
//     */
//    private suspend fun parseGamesFromConsole(context: Context?, console: String) {
//        withContext(IO) {
//            RetroAchievementsApi.GetGameList(context, console) { updateGameListFile(context, it) }
//        }
//    }
//
//    /**
//     * Populate the game list file with games from each console
//     *
//     * @param response
//     */
//    private fun updateGameListFile(context: Context?, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
//        when (response.first) {
//            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
//            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
//                try {
//                    val reader = JSONArray(response.second)
//                    if (reader.length() == 0) return
//                    context?.openFileOutput("RALIST", Context.MODE_APPEND).use {
//                        val console = reader.getJSONObject(0).getString("ConsoleName")
//                        it?.write(("\u0001$console\n").toByteArray())
//                        for (i in 0 until reader.length()) {
//                            val title = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                                Html.fromHtml(reader.getJSONObject(i).getString("Title"), Html.FROM_HTML_MODE_COMPACT).toString()
//                            } else {
//                                reader.getJSONObject(i).getString("Title")
//                            }
//                            val id = reader.getJSONObject(i).getString("ID")
//                            it?.write(("$id\u0002$title\n").toByteArray())
//                            gameList.value?.add(ConsoleListFragment.GameSuggestion(id, title, console))
//                        }
//                    }
//                } catch (e: JSONException) {
//                    Log.e(TAG, "Couldn't parse game list", e)
//                }
//            }
//            else -> Log.v(TAG, "${response.first}: ${response.second}")
//        }
//    }
//
//    private fun populateListsFromFile(context: Context?) {
//        var console = ""
//        context?.openFileInput("RALIST")?.bufferedReader()?.forEachLine { line ->
//            if (line.first() == '\u0001')
//                console = line.substring(1)
//            else
//                gameList.value?.add(ConsoleListFragment.GameSuggestion(
//                        line.substringBefore('\u0002'),
//                        line.substringAfter('\u0002'),
//                        console))
//        }
//    }

    companion object {
        private val TAG = Consts.BASE_TAG + ConsoleListViewModel::class.java.simpleName
    }
}