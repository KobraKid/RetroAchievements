package com.kobrakid.retroachievements.model

import android.util.Log
import com.github.mikephil.charting.data.Entry
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.regex.Pattern

class AchievementChart {
    companion object {
        suspend fun getAchievementDistributionChartForGame(id: String, callback: suspend (List<Entry>) -> Unit) {
            RetroAchievementsApi.getInstance().ScrapeGameInfoFromWeb(id) { parseAchievementDistribution(it, callback) }
        }

        private suspend fun parseAchievementDistribution(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (List<Entry>) -> Unit) {
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
                        callback(entries)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + AchievementChart::class.java.simpleName
    }
}