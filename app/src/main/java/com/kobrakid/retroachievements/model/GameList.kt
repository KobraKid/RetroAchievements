package com.kobrakid.retroachievements.model

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class GameList {

    companion object {
        suspend fun getGamesForConsole(id: String?, callback: suspend (List<IGame>) -> Unit) {
            if (id?.isNotEmpty() == true) {
                // get list from db
                callback(RetroAchievementsDatabase.getInstance().gameDao().getGamesFromConsoleByID(id))
                // update list from network
                RetroAchievementsApi.getInstance().GetGameList(id) { parseGameList(it, callback) }
            } else {
                callback(listOf())
            }
        }

        private suspend fun parseGameList(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (List<IGame>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                    val games = mutableListOf<IGame>()
                    try {
                        val reader = JSONArray(response.second)
                        val db = RetroAchievementsDatabase.getInstance()
                        for (i in 0 until reader.length()) {
                            val game = Game.convertJsonStringToModel(reader.getJSONObject(i))
                            withContext(IO) { db.gameDao().insertGame(game) }
                            games.add(game)
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse game list", e)
                    } finally {
                        callback(games)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        suspend fun getNextRecentlyPlayedGames(username: String?, offset: Int, callback: suspend (List<GameProgress>) -> Unit): Int {
            return if (username?.isNotEmpty() == true) {
                RetroAchievementsApi.getInstance().GetUserRecentlyPlayedGames(username, RECENT_GAMES_BATCH_SIZE, offset) { parseRecentlyPlayedGames(it, callback) }
                offset + RECENT_GAMES_BATCH_SIZE
            } else {
                callback(listOf())
                0
            }
        }

        private suspend fun parseRecentlyPlayedGames(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (List<GameProgress>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_USER_RECENTLY_PLAYED_GAMES -> {
                    val games = mutableListOf<GameProgress>()
                    try {
                        val reader = JSONArray(response.second)
                        for (i in 0 until reader.length()) {
                            games.add(GameProgress(
                                    id = reader.getJSONObject(i).getString("GameID"),
                                    title = reader.getJSONObject(i).getString("Title"),
                                    imageIcon = reader.getJSONObject(i).getString("ImageIcon"),
                                    numAchievements = reader.getJSONObject(i).getString("NumPossibleAchievements").let { if (it.isDigitsOnly()) it.toInt() else 0 },
                                    numAwardedToUser = reader.getJSONObject(i).getString("NumAchieved").let { if (it.isDigitsOnly()) it.toInt() else 0 },
                                    totalPoints = reader.getJSONObject(i).getString("ScoreAchieved").let { if (it.isDigitsOnly()) it.toInt() else 0 },
                                    earnedPoints = reader.getJSONObject(i).getString("PossibleScore").let { if (it.isDigitsOnly()) it.toInt() else 0 }))
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Failed to parse recenntly played games", e)
                    } finally {
                        callback(games)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private const val RECENT_GAMES_BATCH_SIZE = 15
        private val TAG = Consts.BASE_TAG + GameList::class.java.simpleName
    }
}