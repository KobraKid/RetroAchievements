package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    private val _username = MutableLiveData<String>()
    private val _apiKey = MutableLiveData<String>()

    val username: LiveData<String> get() = _username
    val apiKey: LiveData<String> get() = _apiKey

    fun init(username: String, apiKey: String) {
        _username.value = trimQuotes(username)
        _apiKey.value = trimQuotes(apiKey)
    }

    private fun trimQuotes(s: String): String {
        return if (s.isEmpty() || s.isBlank() || s.indexOf("\"") < 0 || s.indexOf("\"") + 1 > s.lastIndexOf("\"")) s
        else s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""))
    }
}