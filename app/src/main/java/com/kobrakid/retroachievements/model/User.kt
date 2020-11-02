package com.kobrakid.retroachievements.model

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

data class User(
        var username: String = "",
        var rank: String = "",
        var motto: String = "",
        var totalPoints: String = "",
        var totalTruePoints: String = "",
        var memberSince: String = "",
        var userPic: String = Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + username + ".png"
) {
    constructor(copy: User?) : this(
            copy?.username ?: "",
            copy?.rank ?: "",
            copy?.motto ?: "",
            copy?.totalPoints ?: "",
            copy?.totalTruePoints ?: "",
            copy?.memberSince ?: "",
            copy?.userPic ?: ""
    )

    val retroRatio: String
        get() = String.format("%.2f",
                if (totalPoints.isNotEmpty()
                        && totalPoints.isDigitsOnly()
                        && totalTruePoints.isNotEmpty()
                        && totalTruePoints.isDigitsOnly())
                    totalTruePoints.toFloat().div(totalPoints.toFloat()) else 0f)

    companion object {
        suspend fun getUser(username: String?, recentGames: Int = 0, callback: suspend (User) -> Unit) {
            if (username?.isNotEmpty() == true) {
                RetroAchievementsApi.getInstance().GetUserSummary(username, recentGames) { parseUserSummary(username, it, callback) }
            } else {
                callback(User())
            }
        }

        private suspend fun parseUserSummary(username: String, response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (User) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                    withContext(Default) {
                        var user = User()
                        try {
                            val reader = JSONObject(response.second)
                            val motto = "\"${reader.getString("Motto")}\""
                            user = User(
                                    username = username,
                                    rank = reader.getString("Rank"),
                                    motto = if (motto.length > 2) motto else "",
                                    totalPoints = reader.getString("TotalPoints"),
                                    totalTruePoints = reader.getString("TotalTruePoints"),
                                    memberSince = reader.getString("MemberSince"),
                                    userPic = Consts.BASE_URL + "/" + reader.getString("UserPic"))
                        } catch (e: JSONException) {
                            Log.e(TAG, "Unable to parse user summary", e)
                        } finally {
                            callback(user)
                        }
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        suspend fun getUserRankAndScore(username: String?, callback: suspend (String, String) -> Unit) {
            if (username?.isNotEmpty() == true) {
                RetroAchievementsApi.getInstance().GetUserRankAndScore(username) { parseRankScore(it, callback) }
            } else {
                callback("", "")
            }
        }

        private suspend fun parseRankScore(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (String, String) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_USER_RANK_AND_SCORE -> {
                    withContext(Default) {
                        var rank = ""
                        var score = ""
                        try {
                            val reader = JSONObject(response.second)
                            rank = reader.getString("Rank")
                            score = reader.getString("Score")
                        } catch (e: JSONException) {
                            Log.e(TAG, "Couldn't parse user rank/score: ${response.second}", e)
                        } finally {
                            callback(rank, score)
                        }
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        suspend fun getUserMasteries(username: String?, callback: suspend (List<IGame>) -> Unit) {
            callback(RetroAchievementsDatabase.getInstance().gameDao().masteredGames)
            if (username?.isNotEmpty() == true) {
                RetroAchievementsApi.getInstance().GetUserWebProfile(username) { parseUserWebProfile(it, callback) }
            }
        }

        private suspend fun parseUserWebProfile(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (List<IGame>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_USER_WEB_PROFILE -> {
                    withContext(Default) {
                        val masteries = mutableListOf<IGame>()
                        Jsoup.parse(response.second).select("div[class=trophyimage]").forEach {
                            val gameID = it.selectFirst("a[href]").attr("href")
                            if (gameID.length >= 6 && gameID.substring(6).isDigitsOnly()) {
                                val image = it.selectFirst("img[src]")
                                masteries.add(Game(
                                        id = gameID.substring(6),
                                        imageIcon = image.attr("src")))
                            }
                        }
                        callback(masteries)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        suspend fun getUserRecentGames(username: String?, callback: suspend (List<GameProgress>) -> Unit) {
            if (username?.isNotEmpty() == true) {
                RetroAchievementsApi.getInstance().GetUserSummary(username, NUM_RECENT_GAMES) { parseUserRecentGames(it, callback) }
            }
        }

        private suspend fun parseUserRecentGames(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (List<GameProgress>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                    withContext(Default) {
                        val recent = mutableListOf<GameProgress>()
                        try {
                            val reader = JSONObject(response.second)
                            val recentlyPlayed = reader.getJSONArray("RecentlyPlayed")
                            for (i in 0 until recentlyPlayed.length()) {
                                val game = recentlyPlayed.getJSONObject(i)
                                val awards = reader.getJSONObject("Awarded").getJSONObject(game.getString("GameID"))
                                val awarded = awards.getString("NumAchieved")
                                val awardedHC = awards.getString("NumAchievedHardcore")
                                val achievements = awards.getString("NumPossibleAchievements")
                                val pts = awards.getString("PossibleScore")
                                val earnedPts =
                                        if (awarded.let { if (it.isDigitsOnly()) it.toInt() else 0 } > awardedHC.let { if (it.isDigitsOnly()) it.toInt() else 0 })
                                            awards.getString("ScoreAchieved")
                                        else
                                            awards.getString("ScoreAchievedHardcore")
                                recent.add(GameProgress(
                                        id = game.getString("GameID"),
                                        title = game.getString("Title"),
                                        imageIcon = game.getString("ImageIcon"),
                                        numAchievements = if (achievements.isDigitsOnly()) achievements.toInt() else 0,
                                        numAwardedToUser = if (awarded.isDigitsOnly()) awarded.toInt() else 0,
                                        numAwardedToUserHardcore = if (awardedHC.isDigitsOnly()) awardedHC.toInt() else 0,
                                        totalPoints = if (pts.isDigitsOnly()) pts.toInt() else 0,
                                        earnedPoints = if (earnedPts.isDigitsOnly()) earnedPts.toInt() else 0))
                            }
                        } catch (e: JSONException) {
                            Log.e(TAG, response.second, e)
                        } finally {
                            callback(recent)
                        }
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        suspend fun getTopUsers(currentUser: User?, callback: suspend (List<User>) -> Unit) {
            if (currentUser?.username?.isNotEmpty() == true) {
                RetroAchievementsApi.getInstance().GetTopTenUsers { parseTopTenUsers(currentUser, it, callback) }
            } else {
                callback(listOf())
            }
        }

        private suspend fun parseTopTenUsers(
                currentUser: User,
                response: Pair<RetroAchievementsApi.RESPONSE, String>,
                callback: suspend (List<User>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_TOP_TEN_USERS -> {
                    withContext(Default) {
                        val users = mutableListOf<User>()
                        try {
                            val reader = JSONArray(response.second)
                            for (i in 0 until reader.length()) {
                                users.add(User(
                                        rank = (i + 1).toString(),
                                        username = (reader[i] as JSONObject).getString("1"),
                                        totalPoints = (reader[i] as JSONObject).getString("2"),
                                        totalTruePoints = (reader[i] as JSONObject).getString("3")))
                            }

                        } catch (e: JSONException) {
                            Log.e(TAG, "Couldn't parse top ten users", e)
                        } finally {
                            if (!users.contains(currentUser)) {
                                users.add(currentUser)
                            }
                            callback(users)
                        }
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + User::class.java.simpleName
        private const val NUM_RECENT_GAMES = 5
    }
}