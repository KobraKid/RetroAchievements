package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.ILeaderboard
import com.kobrakid.retroachievements.model.LeaderboardList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardListViewModel : ViewModel() {

    private val _leaderboards = MutableLiveData<List<ILeaderboard>>()
    private val _loading = MutableLiveData<Boolean>()
    private val _max = MutableLiveData<Int>()
    private val _progress = MutableLiveData<Int>()

    val leaderboards: LiveData<List<ILeaderboard>> get() = _leaderboards
    val loading: LiveData<Boolean> get() = _loading
    val max: LiveData<Int> get() = _max
    val progress: LiveData<Int> get() = _progress

    fun getLeaderboardsForGame(gameId: String) {
        if (leaderboards.value?.isNotEmpty() == true) return
        _loading.value = true
        CoroutineScope(IO).launch {
            LeaderboardList.getLeaderboardList(gameId, { p, m ->
                _progress.value = p
                _max.value = m
            }) {
                withContext(Main) {
                    _leaderboards.value = it
                    _loading.value = false
                }
            }
        }
    }
}