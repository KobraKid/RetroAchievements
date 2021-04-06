package com.kobrakid.retroachievements.model

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

@Entity(tableName = "leaderboard")
data class Leaderboard(
        @field:ColumnInfo @field:PrimaryKey override var id: String = "0",
        @field:ColumnInfo override var gameId: String = "",
        @field:ColumnInfo override var icon: String = "",
        @field:ColumnInfo override var console: String = "",
        @field:ColumnInfo override var title: String = "",
        @field:ColumnInfo override var description: String = "",
        @field:ColumnInfo override var type: String = "",
        @field:ColumnInfo override var numResults: String = "0") : ILeaderboard {

    override fun toString(): String {
        return "#$id: $title ($gameId - $console)"
    }

    companion object {
        suspend fun getLeaderboard(
                id: String,
                loadLeaderboard: suspend (ILeaderboard) -> Unit,
                update: suspend (Int, Int) -> Unit,
                loadParticipants: suspend (List<LeaderboardParticipant>) -> Unit) {
            RetroAchievementsDatabase.getInstance().leaderboardDao().getLeaderboardWithID(id).let {
                if (it.size == 1) {
                    loadLeaderboard(it[0])
                }
            }
            RetroAchievementsApi.getInstance().GetLeaderboard(id, "") { parseLeaderboard(id, it, loadLeaderboard, update, loadParticipants) }
        }

        private suspend fun parseLeaderboard(
                id: String,
                response: Pair<RetroAchievementsApi.RESPONSE, String>,
                loadLeaderboard: suspend (ILeaderboard) -> Unit,
                update: suspend (Int, Int) -> Unit,
                loadParticipants: suspend (List<LeaderboardParticipant>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_LEADERBOARD -> {
                    withContext(Default) {
                        val leaderboard = Leaderboard(id = id)
                        val users = mutableListOf<LeaderboardParticipant>()
                        val document = Jsoup.parse(response.second)
                        val userData = document.select("td.lb_user")
                        val resultData = document.select("td.lb_result")
                        val dateData = document.select("td.lb_date")
                        update(0, userData.size)
                        for (i in userData.indices) {
                            update(i, userData.size)
                            users.add(LeaderboardParticipant(
                                    userData[i].text(),
                                    resultData[i].text(),
                                    dateData[i].text()))
                        }
                        leaderboard.apply {
                            val gameInfo = document.select("div.navpath")
                            gameId = gameInfo.select("a[href^=/game/]").first().attr("href").substring(6)
                            icon = document.select("img.badgeimg").first().attr("src").let { it.substring(it.lastIndexOf("/") + 1, it.lastIndexOf(".")) }
                            console = gameInfo.select("a[href^=/gameList.php?c=]").first().attr("href").substring(16)
                            title = document.select("h3.longheader").first().text().substring(13)
                            description = document.select("div.larger").first().text()
                            type = "Score" // TODO: Can't really get the type reliably anymore
                        }
                        withContext(IO) {
                            RetroAchievementsDatabase.getInstance().leaderboardDao().insertLeaderboard(leaderboard)
                        }
                        loadLeaderboard(leaderboard)
                        loadParticipants(users)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + Leaderboard::class.java.simpleName
    }
}