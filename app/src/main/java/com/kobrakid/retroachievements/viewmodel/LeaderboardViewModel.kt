package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.ILeaderboard
import com.kobrakid.retroachievements.model.Leaderboard
import com.kobrakid.retroachievements.model.LeaderboardParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardViewModel : ViewModel() {

    private val _leaderboard = MutableLiveData<ILeaderboard>()
    private val _max = MutableLiveData<Int>()
    private val _progress = MutableLiveData<Int>()
    private val _participants = MutableLiveData<List<LeaderboardParticipant>>()
    private val _loading = MutableLiveData<Boolean>()

    val leaderboard: LiveData<ILeaderboard> get() = _leaderboard
    val participantCount: LiveData<Int> get() = _max
    val progress: LiveData<Int> get() = _progress
    val participants: LiveData<List<LeaderboardParticipant>> get() = _participants
    val loading: LiveData<Boolean> get() = _loading

    fun setLeaderboard(id: String) {
        _loading.value = true
        CoroutineScope(IO).launch {
            Leaderboard.getLeaderboard(id,
                    loadLeaderboard = {
                        withContext(Main) {
                            _leaderboard.value = it
                        }
                    },
                    update = { p, m ->
                        withContext(Main) {
                            _progress.value = p
                            _max.value = m
                        }
                    },
                    loadParticipants = {
                        withContext(Main) {
                            _participants.value = it
                            _loading.value = false
                        }
                    })
        }
    }
}