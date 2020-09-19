package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.User

class UserProfileViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    val username: String = savedStateHandle["username"]
            ?: throw IllegalArgumentException("missing username")
    val user: LiveData<User> = TODO()
}