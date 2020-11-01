package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Leaderboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class LeaderboardListViewModel : ViewModel() {

    private val _leaderboards = MutableLiveData<List<Leaderboard>>()
    private val _loading = MutableLiveData<Boolean>()
    private val _max = MutableLiveData<Int>()
    private val _progress = MutableLiveData<Int>()

    val leaderboards: LiveData<List<Leaderboard>> get() = _leaderboards
    val loading: LiveData<Boolean> get() = _loading
    val max: LiveData<Int> get() = _max
    val progress: LiveData<Int> get() = _progress

    fun init(filename: String) {
        if (leaderboards.value?.isNotEmpty() == true) return
        _loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.getInstance().GetLeaderboards(filename, true) { parseLeaderboards(it) }
        }
    }

    private suspend fun parseLeaderboards(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_LEADERBOARDS -> {
                withContext(Default) {
                    val lb = mutableListOf<Leaderboard>()
                    val rows = Jsoup.parse(response.second).select("div[class=detaillist] > table > tbody > tr")
                    withContext(Main) { _max.value = (rows.size - 1).coerceAtLeast(0) }
                    for ((i, row) in rows.withIndex()) {
                        withContext(Main) { _progress.value = i }
                        // Skip the header row
                        if (i == 0) continue
                        // Helpers
                        val attr = row.select("td")[1].selectFirst("div").attr("onmouseover")
                        val start = attr.indexOf("<br>") + 5
                        val end = attr.indexOf("</div>") - 1
                        // Passed values
                        val console = if (start >= 0 && start < attr.length && end > start) attr.substring(start, end) else ""
                        if (console.isEmpty()) continue
                        val id = row.select("td")[0].text()
                        val image = row.select("td")[1].selectFirst("img").attr("src")
                        val game = attr.substring(attr.indexOf("<b>") + 3, attr.indexOf("</b>")) // TODO: remove escape chars
                        val title = row.select("td")[3].text()
                        val description = row.select("td")[4].text()
                        val type = row.select("td")[5].text()
                        val numResults = row.select("td")[6].text()
                        lb.add(Leaderboard(id, image, game, console, title, description, type, numResults))
                    }
                    withContext(Main) {
                        _leaderboards.value = lb
                        _loading.value = false
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + LeaderboardListViewModel::class.java.simpleName
    }
}