@file:Suppress("FunctionName", "unused")

package com.kobrakid.retroachievements

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * This class exposes the RetroAchievements API.
 */
class RetroAchievementsApi private constructor() {
    companion object {

        private val TAG = Consts.BASE_TAG + RetroAchievementsApi::class.java.simpleName

        /* Thanks to @RetroAchievements.org for providing RA_API.php */

        private var authUser: String? = null
        private var authApiKey: String? = null
        const val BASE_URL = Consts.BASE_URL + "/" + Consts.API_URL + "/"

        /**
         * Sets the credentials for future API queries.
         *
         * @param user   Username
         * @param apiKey API Key
         */
        fun setCredentials(user: String, apiKey: String) {
            authUser = user
            authApiKey = apiKey
        }

        /**
         * Adds authentication parameters to queries.
         *
         * @param url Unauthenticated URL.
         */
        private fun AuthQS(url: String): String {
            return "$BASE_URL$url?z=$authUser&y=$authApiKey"
        }

        /**
         * Responsible for performing API calls.
         *
         * @param context      The context for the Volley queue.
         * @param target       The API function to call.
         * @param responseCode The response code to be returned to the callback.
         * @param onResult     The function to be called with the results of the API call.
         */
        private fun GetRAURL(context: Context, target: String, responseCode: RESPONSE, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(context, target, "", responseCode, onResult)
        }

        /**
         * Performs an HTTP GET Request on a URL constructed from its String parameters.
         *
         * @param context      The context for the Volley queue.
         * @param target       The target API call.
         * @param params       Additional parameters that the API call may require.
         * @param responseCode The response code associated with this API call (if it succeeds).
         * @param onResult     The function to be called with the results of the API call.
         */
        private fun GetRAURL(context: Context, target: String?, params: String?, responseCode: RESPONSE, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            when {
                target == null -> CoroutineScope(Default).launch { onResult(Pair(RESPONSE.ERROR, "null target")) }
                params == null -> CoroutineScope(Default).launch { onResult(Pair(RESPONSE.ERROR, "null parameters for target $target")) }
                else -> Volley.newRequestQueue(context)
                        .add(StringRequest(
                                target + params,
                                Response.Listener<String> { CoroutineScope(Default).launch { onResult(Pair(responseCode, it)) } },
                                Response.ErrorListener { CoroutineScope(Default).launch { onResult(Pair(RESPONSE.ERROR, it.toString())) } })
                                .setRetryPolicy(DefaultRetryPolicy(
                                        6000,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)))
            }
        }

        /**
         * Queries the top ten users.
         *
         * @param context  The context for the Volley queue.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetTopTenUsers(context: Context, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetTopTenUsers.php"),
                    RESPONSE.GET_TOP_TEN_USERS,
                    onResult
            )
        }

        /**
         * Queries summary information about a game.
         *
         * @param context  The context for the Volley queue.
         * @param gameID   Unique String ID of a game.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetGame(context: Context, gameID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetGame.php"),
                    "&i=$gameID",
                    RESPONSE.GET_GAME,
                    onResult
            )
        }

        /**
         * Queries more extensive information on a particular game.
         *
         * @param context  The context for the Volley queue.
         * @param gameID   Unique String ID of a game.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetGameExtended(context: Context, gameID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetGameExtended.php"),
                    "&i=$gameID",
                    RESPONSE.GET_GAME_EXTENDED,
                    onResult
            )
        }

        /**
         * Queries a list of Console IDs.
         *
         * @param context  The context for the Volley queue.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetConsoleIDs(context: Context, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetConsoleIDs.php"),
                    RESPONSE.GET_CONSOLE_IDS,
                    onResult
            )
        }

        /**
         * Queries the list of games available for a given console.
         *
         * @param context   The context for the Volley queue.
         * @param consoleID Unique String ID of the console.
         * @param onResult  The function to be called with the results of the API call.
         */
        fun GetGameList(context: Context, consoleID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetGameList.php"),
                    "&i=$consoleID",
                    RESPONSE.GET_GAME_LIST,
                    onResult
            )
        }

