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

    val leaderboards: LiveData<List<Leaderboard>> = MutableLiveData()
    val loading: LiveData<Boolean> = MutableLiveData(false)
    val max: LiveData<Int> = MutableLiveData()
    val progress: LiveData<Int> = MutableLiveData()

    fun init(filename: String) {
        if (leaderboards.value?.isNotEmpty() == true) return
        (loading as MutableLiveData).value = true
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
                    withContext(Main) { (max as MutableLiveData).value = (rows.size - 1).coerceAtLeast(0) }
                    for ((i, row) in rows.withIndex()) {
                        withContext(Main) { (progress as MutableLiveData).value = i }
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
                        (leaderboards as MutableLiveData).value = lb
                        (loading as MutableLiveData).value = false
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