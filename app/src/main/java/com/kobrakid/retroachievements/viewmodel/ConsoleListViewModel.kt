package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import com.kobrakid.retroachievements.model.ConsoleList
import com.kobrakid.retroachievements.model.IConsole
import com.kobrakid.retroachievements.model.IGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConsoleListViewModel : ViewModel() {

    private var hideEmptyConsoles = false
    private val _consoleList = MutableLiveData<List<IConsole>>()
    private val _gameSuggestions = MutableLiveData<List<IGame>>()
    private val _loading = MutableLiveData<Boolean>()

    val consoleList: LiveData<List<IConsole>> get() = _consoleList
    val gameSuggestions: LiveData<List<IGame>> get() = _gameSuggestions
    val loading: LiveData<Boolean> get() = _loading

    fun getConsoles(hideEmptyConsoles: Boolean = false, forceRefresh: Boolean = false) {
        // Prevent re-initialization
        if (!forceRefresh && consoleList.value?.isNotEmpty() == true && this.hideEmptyConsoles == hideEmptyConsoles) return

        // Check to see if empty consoles should be hidden
        this.hideEmptyConsoles = hideEmptyConsoles

        _loading.value = true
        CoroutineScope(IO).launch {
            ConsoleList.getConsoles(hideEmptyConsoles) {
                withContext(Main) {
                    _consoleList.value = it
                    _loading.value = false
                }
            }
        }
    }

    fun getGameSuggestions() {
        CoroutineScope(IO).launch {
            val gameList = RetroAchievementsDatabase.getInstance().gameDao().gameList
            withContext(Main) {
                _gameSuggestions.value = gameList
            }
        }
    }
}