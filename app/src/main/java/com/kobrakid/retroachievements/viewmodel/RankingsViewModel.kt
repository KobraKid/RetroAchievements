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

class RankingsViewModel : ViewModel() {

    private val _users = MutableLiveData<List<User>>()

    val users: LiveData<List<User>> get() = _users

    fun getTopUsers(currentUser: User?) {
        if (_users.value?.isNotEmpty() == true) return
        CoroutineScope(Dispatchers.IO).launch {
            User.getTopUsers(currentUser) {
                withContext(Main) {
                    _users.value = it
                }
            }
        }

    }
}