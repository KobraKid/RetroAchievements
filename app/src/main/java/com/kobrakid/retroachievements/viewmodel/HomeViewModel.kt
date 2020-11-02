package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.GameProgress
import com.kobrakid.retroachievements.model.IGame
import com.kobrakid.retroachievements.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _masteries = MutableLiveData<List<IGame>>()
    private val _recentGames = MutableLiveData<List<GameProgress>>()

    val masteries: LiveData<List<IGame>> get() = _masteries
    val recentGames: LiveData<List<GameProgress>> get() = _recentGames

    fun setUser(username: String?) {
        CoroutineScope(IO).launch {
            User.getUserMasteries(username) {
                withContext(Main) {
                    _masteries.value = it
                }
            }
        }
        CoroutineScope(IO).launch {
            User.getUserRecentGames(username) {
                withContext(Main) {
                    _recentGames.value = it
                }
            }
        }
    }
}