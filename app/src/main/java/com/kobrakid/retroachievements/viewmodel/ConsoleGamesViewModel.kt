package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.GameList
import com.kobrakid.retroachievements.model.IGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConsoleGamesViewModel : ViewModel() {

    private val _consoleGamesList = MutableLiveData<List<IGame>>()
    private val _loading = MutableLiveData<Boolean>()

    val consoleGamesList: LiveData<List<IGame>> get() = _consoleGamesList
    val loading: LiveData<Boolean> get() = _loading

    suspend fun setConsoleID(id: String?, forceRefresh: Boolean = false) {
        if (!forceRefresh && _consoleGamesList.value?.isNotEmpty() == true) return
        _loading.value = true
        CoroutineScope(IO).launch {
            GameList.getGamesForConsole(id) {
                withContext(Main) {
                    _consoleGamesList.value = it
                    _loading.value = false
                }
            }
        }
    }
}