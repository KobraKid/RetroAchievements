package com.kobrakid.retroachievements.model

import android.util.Log
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class LeaderboardList {
    companion object {
        suspend fun getLeaderboardList(gameId: String, update: suspend (Int, Int) -> Unit, callback: suspend (List<ILeaderboard>) -> Unit) {
            RetroAchievementsApi.getInstance().ScrapeGameInfoFromWeb(gameId) { parseLeaderboards(it, update, callback) }
        }

        private suspend fun parseLeaderboards(response: Pair<RetroAchievementsApi.RESPONSE, String>, update: suspend (Int, Int) -> Unit, callback: suspend (List<ILeaderboard>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.SCRAPE_GAME_PAGE -> {
                    withContext(Default) {
                        val lb = mutableListOf<ILeaderboard>()
                        Jsoup.parse(response.second).select("div[class=fixheightcellsmaller]").forEach { row ->
                            row.select("a[href^=/leaderboardinfo.php?i=]")?.first()?.let { heading ->
                                lb.add(Leaderboard(id = heading.attr("href").substring(23), title = heading.text()))
                            }
                        }
                        callback(lb)
                    }
                }
                RetroAchievementsApi.RESPONSE.GET_LEADERBOARDS -> {
                    // TOOD old code
                    withContext(Default) {
                        val lb = mutableListOf<ILeaderboard>()
                        val rows = Jsoup.parse(response.second).select("div[class=detaillist] > table > tbody > tr")
                        val max = (rows.size - 1).coerceAtLeast(0)
                        for ((i, row) in rows.withIndex()) {
                            update(i, max)
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
                        callback(lb)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + LeaderboardList::class.java.simpleName
    }
}