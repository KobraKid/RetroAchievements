package com.kobrakid.retroachievements.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.model.Comment
import com.kobrakid.retroachievements.model.CommentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameCommentsViewModel : ViewModel() {

    private val _comments = MutableLiveData<List<Comment>>()
    private val _loading = MutableLiveData<Boolean>()

    val comments: LiveData<List<Comment>> get() = _comments
    val loading: LiveData<Boolean> get() = _loading

    fun setId(id: String, forceReload: Boolean = false) {
        if (!forceReload && comments.value?.isNotEmpty() == true) return
        _loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            CommentList.getCommentsOnGame(id) {
                withContext(Main) {
                    _comments.value = it
                    _loading.value = false
                }
            }
        }
    }
}