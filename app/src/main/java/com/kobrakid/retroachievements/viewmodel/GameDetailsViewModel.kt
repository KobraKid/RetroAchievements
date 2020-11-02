package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.Game
import com.kobrakid.retroachievements.model.IGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameDetailsViewModel : ViewModel() {

    private val _game = MutableLiveData<IGame>()

    val game: LiveData<IGame> get() = _game

    fun getGameInfoForUser(user: String?, id: String?) {
        if (_game.value?.id?.isNotEmpty() == true) return
        CoroutineScope(IO).launch {
            Game.getGame(user, id) { game ->
                withContext(Main) {
                    _game.value = game
                }
            }
        }
    }
}