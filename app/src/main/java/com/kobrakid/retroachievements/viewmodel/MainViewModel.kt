package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()

    val user: LiveData<User> get() = _user

    fun setUsername(username: String?) {
        _user.value = User(username ?: "")
        CoroutineScope(IO).launch {
            User.getUserRankAndScore(username) { r, s ->
                withContext(Main) {
                    _user.value = User(_user.value).apply {
                        rank = r
                        totalPoints = s
                    }
                }
            }
        }
    }
}