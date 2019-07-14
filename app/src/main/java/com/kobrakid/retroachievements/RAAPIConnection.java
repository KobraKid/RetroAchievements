package com.kobrakid.retroachievements;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RAAPIConnection {

    private static final String BASE_URL = "https://retroachievements.org/API/";

    private String ra_user;
    private String ra_api_key;
    private final Context context;

    // Constants
    // Response Codes
    public static final int RESPONSE_ERROR = -1;
    public static final int RESPONSE_GET_TOP_TEN_USERS = 0;
    public static final int RESPONSE_GET_GAME_INFO = 1;
    public static final int RESPONSE_GET_GAME_INFO_EXTENDED = 2;
    public static final int RESPONSE_GET_CONSOLE_IDS = 3;
    public static final int RESPONSE_GET_GAME_LIST = 4;
    public static final int RESPONSE_GET_FEED_FOR = 5;
    public static final int RESPONSE_GET_USER_RANK_AND_SCORE = 6;
    public static final int RESPONSE_GET_USER_PROGRESS = 7;
    public static final int RESPONSE_GET_USER_RECENTLY_PLAYED_GAMES = 8;
    public static final int RESPONSE_GET_USER_SUMMARY = 9;
    public static final int RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS = 10;
    public static final int RESPONSE_GET_ACHIEVEMENTS_EARNED_ON_DAY = 11;
    public static final int RESPONSE_GET_ACHIEVEMENTS_EARNED_BETWEEN = 12;
    public static final int RESPONSE_GET_LEADERBOARDS = 13;

    RAAPIConnection(String ra_user, String ra_api_key, Context context) {
        this.ra_user = ra_user;
        this.ra_api_key = ra_api_key;
        this.context = context;
    }

    /**
     * Construct the portion of the URL responsible for authentication.
     *
     * @return A String containing authentication information.
     */
    private String AuthQS() {
        return "?z=" + ra_user + "&y=" + ra_api_key;
    }

    /**
     * The function responsible for performing API calls.
     *
     * @param target       The API function to call.
     * @param responseCode The response code to be returned to the callback.
     * @param callback     The callback to be notified upon request completion.
     */
    private void GetRAURL(String target, int responseCode, RAAPICallback callback) {
        GetRAURL(target, "", responseCode, callback);
    }

    /**
     * Perform the HTTP GET Request on a constructed URL.
     *
     * @param target The target API call.
     * @param params Additional parameters that the API call may require.
     */
    private void GetRAURL(String target, String params, final int responseCode, final RAAPICallback callback) {
        final String url = BASE_URL + target + AuthQS() + params;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.callback(responseCode, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.callback(RESPONSE_ERROR, "Error!");
            }
        });

        queue.add(stringRequest);
    }

    /**
     * Returns the top ten users.
     *
     * [
     *     {
     *         "1": "Username"      String,
     *         "2": "Score"         String,
     *         "3": "Retro Ratio"   String
     *     }
     * ]
     *
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetTopTenUsers(RAAPICallback callback) {
        GetRAURL(
                "API_GetTopTenUsers.php",
                RESPONSE_GET_TOP_TEN_USERS,
                callback
        );
    }

    /**
     * Returns summary information about a game.
     *
     * {
     *     "Title":         "Title"                             String,
     *     "ForumTopicID":  "Forum Topic ID"                    String,
     *     "ConsoleID":     "Console ID"                        String,
     *     "ConsoleName":   "Console Name"                      String,
     *     "Flags":         "Flags"                             String,
     *     "ImageIcon":     "Escaped URL of Image Icon"         String,
     *     "GameIcon":      "Escaped URL of Game Icon"          String,
     *     "ImageTitle":    "Escaped URL of Game Title Screen"  String,
     *     "ImageIngame":   "Escaped URL of Game Screenshot"    String,
     *     "ImageBoxArt":   "Escaped URL of Game Box Art"       String,
     *     "Publisher":     "Publisher"                         String,
     *     "Developer":     "Developer"                         String,
     *     "Genre":         "Genre"                             String,
     *     "Released":      "Release Date"                      String,
     *     "GameTitle":     "Game Title"                        String,
     *     "Console":       "Console"                           String
     * }
     *
     * @param gameID Unique String ID of a game.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameInfo(String gameID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGame.php",
                "&i=" + gameID,
                RESPONSE_GET_GAME_INFO,
                callback
        );
    }

    /**
     * Returns more extensive information on a particular game.
     *
     * {
     *     "ID":                            Game ID                                 int,
     *     "Title":                         "Game Title"                            String,
     *     "ConsoleID":                     Console ID                              int,
     *     "ForumTopicID":                  Forum Topic ID                          int,
     *     "Flags":                         Flags                                   int,
     *     "ImageIcon":                     "Escaped URL of Image Icon"             String,
     *     "ImageTitle":                    "Escaped URL of Game Title Screen"      String,
     *     "ImageIngame":                   "Escaped URL of Game Screenshot"        String,
     *     "ImageBoxArt":                   "Escaped URL of Game Box Art"           String,
     *     "Publisher":                     "Publisher"                             String,
     *     "Developer":                     "Developer"                             String,
     *     "Genre":                         "Genre"                                 String,
     *     "Released":                      "Release Date"                          String,
     *     "IsFinal":                       Is Final?                               boolean,
     *     "ConsoleName":                   "Console Name"                          String,
     *     "RichPresencePatch":             "Rich Presence Patch"                   String,
     *     "NumAchievements":               Number of Achievements                  int,
     *     "NumDistinctPlayersCasual":      "Number of Distinct Casual Players"     String,
     *     "NumDistinctPlayersHardcore":    "Number of Distinct Hardcore Players"   String,
     *     "Achievements": {
     *         "Achievement ID": {
     *             "ID":                    "Achievement ID"                        String,
     *             "NumAwarded":            "Number of Times Awarded"               String,
     *             "NumAwardedHardcore":    "Number of Times Awarded (Hardcore)"    String,
     *             "Title":                 "Achievement Title"                     String,
     *             "Description":           "Achievement Description"               String,
     *             "Points":                "Points"                                String,
     *             "TrueRatio":             "True Ratio"                            String,
     *             "Author":                "Username of Achievement Author"        String,
     *             "DateModified":          "Date Modified"                         String,
     *             "DateCreated":           "Date Created"                          String,
     *             "BadgeName":             "Badge Name"                            String,
     *             "DisplayOrder":          "Display Order"                         String,
     *             "MemAddr":               "Memory Address of Achievement"         String
     *         }
     *     }
     * }
     *
     * @param gameID Unique String ID of a game.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameInfoExtended(String gameID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGameExtended.php",
                "&i=" + gameID,
                RESPONSE_GET_GAME_INFO_EXTENDED,
                callback
        );
    }

    /**
     * Returns a list of Console IDs.
     *
     * [
     *     {
     *         "ID":    "Console ID"    String,
     *         "Name":  "Console Name"  String
     *     }
     * ]
     *
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetConsoleIDs(RAAPICallback callback) {
        GetRAURL(
                "API_GetConsoleIDs.php",
                RESPONSE_GET_CONSOLE_IDS,
                callback
        );
    }

    /**
     * Returns the list of games available for a given console.
     *
     * [
     *     {
     *         "Title":         "Game Title"                        String,
     *         "ID":            "Game ID"                           String,
     *         "ConsoleID":     "Console ID"                        String,
     *         "ImageIcon":     "Escaped URL of Game Image Icon"    String,
     *         "ConsoleName":   "Console Name"                      String
     *     }
     * ]
     *
     * @param consoleID Unique String ID of the console.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameList(String consoleID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGameList.php",
                "&i=" + consoleID,
                RESPONSE_GET_GAME_LIST,
                callback
        );
    }

    /**
     * Unused.
     *
     * @param user Unused.
     * @param count Unused.
     * @param offset Unused.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetFeedFor(String user, int count, int offset, RAAPICallback callback) {
        GetRAURL(
                "API_GetFeed.php",
                "&u=" + user + "&c=" + Integer.toString(count) + "&o=" + Integer.toString(offset),
                RESPONSE_GET_FEED_FOR,
                callback
        );
    }

    /**
     * Returns the rank and score of a given user.
     *
     * {
     *     "Score": Score   int,
     *     "Rank":  "Rank"  String
     * }
     *
     * @param user The user to get the rank and score of.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserRankAndScore(String user, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserRankAndScore.php",
                "&u=" + user,
                RESPONSE_GET_USER_RANK_AND_SCORE,
                callback
        );
    }

    /**
     * Returns summary information on a given user's progress in a given game.
     *
     * {
     *     gameIDCSV: {
     *         "NumPossibleAchievements":   "Total Number of Achievements"                      String,
     *         "PossibleScore":             "Total Possible Score"                              String,
     *         "NumAchieved":               "Number of Achievements Achieved"                   String,
     *         "ScoreAchieved":             "Score Achieved"                                    String,
     *         "NumAchievedHardcore":       "Number of Achievements Achieved in Hardcore Mode"  String,
     *         "ScoreAchievedHardcore":     "Score Achieved in Hardcore Mode"                   String
     *     }
     * }
     *
     * @param user The user to get the progress of.
     * @param gameIDCSV The unique String ID of the game to check.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserProgress(String user, String gameIDCSV, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserProgress.php",
                "&u=" + user + "&i=" + gameIDCSV,
                RESPONSE_GET_USER_PROGRESS,
                callback
        );
    }

    /**
     * Returns a list of games the given user has recently played.
     *
     * [
     *     {
     *         "GameID":                    "Game ID"                           String,
     *         "ConsoleID":                 "Console ID"                        String,
     *         "ConsoleName":               "Console Name"                      String,
     *         "Title":                     "Game Title"                        String,
     *         "ImageIcon":                 "Escaped URL of Game Image Icon"    String,
     *         "LastPlayed":                "Date Last Played"                  String,
     *         "MyVote":                    "My Vote"                           String/null,
     *         "NumPossibleAchievements":   "Number of Possible Achievements"   String,
     *         "PossibleScore":             "Total Possible Score"              String,
     *         "NumAchieved":               Number of Achievements Achieved     int,
     *         "ScoreAchieved":             Total Score Achieved                int
     *     }
     * ]
     *
     * @param user The user whose list of games will be retrieved.
     * @param count The number of entries returned.
     * @param offset How much to offset the list by before retrieving the given number of games.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserRecentlyPlayedGames(String user, int count, int offset, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserRecentlyPlayedGames.php",
                "&u=" + user + "&c=" + Integer.toString(count) + "&o=" + Integer.toString(offset),
                RESPONSE_GET_USER_RECENTLY_PLAYED_GAMES,
                callback
        );
    }

    /**
     * Returns detailed information about a given user.
     *
     * {
     *     "MemberSince":                       "Date Member Joined"                                String,
     *     "LastLogin":                         "Date Last Logged In"                               String,
     *     "RecentlyPlayedCount":               numRecentGames                                      int,
     *     "RecentlyPlayed": [
     *         {
     *             "GameID":                    "Game ID"                                           String,
     *             "ConsoleID":                 "Console ID"                                        String,
     *             "ConsoleName":               "Console Name"                                      String,
     *             "Title":                     "Game Title"                                        String,
     *             "ImageIcon":                 "Escaped URL of Game Image Icon"                    String,
     *             "LastPlayed":                "Date Last Played"                                  String,
     *             "MyVote":                    "My Vote"                                           String/null
     *         }
     *     ],
     *     "RichPresenceMsg":                   "Rich Presence Message"                             String,
     *     "LastGameID":                        "Last Played Game ID"                               String,
     *     "ContribCount":                      "Contribution Count"                                String,
     *     "ContribYield":                      "Contribution Yield"                                String,
     *     "TotalPoints":                       "Total Points"                                      String,
     *     "TotalTruePoints":                   "Total True Points (Retro Ratio)"                   String,
     *     "Permissions":                       "Permissions"                                       String,
     *     "Untracked":                         "Untracked?"                                        String,
     *     "ID":                                "User ID"                                           String,
     *     "UserWallActive":                    "User Wall Active?"                                 String,
     *     "Motto":                             "User Motto"                                        String,
     *     "Rank":                              "User Rank"                                         String,
     *     "Awarded":{
     *         "Game ID": {
     *             "NumPossibleAchievements":   "Number of Possible Achievements"                   String,
     *             "PossibleScore":             "Total Possible Score"                              String,
     *             "NumAchieved":               Number of Achievements Achieved                     String/int,
     *             "ScoreAchieved":             Total Score Achieved                                String/int,
     *             "NumAchievedHardcore":       Number of Achievements Achieved in Hardcore Mode    String/int,
     *             "ScoreAchievedHardcore":     Total Score Achieved in Hardcore Mode               String/int
     *         }
     *     }
     *     "RecentAchievements": {                                                                  (Newest First)
     *         "Game ID": {
     *             "Achievement ID": {
     *                 "ID":                    "Achievement ID"                                    String,
     *                 "GameID":                "Game ID"                                           String,
     *                 "GameTitle":             "Game Title"                                        String,
     *                 "Title":                 "Achievement Title"                                 String,
     *                 "Description":           "Achievement Description"                           String,
     *                 "Points":                "Achievement Points"                                String,
     *                 "BadgeName":             "Achievement Badge Name"                            String,
     *                 "IsAwarded":             "Is Achievement Awarded?"                           String,
     *                 "DateAwarded":           "Date Achievement Awarded"                          String,
     *                 "HardcoreAchieved":      "Is Achievement Awarded in Hardcode Mode?"          String
     *             }
     *         }
     *     }
     *     "Points":                            "User Points"                                       String,
     *     "UserPic":                           "Escaped URL of User Profile Picture"               String,
     *     "LastActivity": {
     *         "ID":                            "Last Activity ID"                                  String,
     *         "timestamp":                     "Last Activity Timestamp"                           String,
     *         "lastupdate":                    "Last Update Time"                                  String,
     *         "activitytype":                  "Activity Type"                                     String,
     *         "User":                          "User Name"                                         String,
     *         "data":                          Data                                                null,
     *         "data2":                         Data 2                                              null
     *     },
     *     "Status":                            "Status"                                            String
     * }
     *
     * @param user The user whose summary should be retrieved.
     * @param numRecentGames Number of recent games to analyze.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserSummary(String user, int numRecentGames, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserSummary.php",
                "&u=" + user + "&g=" + Integer.toString(numRecentGames) + "&a=5",
                RESPONSE_GET_USER_SUMMARY,
                callback
        );
    }

    /**
     * Returns progress of a given user towards a given game.
     *
     * {
     *     "ID":                            Game ID                                                         int,
     *     "Title":                         "Game Title"                                                    String,
     *     "ConsoleID":                     Console ID                                                      int,
     *     "ForumTopicID":                  Forum Topic ID                                                  int,
     *     "Flags":                         Flags                                                           int,
     *     "ImageIcon":                     "Escaped URL of Game Image Icon"                                String,
     *     "ImageTitle":                    "Escaped URL of Game Title Screen Image"                        String,
     *     "ImageIngame":                   "Escaped URL of Game Screenshot Image"                          String,
     *     "ImageBoxArt":                   "Escaped URL of Game Box Art Image"                             String,
     *     "Publisher":                     "Publisher"                                                     String,
     *     "Developer":                     "Developer"                                                     String,
     *     "Genre":                         "Genre"                                                         String,
     *     "Released":                      "Release Date"                                                  String,
     *     "IsFinal":                       Is Final?                                                       boolean,
     *     "ConsoleName":                   "Console Name"                                                  String,
     *     "RichPresencePatch":             "Rich Presence Patch"                                           String,
     *     "NumAchievements":               Number of Achievements                                          int,
     *     "NumDistinctPlayersCasual":      "Number of Distinct Casual Players"                             String,
     *     "NumDistinctPlayersHardcore":    "Number of Distinct Hardcore Players"                           String,
     *     "Achievements": {
     *         "Achievement ID": {
     *             "ID":                    "Achievement ID"                                                String,
     *             "NumAwarded":            "Number of Times Awarded"                                       String,
     *             "NumAwardedHardcore":    "Number of Times Awarded in Hardcode Mode"                      String,
     *             "Title":                 "Achievement Title"                                             String,
     *             "Description":           "Achievement Description"                                       String,
     *             "Points":                "Points"                                                        String,
     *             "TrueRatio":             "True Ratio (Retro Ratio)"                                      String,
     *             "Author":                "Achievement Author Username"                                   String,
     *             "DateModified":          "Date Modified"                                                 String,
     *             "DateCreated":           "Date Created"                                                  String,
     *             "BadgeName":             "Badge Name"                                                    String,
     *             "DisplayOrder":          "Display Order"                                                 String,
     *             "MemAddr":               "Memory Address"                                                String,
     *             "DateEarned":            "Date Achievement Earned"                                       String,
     *             "DateEarnedHardcore":    "Date Achievement Earned in Hardcore Mode"                      String
     *         }
     *     },
     *     "NumAwardedToUser":              Number of Achievements Awarded to User                          int,
     *     "NumAwardedToUserHardcore":      Number of Achievements Awarded to User in Hardcore Mode         int,
     *     "UserCompletion":                "Percentage of Achievements Awarded to User"                    String,
     *     "UserCompletionHardcore":        "Percentage of Achievements Awarded to User in Hardcore Mode"   String
     * }
     *
     * @param user The user whose info and progress should be retrieved.
     * @param gameID The Unique String ID of the game progress to be retrieved.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameInfoAndUserProgress(String user, String gameID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGameInfoAndUserProgress.php",
                "&u=" + user + "&g=" + gameID,
                RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS,
                callback
        );
    }

    /**
     * Returns the list of achievements earned by a given user on a given day.
     *
     * [
     *     {
     *         "Date":          "Date"                          String,
     *         "HardcoreMode":  "Hardcore Mode?"                String,
     *         "AchievementID": "Achievement ID"                String,
     *         "Title":         "Achievement Title"             String,
     *         "Description":   "Achievement Description"       String,
     *         "BadgeName":     "Badge Name"                    String,
     *         "Points":        "Points"                        String,
     *         "Author":        "Achievement Author Username"   String,
     *         "GameTitle":     "Game Title"                    String,
     *         "GameIcon":      "Escaped URL of Game Icon"      String,
     *         "GameID":        "Game ID"                       String,
     *         "ConsoleName":   "Console Name"                  String,
     *         "CumulScore":    Cumulative Score (Strict incr.) int,
     *         "BadgeURL":      "Escaped URL of Badge"          String,
     *         "GameURL":       "Escaped URL of Game"           String
     *     }
     * ]
     *
     * @param user The user whose achievement earnings should be retrieved.
     * @param dateInput The date to look up, formatted as "yyyy-MM-dd."
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetAchievementsEarnedOnDay(String user, String dateInput, RAAPICallback callback) {
        GetRAURL(
                "API_GetAchievementsEarnedOnDay.php",
                "&u=" + user + "&d=" + dateInput,
                RESPONSE_GET_ACHIEVEMENTS_EARNED_ON_DAY,
                callback
        );
    }

    /**
     * Returns the list of achievements earned by a given user between a range of dates.
     *
     * [
     *     {
     *         "Date":          "Date"                          String,
     *         "HardcoreMode":  "Hardcore Mode?"                String,
     *         "AchievementID": "Achievement ID"                String,
     *         "Title":         "Achievement Title"             String,
     *         "Description":   "Achievement Description"       String,
     *         "BadgeName":     "Badge Name"                    String,
     *         "Points":        "Points"                        String,
     *         "Author":        "Achievement Author Username"   String,
     *         "GameTitle":     "Game Title"                    String,
     *         "GameIcon":      "Escaped URL of Game Icon"      String,
     *         "GameID":        "Game ID"                       String,
     *         "ConsoleName":   "Console Name"                  String,
     *         "CumulScore":    Cumulative Score (Strict incr.) int,
     *         "BadgeURL":      "Escaped URL of Badge"          String,
     *         "GameURL":       "Escaped URL of Game"           String
     *     }
     * ]
     *
     * @param user The user whose achievement earnings should be retrieved.
     * @param dateStart The starting Date object.
     * @param dateEnd The ending Date object.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetAchievementsEarnedBetween(String user, Date dateStart, Date dateEnd, RAAPICallback callback) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GetRAURL(
                "API_GetAchievementsEarnedBetween.php",
                "&u=" + user
                        + "&f=" + Long.toString(dateStart.getTime() / 1000)
                        + "&t=" + Long.toString(dateEnd.getTime() / 1000),
                RESPONSE_GET_ACHIEVEMENTS_EARNED_BETWEEN,
                callback
        );
    }

    /**
     * Returns the full list of leaderboards available.
     *
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetLeaderboardsList(final RAAPICallback callback) {
        final String url = "https://retroachievements.org/leaderboardList.php";
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.callback(RESPONSE_GET_LEADERBOARDS, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.callback(RESPONSE_ERROR, "Error!");
            }
        });

        queue.add(stringRequest);
    }

}
