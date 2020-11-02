package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.GameList
import com.kobrakid.retroachievements.model.GameProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentGamesViewModel : ViewModel() {

    private var offset = 0
    private val _recentGames = MutableLiveData<List<GameProgress>>(mutableListOf())
    private val _loading = MutableLiveData<Boolean>()

    val recentGames: LiveData<List<GameProgress>> get() = _recentGames
    val loading: LiveData<Boolean> get() = _loading

    /**
     * Adds recent games to the view's recyclerview. Tries to catch the user reaching end of list
     * early and append games past the screen incrementally.
     *
     * @param scrollPosition The last visible item's index within the scrollview
     */
    fun getRecentGames(username: String?, itemCount: Int, scrollPosition: Int) {
        // Only get more games if there are none, or if the last request has completed and the user is scrolling near the end
        if ((scrollPosition == 0 && itemCount == 0) || (scrollPosition >= offset - 15 && itemCount == offset)) {
            _loading.value = true
            CoroutineScope(Dispatchers.IO).launch {
                offset = GameList.getNextRecentlyPlayedGames(username, offset) {
                    withContext(Main) {
                        _recentGames.value = (_recentGames.value as MutableList).apply { addAll(it) }
                        _loading.value = false
                    }
                }
            }
        }
    }

    fun onRefresh() {
        offset = 0
        (_recentGames.value as MutableList).clear()
    }
}