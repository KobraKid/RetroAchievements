package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserSummaryViewModel : ViewModel() {

    private val _userState = MutableLiveData<User>()

    val userState: LiveData<User> get() = _userState

    fun setUsername(username: String) {
        CoroutineScope(Dispatchers.IO).launch {
            User.getUser(username) {
                withContext(Main) {
                    _userState.value = it
                }
            }
        }
    }
}