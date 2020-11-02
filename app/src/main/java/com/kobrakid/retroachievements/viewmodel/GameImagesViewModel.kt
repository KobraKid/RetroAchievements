package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameImagesViewModel : ViewModel() {

    private val _boxURL = MutableLiveData<String>()
    private val _titleURL = MutableLiveData<String>()
    private val _ingameURL = MutableLiveData<String>()

    val boxURL: LiveData<String> get() = _boxURL
    val titleURL: LiveData<String> get() = _titleURL
    val ingameURL: LiveData<String> get() = _ingameURL

    fun setId(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Game.getGame("", id) {
                withContext(Main) {
                    _boxURL.value = it.imageBoxArt
                    _titleURL.value = it.imageTitle
                    _ingameURL.value = it.imageIngame
                }
            }
        }
    }
}