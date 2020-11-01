package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class GameImagesViewModel : ViewModel() {

    private val _boxURL = MutableLiveData<String>()
    private val _titleURL = MutableLiveData<String>()
    private val _ingameURL = MutableLiveData<String>()

    val boxURL: LiveData<String> get() = _boxURL
    val titleURL: LiveData<String> get() = _titleURL
    val ingameURL: LiveData<String> get() = _ingameURL

    fun setId(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.getInstance().GetGame(id) { parseGameImages(it) }
        }
    }

    private suspend fun parseGameImages(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME -> {
                withContext(Default) {
                    var box = ""
                    var title = ""
                    var ingame = ""
                    try {
                        val reader = JSONObject(response.second)
                        box = reader.getString("ImageBoxArt").let { if (it.contains("000002.png")) "" else it }
                        title = reader.getString("ImageTitle").let { if (it.contains("000002.png")) "" else it }
                        ingame = reader.getString("ImageIngame").let { if (it.contains("000002.png")) "" else it }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse game images", e)
                    } finally {
                        withContext(Main) {
                            _boxURL.value = box
                            _titleURL.value = title
                            _ingameURL.value = ingame
                        }
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + GameImagesViewModel::class.java.simpleName
    }
}