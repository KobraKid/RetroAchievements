@file:Suppress("FunctionName", "unused")

package com.kobrakid.retroachievements

import android.content.Context
import android.util.Log
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

        private val TAG = RetroAchievementsApi::class.java.simpleName

        /* Thanks to @RetroAchievements.org for providing RA_API.php */

        private var ra_user: String? = null
        private var ra_api_key: String? = null
        const val BASE_URL = Consts.BASE_URL + "/" + Consts.API_URL + "/"

        fun setCredentials(context: Context) {
            ra_user = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getString(context.getString(R.string.ra_user), "")
            ra_api_key = context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getString(context.getString(R.string.ra_api_key), "")
        }

        private fun AuthQS(url: String): String {
            return "$BASE_URL$url?z=$ra_user&y=$ra_api_key"
        }

        /**
         * Responsible for performing API calls.
         *
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
         * @param target       The target API call.
         * @param params       Additional parameters that the API call may require.
         * @param responseCode The response code associated with this API call (if it succeeds).
         * @param onResult     The function to be called with the results of the API call.
         */
        private fun GetRAURL(context: Context, target: String?, params: String?, responseCode: RESPONSE, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            if (target == null) {
                CoroutineScope(Default).launch { onResult(Pair(RESPONSE.ERROR, "null target")) }
            } else if (params == null) {
                CoroutineScope(Default).launch { onResult(Pair(RESPONSE.ERROR, "null parameters for target $target")) }
            } else {
                val url = target + params
                val queue = Volley.newRequestQueue(context)
                queue.add(StringRequest(
                        url,
                        Response.Listener<String> { CoroutineScope(Default).launch { onResult(Pair(responseCode, it)) } },
                        Response.ErrorListener { CoroutineScope(Default).launch { onResult(Pair(RESPONSE.ERROR, it.toString())) } }))
            }
        }

        /**
         * Queries the top ten users.
         * <p>
         * [<br/>
         * &emsp;{<br/>
         * &emsp;&emsp;"1": "Username"      String,<br/>
         * &emsp;&emsp;"2": "Score"         String,<br/>
         * &emsp;&emsp;"3": "Retro Ratio"   String<br/>
         * &emsp;}<br/>
         * ]
         *
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetTopTenUsers(context: Context, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    "API_GetTopTenUsers.php",
                    RESPONSE.GET_TOP_TEN_USERS,
                    onResult
            )
        }

        /**
         * Queries summary information about a game.
         *
         *
         * {<br></br>
         * "Title":         "Title"                             String,<br></br>
         * "ForumTopicID":  "Forum Topic ID"                    String,<br></br>
         * "ConsoleID":     "Console ID"                        String,<br></br>
         * "ConsoleName":   "Console Name"                      String,<br></br>
         * "Flags":         "Flags"                             String,<br></br>
         * "ImageIcon":     "Escaped URL of Image Icon"         String,<br></br>
         * "GameIcon":      "Escaped URL of Game Icon"          String,<br></br>
         * "ImageTitle":    "Escaped URL of Game Title Screen"  String,<br></br>
         * "ImageIngame":   "Escaped URL of Game Screenshot"    String,<br></br>
         * "ImageBoxArt":   "Escaped URL of Game Box Art"       String,<br></br>
         * "Publisher":     "Publisher"                         String,<br></br>
         * "Developer":     "Developer"                         String,<br></br>
         * "Genre":         "Genre"                             String,<br></br>
         * "Released":      "Release Date"                      String,<br></br>
         * "GameTitle":     "Game Title"                        String,<br></br>
         * "Console":       "Console"                           String<br></br>
         * }
         *
         * @param gameID   Unique String ID of a game.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetGameInfo(context: Context, gameID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetGame.php"),
                    "&i=$gameID",
                    RESPONSE.GET_GAME_INFO,
                    onResult
            )
        }

        /**
         * Queries more extensive information on a particular game.
         *
         *
         * {<br></br>
         * "ID":                                Game ID                                 int,<br></br>
         * "Title":                             "Game Title"                            String,<br></br>
         * "ConsoleID":                         Console ID                              int,<br></br>
         * "ForumTopicID":                      Forum Topic ID                          int,<br></br>
         * "Flags":                             Flags                                   int,<br></br>
         * "ImageIcon":                         "Escaped URL of Image Icon"             String,<br></br>
         * "ImageTitle":                        "Escaped URL of Game Title Screen"      String,<br></br>
         * "ImageIngame":                       "Escaped URL of Game Screenshot"        String,<br></br>
         * "ImageBoxArt":                       "Escaped URL of Game Box Art"           String,<br></br>
         * "Publisher":                         "Publisher"                             String,<br></br>
         * "Developer":                         "Developer"                             String,<br></br>
         * "Genre":                             "Genre"                                 String,<br></br>
         * "Released":                          "Release Date"                          String,<br></br>
         * "IsFinal":                           Is Final?                               boolean,<br></br>
         * "ConsoleName":                       "Console Name"                          String,<br></br>
         * "RichPresencePatch":                 "Rich Presence Patch"                   String,<br></br>
         * "NumAchievements":                   Number of Achievements                  int,<br></br>
         * "NumDistinctPlayersCasual":          "Number of Distinct Casual Players"     String,<br></br>
         * "NumDistinctPlayersHardcore":        "Number of Distinct Hardcore Players"   String,<br></br>
         * "Achievements": {<br></br>
         * "Achievement ID": {<br></br>
         * "ID":                    "Achievement ID"                        String,<br></br>
         * "NumAwarded":            "Number of Times Awarded"               String,<br></br>
         * "NumAwardedHardcore":    "Number of Times Awarded (Hardcore)"    String,<br></br>
         * "Title":                 "Achievement Title"                     String,<br></br>
         * "Description":           "Achievement Description"               String,<br></br>
         * "Points":                "Points"                                String,<br></br>
         * "TrueRatio":             "True Ratio"                            String,<br></br>
         * "Author":                "Username of Achievement Author"        String,<br></br>
         * "DateModified":          "Date Modified"                         String,<br></br>
         * "DateCreated":           "Date Created"                          String,<br></br>
         * "BadgeName":             "Badge Name"                            String,<br></br>
         * "DisplayOrder":          "Display Order"                         String,<br></br>
         * "MemAddr":               "Memory Address of Achievement"         String<br></br>
         * }<br></br>
         * }<br></br>
         * }
         *
         * @param gameID   Unique String ID of a game.
         * @param onResult The function to be called with the results of the API call.
         */
        fun GetGameInfoExtended(context: Context, gameID: String, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetGameExtended.php"),
                    "&i=$gameID",
                    RESPONSE.GET_GAME_INFO_EXTENDED,
                    onResult
            )
        }

        /**
         * Queries a list of Console IDs.
         *
         *
         * [<br></br>
         * {<br></br>
         * "ID":    "Console ID"    String,<br></br>
         * "Name":  "Console Name"  String<br></br>
         * }<br></br>
         * ]
         *
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
         *
         * [<br></br>
         * {<br></br>
         * "Title":         "Game Title"                        String,<br></br>
         * "ID":            "Game ID"                           String,<br></br>
         * "ConsoleID":     "Console ID"                        String,<br></br><br></br>
         * "ImageIcon":     "Escaped URL of Game Image Icon"    String,<br></br>
         * "ConsoleName":   "Console Name"                      String<br></br>
         * }<br></br>
         * ]
         *
         * @param consoleID Unique String ID of the console.
         * @param onResult The function to be called with the results of the API call.
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
         * @param user     Unused.
         * @param count    Unused.
         * @param offset   Unused.
         * @param onResult The function to be called with the results of the API call.
         */
        @Deprecated("Unused by RA")
        fun GetFeedFor(context: Context, user: String, count: Int, offset: Int, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            GetRAURL(
                    context,
                    AuthQS("API_GetFeed.php"),
                    "&u=$user&c=$count&o=$offset",
                    RESPONSE.GET_FEED_FOR,
                    onResult
            )
        }

        /**
         * Queries the rank and score of a given user.
         *
         *
         * {<br></br>
         * "Score": Score   int,<br></br>
         * "Rank":  "Rank"  String<br></br>
         * }
         *
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
         *
         * {<br></br>
         * gameIDCSV: {<br></br>
         * "NumPossibleAchievements":   "Total Number of Achievements"                      String,<br></br>
         * "PossibleScore":             "Total Possible Score"                              String,<br></br>
         * "NumAchieved":               "Number of Achievements Achieved"                   String,<br></br>
         * "ScoreAchieved":             "Score Achieved"                                    String,<br></br>
         * "NumAchievedHardcore":       "Number of Achievements Achieved in Hardcore Mode"  String,<br></br>
         * "ScoreAchievedHardcore":     "Score Achieved in Hardcore Mode"                   String<br></br>
         * }<br></br>
         * }
         *
         * @param user      The user to get the progress of.
         * @param gameIDCSV The unique String ID of the game to check.
         * @param onResult The function to be called with the results of the API call.
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
         *
         * [<br></br>
         * {<br></br>
         * "GameID":                    "Game ID"                           String,<br></br>
         * "ConsoleID":                 "Console ID"                        String,<br></br>
         * "ConsoleName":               "Console Name"                      String,<br></br>
         * "Title":                     "Game Title"                        String,<br></br>
         * "ImageIcon":                 "Escaped URL of Game Image Icon"    String,<br></br>
         * "LastPlayed":                "Date Last Played"                  String,<br></br>
         * "MyVote":                    "My Vote"                           String/null,<br></br>
         * "NumPossibleAchievements":   "Number of Possible Achievements"   String,<br></br>
         * "PossibleScore":             "Total Possible Score"              String,<br></br>
         * "NumAchieved":               Number of Achievements Achieved     int,<br></br>
         * "ScoreAchieved":             Total Score Achieved                int<br></br>
         * }<br></br>
         * ]
         *
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
         *
         * {<br></br>
         * "MemberSince":                         "Date Member Joined"                                String,<br></br>
         * "LastLogin":                           "Date Last Logged In"                               String,<br></br>
         * "RecentlyPlayedCount":                 numRecentGames                                      int,<br></br>
         * "RecentlyPlayed": [<br></br>
         * {<br></br>
         * "GameID":                  "Game ID"                                           String,<br></br>
         * "ConsoleID":               "Console ID"                                        String,<br></br>
         * "ConsoleName":             "Console Name"                                      String,<br></br>
         * "Title":                   "Game Title"                                        String,<br></br>
         * "ImageIcon":               "Escaped URL of Game Image Icon"                    String,<br></br>
         * "LastPlayed":              "Date Last Played"                                  String,<br></br>
         * "MyVote":                  "My Vote"                                           String/null<br></br>
         * }<br></br>
         * ],<br></br>
         * "RichPresenceMsg":                     "Rich Presence Message"                             String,<br></br>
         * "LastGameID":                          "Last Played Game ID"                               String,<br></br>
         * "ContribCount":                        "Contribution Count"                                String,<br></br>
         * "ContribYield":                        "Contribution Yield"                                String,<br></br>
         * "TotalPoints":                         "Total Points"                                      String,<br></br>
         * "TotalTruePoints":                     "Total True Points (Retro Ratio)"                   String,<br></br>
         * "Permissions":                         "Permissions"                                       String,<br></br>
         * "Untracked":                           "Untracked?"                                        String,<br></br>
         * "ID":                                  "User ID"                                           String,<br></br>
         * "UserWallActive":                      "User Wall Active?"                                 String,<br></br>
         * "Motto":                               "User Motto"                                        String,<br></br>
         * "Rank":                                "User Rank"                                         String,<br></br>
         * "Awarded":{<br></br>
         * "Game ID": {<br></br>
         * "NumPossibleAchievements": "Number of Possible Achievements"                   String,<br></br>
         * "PossibleScore":           "Total Possible Score"                              String,<br></br>
         * "NumAchieved":             Number of Achievements Achieved                     String/int,<br></br>
         * "ScoreAchieved":           Total Score Achieved                                String/int,<br></br>
         * "NumAchievedHardcore":     Number of Achievements Achieved in Hardcore Mode    String/int,<br></br>
         * "ScoreAchievedHardcore":   Total Score Achieved in Hardcore Mode               String/int<br></br>
         * }<br></br>
         * }<br></br>
         * "RecentAchievements": {                                                                    (Newest First)<br></br>
         * "Game ID": {<br></br>
         * "Achievement ID": {<br></br>
         * "ID":                "Achievement ID"                                    String,<br></br>
         * "GameID":            "Game ID"                                           String,<br></br>
         * "GameTitle":         "Game Title"                                        String,<br></br>
         * "Title":             "Achievement Title"                                 String,<br></br>
         * "Description":       "Achievement Description"                           String,<br></br>
         * "Points":            "Achievement Points"                                String,<br></br>
         * "BadgeName":         "Achievement Badge Name"                            String,<br></br>
         * "IsAwarded":         "Is Achievement Awarded?"                           String,<br></br>
         * "DateAwarded":       "Date Achievement Awarded"                          String,<br></br>
         * "HardcoreAchieved":  "Is Achievement Awarded in Hardcode Mode?"          String<br></br>
         * }<br></br>
         * }<br></br>
         * }<br></br>
         * "Points":                              "User Points"                                       String,<br></br>
         * "UserPic":                             "Escaped URL of User Profile Picture"               String,<br></br>
         * "LastActivity": {<br></br>
         * "ID":                            "Last Activity ID"                                  String,<br></br>
         * "timestamp":                     "Last Activity Timestamp"                           String,<br></br>
         * "lastupdate":                    "Last Update Time"                                  String,<br></br>
         * "activitytype":                  "Activity Type"                                     String,<br></br>
         * "User":                          "User Name"                                         String,<br></br>
         * "data":                          Data                                                null,<br></br>
         * "data2":                         Data 2                                              null<br></br>
         * },<br></br>
         * "Status":                              "Status"                                            String<br></br>
         * }
         *
         * @param user           The user whose summary should be retrieved.
         * @param numRecentGames Number of recent games to analyze.
         * @param onResult The function to be called with the results of the API call.
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
         *
         * {<br></br>
         * "ID":                              Game ID                                                         int,<br></br>
         * "Title":                           "Game Title"                                                    String,<br></br>
         * "ConsoleID":                       Console ID                                                      int,<br></br>
         * "ForumTopicID":                    Forum Topic ID                                                  int,<br></br>
         * "Flags":                           Flags                                                           int,<br></br>
         * "ImageIcon":                       "Escaped URL of Game Image Icon"                                String,<br></br>
         * "ImageTitle":                      "Escaped URL of Game Title Screen Image"                        String,<br></br>
         * "ImageIngame":                     "Escaped URL of Game Screenshot Image"                          String,<br></br>
         * "ImageBoxArt":                     "Escaped URL of Game Box Art Image"                             String,<br></br>
         * "Publisher":                       "Publisher"                                                     String,<br></br>
         * "Developer":                       "Developer"                                                     String,<br></br>
         * "Genre":                           "Genre"                                                         String,<br></br>
         * "Released":                        "Release Date"                                                  String,<br></br>
         * "IsFinal":                         Is Final?                                                       boolean,<br></br>
         * "ConsoleName":                     "Console Name"                                                  String,<br></br>
         * "RichPresencePatch":               "Rich Presence Patch"                                           String,<br></br>
         * "NumAchievements":                 Number of Achievements                                          int,<br></br>
         * "NumDistinctPlayersCasual":        "Number of Distinct Casual Players"                             String,<br></br>
         * "NumDistinctPlayersHardcore":      "Number of Distinct Hardcore Players"                           String,<br></br>
         * "Achievements": {<br></br>
         * "Achievement ID": {<br></br>
         * "ID":                  "Achievement ID"                                                String,<br></br>
         * "NumAwarded":          "Number of Times Awarded"                                       String,<br></br>
         * "NumAwardedHardcore":  "Number of Times Awarded in Hardcode Mode"                      String,<br></br>
         * "Title":               "Achievement Title"                                             String,<br></br>
         * "Description":         "Achievement Description"                                       String,<br></br>
         * "Points":              "Points"                                                        String,<br></br>
         * "TrueRatio":           "True Ratio (Retro Ratio)"                                      String,<br></br>
         * "Author":              "Achievement Author Username"                                   String,<br></br>
         * "DateModified":        "Date Modified"                                                 String,<br></br>
         * "DateCreated":         "Date Created"                                                  String,<br></br>
         * "BadgeName":           "Badge Name"                                                    String,<br></br>
         * "DisplayOrder":        "Display Order"                                                 String,<br></br>
         * "MemAddr":             "Memory Address"                                                String,<br></br>
         * "DateEarned":          "Date Achievement Earned"                                       String,<br></br>
         * "DateEarnedHardcore":  "Date Achievement Earned in Hardcore Mode"                      String<br></br>
         * }<br></br>
         * },<br></br>
         * "NumAwardedToUser":                Number of Achievements Awarded to User                          int,<br></br>
         * "NumAwardedToUserHardcore":        Number of Achievements Awarded to User in Hardcore Mode         int,<br></br>
         * "UserCompletion":                  "Percentage of Achievements Awarded to User"                    String,<br></br>
         * "UserCompletionHardcore":          "Percentage of Achievements Awarded to User in Hardcore Mode"   String<br></br>
         * }
         *
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
         *
         * [<br></br>
         * {<br></br>
         * "Date":          "Date"                          String,<br></br>
         * "HardcoreMode":  "Hardcore Mode?"                String,<br></br>
         * "AchievementID": "Achievement ID"                String,<br></br>
         * "Title":         "Achievement Title"             String,<br></br>
         * "Description":   "Achievement Description"       String,<br></br>
         * "BadgeName":     "Badge Name"                    String,<br></br>
         * "Points":        "Points"                        String,<br></br>
         * "Author":        "Achievement Author Username"   String,<br></br>
         * "GameTitle":     "Game Title"                    String,<br></br>
         * "GameIcon":      "Escaped URL of Game Icon"      String,<br></br>
         * "GameID":        "Game ID"                       String,<br></br>
         * "ConsoleName":   "Console Name"                  String,<br></br>
         * "CumulScore":    Cumulative Score (Strict incr.) int,<br></br>
         * "BadgeURL":      "Escaped URL of Badge"          String,<br></br>
         * "GameURL":       "Escaped URL of Game"           String<br></br>
         * }<br></br>
         * ]
         *
         * @param user      The user whose achievement earnings should be retrieved.
         * @param dateInput The date to look up, formatted as "yyyy-MM-dd."
         * @param onResult The function to be called with the results of the API call.
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
         * Queries the list of achievements earned by a given user between a range of dates.
         *
         *
         * [<br></br>
         * {<br></br>
         * "Date":          "Date"                          String,<br></br>
         * "HardcoreMode":  "Hardcore Mode?"                String,<br></br>
         * "AchievementID": "Achievement ID"                String,<br></br>
         * "Title":         "Achievement Title"             String,<br></br>
         * "Description":   "Achievement Description"       String,<br></br>
         * "BadgeName":     "Badge Name"                    String,<br></br>
         * "Points":        "Points"                        String,<br></br>
         * "Author":        "Achievement Author Username"   String,<br></br>
         * "GameTitle":     "Game Title"                    String,<br></br>
         * "GameIcon":      "Escaped URL of Game Icon"      String,<br></br>
         * "GameID":        "Game ID"                       String,<br></br>
         * "ConsoleName":   "Console Name"                  String,<br></br>
         * "CumulScore":    Cumulative Score (Strict incr.) int,<br></br>
         * "BadgeURL":      "Escaped URL of Badge"          String,<br></br>
         * "GameURL":       "Escaped URL of Game"           String<br></br>
         * }<br></br>
         * ]
         *
         * @param user      The user whose achievement earnings should be retrieved.
         * @param dateStart The starting Date object.
         * @param dateEnd   The ending Date object.
         * @param onResult The function to be called with the results of the API call.
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

        fun GetLeaderboards(context: Context, useCache: Boolean, onResult: suspend (Pair<RESPONSE, String>) -> Unit) {
            var response = ""
            if (useCache) {
                // Try to fetch cached file
                // TODO Check timestamp and re-download if file is too old
                try {
                    val f = File(context.filesDir.path + "/" + context.getString(R.string.file_leaderboards_cache))
                    val `is` = FileInputStream(f)
                    val size = `is`.available()
                    val buffer = ByteArray(size)
                    if (`is`.read(buffer) == -1) Log.i(TAG, "Retrieved cached data")
                    `is`.close()
                    response = String(buffer)
                } catch (e: IOException) {
                    Log.e(TAG, "Error reading cached data", e)
                }
            }
            if (response.isEmpty()) {
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
                                Log.i(TAG, "Wrote data to disk")
                            } catch (e: IOException) {
                                Log.e(TAG, "Error writing data to disk", e)
                            }
                            CoroutineScope(Default).launch {
                                when (urlResponse) {
                                    null -> onResult(Pair(RESPONSE.ERROR, "empty response"))
                                    else -> onResult(Pair(RESPONSE.GET_LEADERBOARDS, urlResponse))
                                }
                            }
                        },
                        Response.ErrorListener { error: VolleyError? ->
                            CoroutineScope(Default).launch {
                                onResult(Pair(RESPONSE.ERROR, "Error retrieving remote leaderboards\n${error.toString()}"))
                            }
                        }
                )
                queue.add(stringRequest)
            }
        }

        /**
         * Scrapes the RA website for a single Leaderboard page.
         *
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
        GET_GAME_INFO,
        GET_GAME_INFO_EXTENDED,
        GET_CONSOLE_IDS,
        GET_GAME_LIST,
        GET_FEED_FOR,
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