        /**
         * Unused.
         *
         * @param context  Unused.
         * @param user     Unused.
         * @param count    Unused.
         * @param offset   Unused.
         * @param onResult Unused.
         */
        @Deprecated("Unused by RA")
        fun GetFeed(context: Context, user: String, count: Int, offset: Int, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetFeed.php"),
                    "&u=$user&c=$count&o=$offset",
                    RESPONSE.GET_FEED,
                    onResult
            )
        }

        /**
         * Queries the rank and score of a given user.
         *
         * @param context  The context for the Volley queue.
         * @param user     The user to get the rank and score of.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetUserRankAndScore(context: Context, user: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetUserRankAndScore.php"),
                    "&u=$user",
                    RESPONSE.GET_USER_RANK_AND_SCORE,
                    onResult
            )
        }

        /**
         * Queries summary information on a given user's progress in a given game.
         *
         * @param context   The context for the Volley queue.
         * @param user      The user to get the progress of.
         * @param gameIDCSV The unique String ID of the game to check.
         * @param onResult  The function to be called with the results of the API call.
         */
        fun GetUserProgress(context: Context, user: String, gameIDCSV: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetUserProgress.php"),
                    "&u=$user&i=$gameIDCSV",
                    RESPONSE.GET_USER_PROGRESS,
                    onResult
            )
        }

        /**
         * Queries a list of games the given user has recently played.
         *
         * @param context  The context for the Volley queue.
         * @param user     The user whose list of games will be retrieved.
         * @param count    The number of entries returned.
         * @param offset   How much to offset the list by before retrieving the given number of games.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetUserRecentlyPlayedGames(context: Context, user: String, count: Int, offset: Int, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetUserRecentlyPlayedGames.php"),
                    "&u=$user&c=$count&o=$offset",
                    RESPONSE.GET_USER_RECENTLY_PLAYED_GAMES,
                    onResult
            )
        }

        /**
         * Queries detailed information about a given user.
         *
         * @param context        The context for the Volley queue.
         * @param user           The user whose summary should be retrieved.
         * @param numRecentGames Number of recent games to analyze.
         * @param onResult       The function to be called with the results of the API call.
         */
        fun GetUserSummary(context: Context, user: String, numRecentGames: Int, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetUserSummary.php"),
                    "&u=$user&g=$numRecentGames&a=5",
                    RESPONSE.GET_USER_SUMMARY,
                    onResult
            )
        }

        /**
         * Queries progress of a given user towards a given game.
         *
         * @param context  The context for the Volley queue.
         * @param user     The user whose info and progress should be retrieved.
         * @param gameID   The Unique String ID of the game progress to be retrieved.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetGameInfoAndUserProgress(context: Context, user: String?, gameID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            // Intentionally fall through to following if condition, because it is possible to
            // call api without user here, but the caller should still be warned
            if (user == null) CoroutineScope(Default).launch { onResult(Pair(RESPONSE.ERROR, "No user provided")) }
            GetRAURL(
                    context,
                    AuthQS("API_GetGameInfoAndUserProgress.php"),
                    "&u=$user&g=$gameID",
                    RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS,
                    onResult
            )
        }

        /**
         * Queries the list of achievements earned by a given user on a given day.
         *
         * @param context   The context for the Volley queue.
         * @param user      The user whose achievement earnings should be retrieved.
         * @param dateInput The date to look up, formatted as "yyyy-MM-dd."
         * @param onResult  The function to be called with the results of the API call.
         */
        fun GetAchievementsEarnedOnDay(context: Context, user: String, dateInput: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetAchievementsEarnedOnDay.php"),
                    "&u=$user&d=$dateInput",
                    RESPONSE.GET_ACHIEVEMENTS_EARNED_ON_DAY,
                    onResult
            )
        }

        /**
         * Queries the list of achievements earned by a given user between a range of dates,
         * given in seconds since Epoch.
         *
         * @param context   The context for the Volley queue.
         * @param user      The user whose achievement earnings should be retrieved.
         * @param dateStart The starting Date object.
         * @param dateEnd   The ending Date object.
         * @param onResult  The function to be called with the results of the API call.
         */
        fun GetAchievementsEarnedBetween(context: Context, user: String, dateStart: Date, dateEnd: Date, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetAchievementsEarnedBetween.php"),
                    "&u=" + user
                            + "&f=" + dateStart.time / 1000
                            + "&t=" + dateEnd.time / 1000,
                    RESPONSE.GET_ACHIEVEMENTS_EARNED_BETWEEN,
                    onResult
            )
        }

        /**
         * Scrapes the RA website for alll Leaderboards.
         *
         * @param context  The context for the Volley queue.
         * @param useCache Whether this call should try to rely on a cached version before accessing the network.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetLeaderboards(context: Context, useCache: Boolean, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            // Invalidate cache after at least this many milliseconds
            // Current value: 60 min * 60 sec * 1000 ms = 1 hr
            val timeToInvalidateCache = 60 * 60 * 1000
            var response = ""
            if (useCache) {
                // Try to fetch cached file
                try {
                    val f = File(context.filesDir.path + "/" + context.getString(R.string.file_leaderboards_cache))
                    if (System.currentTimeMillis() - f.lastModified() <= timeToInvalidateCache) {
                        Log.v(TAG, "Retreiving cached data")
                        val inputStream = FileInputStream(f)
                        val buffer = ByteArray(inputStream.available())
                        if (inputStream.read(buffer) == -1) Log.v(TAG, "Read cached data")
                        inputStream.close()
                        response = String(buffer)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading cached data", e)
                }
            }
            when {
                response.isEmpty() -> {
                    Log.v(TAG, "No cached data read")
                    val url = Consts.BASE_URL + "/" + Consts.LEADERBOARDS_POSTFIX
                    val queue = Volley.newRequestQueue(context)
                    val stringRequest = StringRequest(
                            url,
                            Response.Listener { urlResponse: String? ->
                                // Cache result
                                try {
                                    val writer = FileWriter(context.filesDir.path + "/" + context.getString(R.string.file_leaderboards_cache))
                                    writer.write(urlResponse)
                                    writer.flush()
                                    writer.close()
                                    Log.v(TAG, "Wrote data to disk")
                                } catch (e: IOException) {
                                    Log.e(TAG, "Error writing data to disk", e)
                                }
                                CoroutineScope(Default).launch {
                                    when (urlResponse) {
                                        null -> onResult(Pair(RESPONSE.ERROR, "Empty response when retreiving leaderboards"))
                                        else -> onResult(Pair(RESPONSE.GET_LEADERBOARDS, urlResponse))
                                    }
                                }
                            },
                            Response.ErrorListener { error: VolleyError? ->
                                CoroutineScope(Default).launch {
                                    onResult(Pair(RESPONSE.ERROR, "Error retrieving remote leaderboards\n$error"))
                                }
                            }
                    )
                    queue.add(stringRequest)
                }
                else -> CoroutineScope(Default).launch { onResult(Pair(RESPONSE.GET_LEADERBOARDS, response)) }
            }
        }

        /**
         * Scrapes the RA website for a single Leaderboard page.
         *
         * @param context       The context for the Volley queue.
         * @param leaderboardID The ID of the corresponding leaderboard.
         * @param count         The number of users participating in this leaderboard.
         * @param onResult      The function to be called with the results of the API call.
         */
        fun GetLeaderboard(context: Context, leaderboardID: String, count: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    Consts.BASE_URL + "/" + Consts.LEADERBOARDS_INFO_POSTFIX + leaderboardID,
                    "&c=$count",
                    RESPONSE.GET_LEADERBOARD,
                    onResult
            )
        }

        /**
         * Scrapes the RA website for any of the user's information that is not exposed by the API.
         *
         * @param context  The context for the Volley queue.
         * @param user     The user whose information should be scraped.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetUserWebProfile(context: Context, user: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    Consts.BASE_URL + "/" + Consts.USER_POSTFIX + "/" + user,
                    RESPONSE.GET_USER_WEB_PROFILE,
                    onResult
            )
        }

        /**
         * Scrapes the RA website for the achievement distribution of a particular game.
         *
         * @param context  The context for the Volley queue.
         * @param gameID   The ID of the game whose distribution is to be fetched.
         * @param onResult The function to be called with the results of the API call.
         */
        fun ScrapeGameInfoFromWeb(context: Context, gameID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    Consts.BASE_URL + "/" + Consts.GAME_POSTFIX + "/" + gameID,
                    RESPONSE.SCRAPE_GAME_PAGE,
                    onResult
            )
        }

        /**
         * Scrapes the RA website for the hashes linked to a particular game.
         * Currently not working, requires login cookie.
         *
         * @param context  The context for the Volley queue.
         * @param gameID   The ID of the game whose linked hashes are to be fetched.
         * @param onResult The function to be called with the results of the API call.
         */
        @Deprecated("")
        fun GetLinkedHashes(context: Context, gameID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    Consts.BASE_URL + "/" + Consts.LINKED_HASHES_POSTFIX + gameID,
                    RESPONSE.GET_LINKED_HASHES,
                    onResult
            )
        }
    }

    enum class RESPONSE {
        ERROR,
        GET_TOP_TEN_USERS,
        GET_GAME,
        GET_GAME_EXTENDED,
        GET_CONSOLE_IDS,
        GET_GAME_LIST,
        GET_FEED,
        GET_USER_RANK_AND_SCORE,
        GET_USER_PROGRESS,
        GET_USER_RECENTLY_PLAYED_GAMES,
        GET_USER_SUMMARY,
        GET_GAME_INFO_AND_USER_PROGRESS,
        GET_ACHIEVEMENTS_EARNED_ON_DAY,
        GET_ACHIEVEMENTS_EARNED_BETWEEN,
        GET_LEADERBOARDS,
        GET_LEADERBOARD,
        GET_USER_WEB_PROFILE,
        SCRAPE_GAME_PAGE,
        GET_LINKED_HASHES,
        GET_COMMENTS
    }
}