package com.kobrakid.retroachievements.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.Console
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import com.kobrakid.retroachievements.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class SettingsViewModel : ViewModel() {

    private var sharedPrefs: SharedPreferences? = null
    private var selectedTheme: Int = 0
    private val _settings = MutableLiveData<Settings>()
    private val _loading = MutableLiveData<Boolean>()
    private val _activityNeedsRecreating = MutableLiveData<Boolean>()

    val settings: LiveData<Settings> get() = _settings
    val activityNeedsRecreating: LiveData<Boolean> get() = _activityNeedsRecreating
    val loading: LiveData<Boolean> get() = _loading

    private var counter = 0

    fun init(context: Context?) {
        context?.let {
            sharedPrefs = it.getSharedPreferences(it.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
            getSettings(it)
        }
    }

    private fun getSettings(context: Context) {
        _settings.value = Settings().apply {
            theme = sharedPrefs?.getInt(context.getString(R.string.theme_setting), R.style.BlankTheme)
                    ?: 0
            user = sharedPrefs?.getString(context.getString(R.string.ra_user), "")
                    ?: ""
            hideEmptyConsoles = sharedPrefs?.getBoolean(context.getString(R.string.empty_console_hide_setting), false)
                    ?: false
            hideEmptyGames = sharedPrefs?.getBoolean(context.getString(R.string.empty_game_hide_setting), false)
                    ?: false
        }
    }

    fun onRecreate() {
        _activityNeedsRecreating.value = false
    }

    fun setTheme(theme: Int) {
        if (selectedTheme != Consts.Theme.values()[theme].themeAttr) {
            selectedTheme = Consts.Theme.values()[theme].themeAttr
        }
    }

    fun applyTheme(context: Context?) {
        if (selectedTheme != _settings.value?.theme) {
            context?.let {
                sharedPrefs?.edit()?.putInt(it.getString(R.string.theme_setting), this.selectedTheme)?.apply()
                _settings.value = Settings(_settings.value).apply { theme = selectedTheme }
            }
            _activityNeedsRecreating.value = true
        }
    }

    fun setHideConsoles(context: Context?, hide: Boolean) {
        if (hide != _settings.value?.hideEmptyConsoles) {
            context?.let { ctx ->
                sharedPrefs?.edit()?.putBoolean(ctx.getString(R.string.empty_console_hide_setting), hide)?.apply()
                _settings.value = Settings(_settings.value).apply { hideEmptyConsoles = hide }
                if (hide) {
                    _loading.value = true
                    CoroutineScope(IO).launch {
                        RetroAchievementsDatabase.getInstance().consoleDao().clearTable()
                        Log.d(TAG, "Clearing console table")
                        RetroAchievementsApi.getInstance().GetConsoleIDs { removeConsoles(it) }
                    }
                }
            }
        }
    }

    fun setHideGames(context: Context?, hide: Boolean) {
        context?.let {
            sharedPrefs?.edit()?.putBoolean(it.getString(R.string.empty_game_hide_setting), hide)?.apply()
            _settings.value = Settings(_settings.value).apply { hideEmptyGames = hide }
        }
    }

    fun logout(context: Context?) {
        context?.let {
            sharedPrefs?.edit()?.putString(it.getString(R.string.ra_user), "")?.apply()
            _settings.value = Settings(_settings.value).apply { user = "" }
            _activityNeedsRecreating.value = true
        }
    }

    private suspend fun removeConsoles(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
                try {
                    val reader = JSONArray(response.second)
                    counter = reader.length()
                    for (i in 0 until reader.length()) {
                        withContext(IO) {
                            // Set each console to have 0 games
                            RetroAchievementsDatabase.getInstance().consoleDao().insertConsole(
                                    Console(
                                            reader.getJSONObject(i).getString("ID"),
                                            reader.getJSONObject(i).getString("Name")))
                            RetroAchievementsApi.getInstance().GetGameList(reader.getJSONObject(i).getString("ID")) { removeConsoles(it) }
                        }
                        Log.v(TAG, "calling api on console $i")
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse console IDs", e)
                }
            }
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    if (reader.length() > 0) {
                        withContext(IO) {
                            // Only update a console if it has more than 0 games
                            val id = reader.getJSONObject(0).getString("ConsoleID")
                            val name = reader.getJSONObject(0).getString("ConsoleName")
                            RetroAchievementsDatabase.getInstance().consoleDao().updateConsole(Console(id, name, reader.length()))
                            Log.v(TAG, "Updating console $id ($name): ${reader.length()} games")
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse game list", e)
                } finally {
                    counter--
                }
                if (counter == 0) withContext(Main) { _loading.value = false }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + SettingsViewModel::class.java.simpleName
    }
}