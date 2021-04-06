package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.IAchievement
import com.kobrakid.retroachievements.model.IGame
import com.kobrakid.retroachievements.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserSummaryViewModel : ViewModel() {

    private val _userState = MutableLiveData<User>()
    private val _recentGames = MutableLiveData<List<IGame>>()
    private val _recentAchievements = MutableLiveData<Map<String, List<IAchievement>>>()

    val userState: LiveData<User> get() = _userState
    val recentGames: LiveData<List<IGame>> get() = _recentGames
    val recentAchievements: LiveData<Map<String, List<IAchievement>>> get() = _recentAchievements

    fun setUsername(username: String) {
        CoroutineScope(IO).launch {
            User.getUser(username, 5) { user, gameList, achievementList ->
                withContext(Main) {
                    _userState.value = user
                    _recentGames.value = gameList
                    _recentAchievements.value = achievementList
                }
            }
        }
    }
}