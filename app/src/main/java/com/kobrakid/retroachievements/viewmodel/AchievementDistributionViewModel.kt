package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.regex.Pattern

class AchievementDistributionViewModel : ViewModel() {

    private val _chartData = MutableLiveData<List<Entry>>()

    val chartData: LiveData<List<Entry>> get() = _chartData

    fun setId(id: String) {
        CoroutineScope(IO).launch {
            RetroAchievementsApi.getInstance().ScrapeGameInfoFromWeb(id) { parseAchievementDistribution(it) }
        }
    }

    private suspend fun parseAchievementDistribution(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.SCRAPE_GAME_PAGE -> {
                withContext(Dispatchers.Default) {
                    val entries = mutableListOf<Entry>()
                    Jsoup.parse(response.second)
                            .getElementsByTag("script")
                            .filter { it.html().startsWith("google.load('visualization'") }
                            .forEach {
                                val m1 = Pattern.compile("v:(\\d+),").matcher(it.dataNodes()[0].wholeData)
                                val m2 = Pattern.compile(",\\s(\\d+)\\s]").matcher(it.dataNodes()[0].wholeData)
                                while (m1.find() && m2.find()) {
                                    entries.add(Entry(
                                            m1.group(1)?.toFloat() ?: 0f,
                                            m2.group(1)?.toFloat() ?: 0f))
                                }
                            }
                    withContext(Main) {
                        _chartData.value = entries
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + AchievementDistributionViewModel::class.java.simpleName
    }
}