package com.kobrakid.retroachievements;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * The RAAPIConnection class exposes the RetroAchievements API.
 */
@SuppressWarnings("WeakerAccess")
public class RAAPIConnection {

    private static final String TAG = RAAPIConnection.class.getSimpleName();

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
    public static final int RESPONSE_GET_LEADERBOARD = 14;
    public static final int RESPONSE_GET_USER_WEB_PROFILE = 15;
    public static final int RESPONSE_GET_ACHIEVEMENT_DISTRIBUTION = 16;

    private static final String BASE_URL = Consts.BASE_URL + "/" + Consts.API_URL + "/";

    private final String ra_user;
    private final String ra_api_key;
    private final Context context;

    /* Thanks to @RetroAchievements.org for providing RA_API.php */

    /**
     * @param context The context which will hold the Request Queue.
     */
    RAAPIConnection(Context context) {
        this.ra_user = MainActivity.ra_api_user;
        this.ra_api_key = MainActivity.ra_api_key;
        this.context = context;
    }

    /**
     * Constructs the portion of the URL responsible for authentication.
     *
     * @return A String containing authentication information.
     */
    private String AuthQS() {
        return "?z=" + ra_user + "&y=" + ra_api_key;
    }

    /**
     * Responsible for performing API calls.
     *
     * @param target       The API function to call.
     * @param responseCode The response code to be returned to the callback.
     * @param callback     The callback to be notified upon request completion.
     */
    private void GetRAURL(String target, int responseCode, RAAPICallback callback) {
        GetRAURL(target, "", responseCode, callback);
    }

    /**
     * Performs an HTTP GET Request on a URL constructed from its String parameters.
     *
     * @param target       The target API call.
     * @param params       Additional parameters that the API call may require.
     * @param responseCode The response code associated with this API call (if it succeeds).
     * @param callback     The RAAPICallback whose callback function will be called with the results of the API call.
     */
    private void GetRAURL(String target, String params, final int responseCode, final RAAPICallback callback) {
        if (target == null) {
            callback.callback(RESPONSE_ERROR, "null target");
        } else if (params == null) {
            callback.callback(RESPONSE_ERROR, "null parameters for target " + target);
        } else {
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
     * Queries summary information about a game.
     * <p>
     * {<br/>
     * &emsp;"Title":         "Title"                             String,<br/>
     * &emsp;"ForumTopicID":  "Forum Topic ID"                    String,<br/>
     * &emsp;"ConsoleID":     "Console ID"                        String,<br/>
     * &emsp;"ConsoleName":   "Console Name"                      String,<br/>
     * &emsp;"Flags":         "Flags"                             String,<br/>
     * &emsp;"ImageIcon":     "Escaped URL of Image Icon"         String,<br/>
     * &emsp;"GameIcon":      "Escaped URL of Game Icon"          String,<br/>
     * &emsp;"ImageTitle":    "Escaped URL of Game Title Screen"  String,<br/>
     * &emsp;"ImageIngame":   "Escaped URL of Game Screenshot"    String,<br/>
     * &emsp;"ImageBoxArt":   "Escaped URL of Game Box Art"       String,<br/>
     * &emsp;"Publisher":     "Publisher"                         String,<br/>
     * &emsp;"Developer":     "Developer"                         String,<br/>
     * &emsp;"Genre":         "Genre"                             String,<br/>
     * &emsp;"Released":      "Release Date"                      String,<br/>
     * &emsp;"GameTitle":     "Game Title"                        String,<br/>
     * &emsp;"Console":       "Console"                           String<br/>
     * }
     *
     * @param gameID   Unique String ID of a game.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameInfo(String gameID, RAAPICallback callback) {
        if (gameID == null)
            callback.callback(RESPONSE_ERROR, "No game ID");
        else
            GetRAURL(
                    "API_GetGame.php",
                    "&i=" + gameID,
                    RESPONSE_GET_GAME_INFO,
                    callback
            );
    }

    /**
     * Queries more extensive information on a particular game.
     * <p>
     * {<br/>
     * &emsp;"ID":                                Game ID                                 int,<br/>
     * &emsp;"Title":                             "Game Title"                            String,<br/>
     * &emsp;"ConsoleID":                         Console ID                              int,<br/>
     * &emsp;"ForumTopicID":                      Forum Topic ID                          int,<br/>
     * &emsp;"Flags":                             Flags                                   int,<br/>
     * &emsp;"ImageIcon":                         "Escaped URL of Image Icon"             String,<br/>
     * &emsp;"ImageTitle":                        "Escaped URL of Game Title Screen"      String,<br/>
     * &emsp;"ImageIngame":                       "Escaped URL of Game Screenshot"        String,<br/>
     * &emsp;"ImageBoxArt":                       "Escaped URL of Game Box Art"           String,<br/>
     * &emsp;"Publisher":                         "Publisher"                             String,<br/>
     * &emsp;"Developer":                         "Developer"                             String,<br/>
     * &emsp;"Genre":                             "Genre"                                 String,<br/>
     * &emsp;"Released":                          "Release Date"                          String,<br/>
     * &emsp;"IsFinal":                           Is Final?                               boolean,<br/>
     * &emsp;"ConsoleName":                       "Console Name"                          String,<br/>
     * &emsp;"RichPresencePatch":                 "Rich Presence Patch"                   String,<br/>
     * &emsp;"NumAchievements":                   Number of Achievements                  int,<br/>
     * &emsp;"NumDistinctPlayersCasual":          "Number of Distinct Casual Players"     String,<br/>
     * &emsp;"NumDistinctPlayersHardcore":        "Number of Distinct Hardcore Players"   String,<br/>
     * &emsp;"Achievements": {<br/>
     * &emsp;&emsp;"Achievement ID": {<br/>
     * &emsp;&emsp;&emsp;"ID":                    "Achievement ID"                        String,<br/>
     * &emsp;&emsp;&emsp;"NumAwarded":            "Number of Times Awarded"               String,<br/>
     * &emsp;&emsp;&emsp;"NumAwardedHardcore":    "Number of Times Awarded (Hardcore)"    String,<br/>
     * &emsp;&emsp;&emsp;"Title":                 "Achievement Title"                     String,<br/>
     * &emsp;&emsp;&emsp;"Description":           "Achievement Description"               String,<br/>
     * &emsp;&emsp;&emsp;"Points":                "Points"                                String,<br/>
     * &emsp;&emsp;&emsp;"TrueRatio":             "True Ratio"                            String,<br/>
     * &emsp;&emsp;&emsp;"Author":                "Username of Achievement Author"        String,<br/>
     * &emsp;&emsp;&emsp;"DateModified":          "Date Modified"                         String,<br/>
     * &emsp;&emsp;&emsp;"DateCreated":           "Date Created"                          String,<br/>
     * &emsp;&emsp;&emsp;"BadgeName":             "Badge Name"                            String,<br/>
     * &emsp;&emsp;&emsp;"DisplayOrder":          "Display Order"                         String,<br/>
     * &emsp;&emsp;&emsp;"MemAddr":               "Memory Address of Achievement"         String<br/>
     * &emsp;&emsp;}<br/>
     * &emsp;}<br/>
     * }
     *
     * @param gameID   Unique String ID of a game.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameInfoExtended(String gameID, RAAPICallback callback) {
        if (gameID == null)
            callback.callback(RESPONSE_ERROR, "No game ID");
        else
            GetRAURL(
                    "API_GetGameExtended.php",
                    "&i=" + gameID,
                    RESPONSE_GET_GAME_INFO_EXTENDED,
                    callback
            );
    }

    /**
     * Queries a list of Console IDs.
     * <p>
     * [<br/>
     * &emsp;{<br/>
     * &emsp;&emsp;"ID":    "Console ID"    String,<br/>
     * &emsp;&emsp;"Name":  "Console Name"  String<br/>
     * &emsp;}<br/>
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
     * Queries the list of games available for a given console.
     * <p>
     * [<br/>
     * &emsp;{<br/>
     * &emsp;&emsp;"Title":         "Game Title"                        String,<br/>
     * &emsp;&emsp;"ID":            "Game ID"                           String,<br/>
     * &emsp;&emsp;"ConsoleID":     "Console ID"                        String,<br/><br/>
     * &emsp;&emsp;"ImageIcon":     "Escaped URL of Game Image Icon"    String,<br/>
     * &emsp;&emsp;"ConsoleName":   "Console Name"                      String<br/>
     * &emsp;}<br/>
     * ]
     *
     * @param consoleID Unique String ID of the console.
     * @param callback  The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameList(String consoleID, RAAPICallback callback) {
        if (consoleID == null)
            callback.callback(RESPONSE_ERROR, "No console ID");
        else
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
     * @param user     Unused.
     * @param count    Unused.
     * @param offset   Unused.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    @Deprecated
    public void GetFeedFor(String user, int count, int offset, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            GetRAURL(
                    "API_GetFeed.php",
                    "&u=" + user + "&c=" + count + "&o=" + offset,
                    RESPONSE_GET_FEED_FOR,
                    callback
            );
    }

    /**
     * Queries the rank and score of a given user.
     * <p>
     * {<br/>
     * &emsp;"Score": Score   int,<br/>
     * &emsp;"Rank":  "Rank"  String<br/>
     * }
     *
     * @param user     The user to get the rank and score of.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserRankAndScore(String user, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            GetRAURL(
                    "API_GetUserRankAndScore.php",
                    "&u=" + user,
                    RESPONSE_GET_USER_RANK_AND_SCORE,
                    callback
            );
    }

    /**
     * Queries summary information on a given user's progress in a given game.
     * <p>
     * {<br/>
     * &emsp;gameIDCSV: {<br/>
     * &emsp;&emsp;"NumPossibleAchievements":   "Total Number of Achievements"                      String,<br/>
     * &emsp;&emsp;"PossibleScore":             "Total Possible Score"                              String,<br/>
     * &emsp;&emsp;"NumAchieved":               "Number of Achievements Achieved"                   String,<br/>
     * &emsp;&emsp;"ScoreAchieved":             "Score Achieved"                                    String,<br/>
     * &emsp;&emsp;"NumAchievedHardcore":       "Number of Achievements Achieved in Hardcore Mode"  String,<br/>
     * &emsp;&emsp;"ScoreAchievedHardcore":     "Score Achieved in Hardcore Mode"                   String<br/>
     * &emsp;}<br/>
     * }
     *
     * @param user      The user to get the progress of.
     * @param gameIDCSV The unique String ID of the game to check.
     * @param callback  The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserProgress(String user, String gameIDCSV, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else if (gameIDCSV == null)
            callback.callback(RESPONSE_ERROR, "No game ID");
        else
            GetRAURL(
                    "API_GetUserProgress.php",
                    "&u=" + user + "&i=" + gameIDCSV,
                    RESPONSE_GET_USER_PROGRESS,
                    callback
            );
    }

    /**
     * Queries a list of games the given user has recently played.
     * <p>
     * [<br/>
     * &emsp;{<br/>
     * &emsp;&emsp;"GameID":                    "Game ID"                           String,<br/>
     * &emsp;&emsp;"ConsoleID":                 "Console ID"                        String,<br/>
     * &emsp;&emsp;"ConsoleName":               "Console Name"                      String,<br/>
     * &emsp;&emsp;"Title":                     "Game Title"                        String,<br/>
     * &emsp;&emsp;"ImageIcon":                 "Escaped URL of Game Image Icon"    String,<br/>
     * &emsp;&emsp;"LastPlayed":                "Date Last Played"                  String,<br/>
     * &emsp;&emsp;"MyVote":                    "My Vote"                           String/null,<br/>
     * &emsp;&emsp;"NumPossibleAchievements":   "Number of Possible Achievements"   String,<br/>
     * &emsp;&emsp;"PossibleScore":             "Total Possible Score"              String,<br/>
     * &emsp;&emsp;"NumAchieved":               Number of Achievements Achieved     int,<br/>
     * &emsp;&emsp;"ScoreAchieved":             Total Score Achieved                int<br/>
     * &emsp;}<br/>
     * ]
     *
     * @param user     The user whose list of games will be retrieved.
     * @param count    The number of entries returned.
     * @param offset   How much to offset the list by before retrieving the given number of games.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserRecentlyPlayedGames(String user, int count, int offset, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            GetRAURL(
                    "API_GetUserRecentlyPlayedGames.php",
                    "&u=" + user + "&c=" + count + "&o=" + offset,
                    RESPONSE_GET_USER_RECENTLY_PLAYED_GAMES,
                    callback
            );
    }

    /**
     * Queries detailed information about a given user.
     * <p>
     * {<br/>
     * &emsp;"MemberSince":                         "Date Member Joined"                                String,<br/>
     * &emsp;"LastLogin":                           "Date Last Logged In"                               String,<br/>
     * &emsp;"RecentlyPlayedCount":                 numRecentGames                                      int,<br/>
     * &emsp;"RecentlyPlayed": [<br/>
     * &emsp;&emsp;{<br/>
     * &emsp;&emsp;&emsp;"GameID":                  "Game ID"                                           String,<br/>
     * &emsp;&emsp;&emsp;"ConsoleID":               "Console ID"                                        String,<br/>
     * &emsp;&emsp;&emsp;"ConsoleName":             "Console Name"                                      String,<br/>
     * &emsp;&emsp;&emsp;"Title":                   "Game Title"                                        String,<br/>
     * &emsp;&emsp;&emsp;"ImageIcon":               "Escaped URL of Game Image Icon"                    String,<br/>
     * &emsp;&emsp;&emsp;"LastPlayed":              "Date Last Played"                                  String,<br/>
     * &emsp;&emsp;&emsp;"MyVote":                  "My Vote"                                           String/null<br/>
     * &emsp;&emsp;}<br/>
     * &emsp;],<br/>
     * &emsp;"RichPresenceMsg":                     "Rich Presence Message"                             String,<br/>
     * &emsp;"LastGameID":                          "Last Played Game ID"                               String,<br/>
     * &emsp;"ContribCount":                        "Contribution Count"                                String,<br/>
     * &emsp;"ContribYield":                        "Contribution Yield"                                String,<br/>
     * &emsp;"TotalPoints":                         "Total Points"                                      String,<br/>
     * &emsp;"TotalTruePoints":                     "Total True Points (Retro Ratio)"                   String,<br/>
     * &emsp;"Permissions":                         "Permissions"                                       String,<br/>
     * &emsp;"Untracked":                           "Untracked?"                                        String,<br/>
     * &emsp;"ID":                                  "User ID"                                           String,<br/>
     * &emsp;"UserWallActive":                      "User Wall Active?"                                 String,<br/>
     * &emsp;"Motto":                               "User Motto"                                        String,<br/>
     * &emsp;"Rank":                                "User Rank"                                         String,<br/>
     * &emsp;"Awarded":{<br/>
     * &emsp;&emsp;"Game ID": {<br/>
     * &emsp;&emsp;&emsp;"NumPossibleAchievements": "Number of Possible Achievements"                   String,<br/>
     * &emsp;&emsp;&emsp;"PossibleScore":           "Total Possible Score"                              String,<br/>
     * &emsp;&emsp;&emsp;"NumAchieved":             Number of Achievements Achieved                     String/int,<br/>
     * &emsp;&emsp;&emsp;"ScoreAchieved":           Total Score Achieved                                String/int,<br/>
     * &emsp;&emsp;&emsp;"NumAchievedHardcore":     Number of Achievements Achieved in Hardcore Mode    String/int,<br/>
     * &emsp;&emsp;&emsp;"ScoreAchievedHardcore":   Total Score Achieved in Hardcore Mode               String/int<br/>
     * &emsp;&emsp;}<br/>
     * &emsp;}<br/>
     * &emsp;"RecentAchievements": {                                                                    (Newest First)<br/>
     * &emsp;&emsp;"Game ID": {<br/>
     * &emsp;&emsp;&emsp;"Achievement ID": {<br/>
     * &emsp;&emsp;&emsp;&emsp;"ID":                "Achievement ID"                                    String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"GameID":            "Game ID"                                           String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"GameTitle":         "Game Title"                                        String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"Title":             "Achievement Title"                                 String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"Description":       "Achievement Description"                           String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"Points":            "Achievement Points"                                String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"BadgeName":         "Achievement Badge Name"                            String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"IsAwarded":         "Is Achievement Awarded?"                           String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"DateAwarded":       "Date Achievement Awarded"                          String,<br/>
     * &emsp;&emsp;&emsp;&emsp;"HardcoreAchieved":  "Is Achievement Awarded in Hardcode Mode?"          String<br/>
     * &emsp;&emsp;&emsp;}<br/>
     * &emsp;&emsp;}<br/>
     * &emsp;}<br/>
     * &emsp;"Points":                              "User Points"                                       String,<br/>
     * &emsp;"UserPic":                             "Escaped URL of User Profile Picture"               String,<br/>
     * &emsp;"LastActivity": {<br/>
     * &emsp;&emsp;"ID":                            "Last Activity ID"                                  String,<br/>
     * &emsp;&emsp;"timestamp":                     "Last Activity Timestamp"                           String,<br/>
     * &emsp;&emsp;"lastupdate":                    "Last Update Time"                                  String,<br/>
     * &emsp;&emsp;"activitytype":                  "Activity Type"                                     String,<br/>
     * &emsp;&emsp;"User":                          "User Name"                                         String,<br/>
     * &emsp;&emsp;"data":                          Data                                                null,<br/>
     * &emsp;&emsp;"data2":                         Data 2                                              null<br/>
     * &emsp;},<br/>
     * &emsp;"Status":                              "Status"                                            String<br/>
     * }
     *
     * @param user           The user whose summary should be retrieved.
     * @param numRecentGames Number of recent games to analyze.
     * @param callback       The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserSummary(String user, int numRecentGames, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            GetRAURL(
                    "API_GetUserSummary.php",
                    "&u=" + user + "&g=" + numRecentGames + "&a=5",
                    RESPONSE_GET_USER_SUMMARY,
                    callback
            );
    }

    /**
     * Queries progress of a given user towards a given game.
     * <p>
     * {<br/>
     * &emsp;"ID":                              Game ID                                                         int,<br/>
     * &emsp;"Title":                           "Game Title"                                                    String,<br/>
     * &emsp;"ConsoleID":                       Console ID                                                      int,<br/>
     * &emsp;"ForumTopicID":                    Forum Topic ID                                                  int,<br/>
     * &emsp;"Flags":                           Flags                                                           int,<br/>
     * &emsp;"ImageIcon":                       "Escaped URL of Game Image Icon"                                String,<br/>
     * &emsp;"ImageTitle":                      "Escaped URL of Game Title Screen Image"                        String,<br/>
     * &emsp;"ImageIngame":                     "Escaped URL of Game Screenshot Image"                          String,<br/>
     * &emsp;"ImageBoxArt":                     "Escaped URL of Game Box Art Image"                             String,<br/>
     * &emsp;"Publisher":                       "Publisher"                                                     String,<br/>
     * &emsp;"Developer":                       "Developer"                                                     String,<br/>
     * &emsp;"Genre":                           "Genre"                                                         String,<br/>
     * &emsp;"Released":                        "Release Date"                                                  String,<br/>
     * &emsp;"IsFinal":                         Is Final?                                                       boolean,<br/>
     * &emsp;"ConsoleName":                     "Console Name"                                                  String,<br/>
     * &emsp;"RichPresencePatch":               "Rich Presence Patch"                                           String,<br/>
     * &emsp;"NumAchievements":                 Number of Achievements                                          int,<br/>
     * &emsp;"NumDistinctPlayersCasual":        "Number of Distinct Casual Players"                             String,<br/>
     * &emsp;"NumDistinctPlayersHardcore":      "Number of Distinct Hardcore Players"                           String,<br/>
     * &emsp;"Achievements": {<br/>
     * &emsp;&emsp;"Achievement ID": {<br/>
     * &emsp;&emsp;&emsp;"ID":                  "Achievement ID"                                                String,<br/>
     * &emsp;&emsp;&emsp;"NumAwarded":          "Number of Times Awarded"                                       String,<br/>
     * &emsp;&emsp;&emsp;"NumAwardedHardcore":  "Number of Times Awarded in Hardcode Mode"                      String,<br/>
     * &emsp;&emsp;&emsp;"Title":               "Achievement Title"                                             String,<br/>
     * &emsp;&emsp;&emsp;"Description":         "Achievement Description"                                       String,<br/>
     * &emsp;&emsp;&emsp;"Points":              "Points"                                                        String,<br/>
     * &emsp;&emsp;&emsp;"TrueRatio":           "True Ratio (Retro Ratio)"                                      String,<br/>
     * &emsp;&emsp;&emsp;"Author":              "Achievement Author Username"                                   String,<br/>
     * &emsp;&emsp;&emsp;"DateModified":        "Date Modified"                                                 String,<br/>
     * &emsp;&emsp;&emsp;"DateCreated":         "Date Created"                                                  String,<br/>
     * &emsp;&emsp;&emsp;"BadgeName":           "Badge Name"                                                    String,<br/>
     * &emsp;&emsp;&emsp;"DisplayOrder":        "Display Order"                                                 String,<br/>
     * &emsp;&emsp;&emsp;"MemAddr":             "Memory Address"                                                String,<br/>
     * &emsp;&emsp;&emsp;"DateEarned":          "Date Achievement Earned"                                       String,<br/>
     * &emsp;&emsp;&emsp;"DateEarnedHardcore":  "Date Achievement Earned in Hardcore Mode"                      String<br/>
     * &emsp;&emsp;}<br/>
     * &emsp;},<br/>
     * &emsp;"NumAwardedToUser":                Number of Achievements Awarded to User                          int,<br/>
     * &emsp;"NumAwardedToUserHardcore":        Number of Achievements Awarded to User in Hardcore Mode         int,<br/>
     * &emsp;"UserCompletion":                  "Percentage of Achievements Awarded to User"                    String,<br/>
     * &emsp;"UserCompletionHardcore":          "Percentage of Achievements Awarded to User in Hardcore Mode"   String<br/>
     * }
     *
     * @param user     The user whose info and progress should be retrieved.
     * @param gameID   The Unique String ID of the game progress to be retrieved.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetGameInfoAndUserProgress(String user, String gameID, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else if (gameID == null)
            callback.callback(RESPONSE_ERROR, "No game ID");
        else
            GetRAURL(
                    "API_GetGameInfoAndUserProgress.php",
                    "&u=" + user + "&g=" + gameID,
                    RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS,
                    callback
            );
    }

    /**
     * Queries the list of achievements earned by a given user on a given day.
     * <p>
     * [<br/>
     * &emsp;{<br/>
     * &emsp;&emsp;"Date":          "Date"                          String,<br/>
     * &emsp;&emsp;"HardcoreMode":  "Hardcore Mode?"                String,<br/>
     * &emsp;&emsp;"AchievementID": "Achievement ID"                String,<br/>
     * &emsp;&emsp;"Title":         "Achievement Title"             String,<br/>
     * &emsp;&emsp;"Description":   "Achievement Description"       String,<br/>
     * &emsp;&emsp;"BadgeName":     "Badge Name"                    String,<br/>
     * &emsp;&emsp;"Points":        "Points"                        String,<br/>
     * &emsp;&emsp;"Author":        "Achievement Author Username"   String,<br/>
     * &emsp;&emsp;"GameTitle":     "Game Title"                    String,<br/>
     * &emsp;&emsp;"GameIcon":      "Escaped URL of Game Icon"      String,<br/>
     * &emsp;&emsp;"GameID":        "Game ID"                       String,<br/>
     * &emsp;&emsp;"ConsoleName":   "Console Name"                  String,<br/>
     * &emsp;&emsp;"CumulScore":    Cumulative Score (Strict incr.) int,<br/>
     * &emsp;&emsp;"BadgeURL":      "Escaped URL of Badge"          String,<br/>
     * &emsp;&emsp;"GameURL":       "Escaped URL of Game"           String<br/>
     * &emsp;}<br/>
     * ]
     *
     * @param user      The user whose achievement earnings should be retrieved.
     * @param dateInput The date to look up, formatted as "yyyy-MM-dd."
     * @param callback  The RAAPICallback that should accept the results of the API call.
     */
    public void GetAchievementsEarnedOnDay(String user, String dateInput, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else if (dateInput == null)
            callback.callback(RESPONSE_ERROR, "No date");
        else
            GetRAURL(
                    "API_GetAchievementsEarnedOnDay.php",
                    "&u=" + user + "&d=" + dateInput,
                    RESPONSE_GET_ACHIEVEMENTS_EARNED_ON_DAY,
                    callback
            );
    }

    /**
     * Queries the list of achievements earned by a given user between a range of dates.
     * <p>
     * [<br/>
     * &emsp;{<br/>
     * &emsp;&emsp;"Date":          "Date"                          String,<br/>
     * &emsp;&emsp;"HardcoreMode":  "Hardcore Mode?"                String,<br/>
     * &emsp;&emsp;"AchievementID": "Achievement ID"                String,<br/>
     * &emsp;&emsp;"Title":         "Achievement Title"             String,<br/>
     * &emsp;&emsp;"Description":   "Achievement Description"       String,<br/>
     * &emsp;&emsp;"BadgeName":     "Badge Name"                    String,<br/>
     * &emsp;&emsp;"Points":        "Points"                        String,<br/>
     * &emsp;&emsp;"Author":        "Achievement Author Username"   String,<br/>
     * &emsp;&emsp;"GameTitle":     "Game Title"                    String,<br/>
     * &emsp;&emsp;"GameIcon":      "Escaped URL of Game Icon"      String,<br/>
     * &emsp;&emsp;"GameID":        "Game ID"                       String,<br/>
     * &emsp;&emsp;"ConsoleName":   "Console Name"                  String,<br/>
     * &emsp;&emsp;"CumulScore":    Cumulative Score (Strict incr.) int,<br/>
     * &emsp;&emsp;"BadgeURL":      "Escaped URL of Badge"          String,<br/>
     * &emsp;&emsp;"GameURL":       "Escaped URL of Game"           String<br/>
     * &emsp;}<br/>
     * ]
     *
     * @param user      The user whose achievement earnings should be retrieved.
     * @param dateStart The starting Date object.
     * @param dateEnd   The ending Date object.
     * @param callback  The RAAPICallback that should accept the results of the API call.
     */
    public void GetAchievementsEarnedBetween(String user, Date dateStart, Date dateEnd, RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else {
            GetRAURL(
                    "API_GetAchievementsEarnedBetween.php",
                    "&u=" + user
                            + "&f=" + (dateStart.getTime() / 1000)
                            + "&t=" + (dateEnd.getTime() / 1000),
                    RESPONSE_GET_ACHIEVEMENTS_EARNED_BETWEEN,
                    callback
            );
        }
    }

    /* Web Scraping */

    /**
     * Scrapes the RA website for all available leaderboards.
     *
     * @param useCache If set, a cached version of the leaderboards list will be fetched instead (if
     *                 it exists), to prevent repeat downloads of large HTML.
     * @param callback The RAAPICallback that should accept the results of the call.
     */
    public void GetLeaderboards(boolean useCache, final RAAPICallback callback) {
        GetFile getFile = new GetFile();
        getFile.execute(useCache, callback, context);
    }

    /**
     * Scrapes the RA website for a single Leaderboard page.
     *
     * @param leaderboardID The ID of the corresponding leaderboard.
     * @param callback      The RAAPICallback that should accept the results of the call.
     */
    public void GetLeaderboard(String leaderboardID, String count, final RAAPICallback callback) {
        if (leaderboardID == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            new GetWeb(callback, RESPONSE_GET_LEADERBOARD).execute(Consts.BASE_URL + "/" + Consts.LEADERBOARDS_INFO_POSTFIX + leaderboardID + "&c=" + count);
    }

    /**
     * Scrapes the RA website for any of the user's information that is not exposed by the API.
     *
     * @param user     The user whose information should be scraped.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetUserWebProfile(String user, final RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            new GetWeb(callback, RESPONSE_GET_USER_WEB_PROFILE).execute(Consts.BASE_URL + "/" + Consts.USER_POSTFIX + "/" + user);
    }

    /**
     * Scrapes the RA website for the achievement distribution of a particular game.
     *
     * @param gameID   The ID of the game whose distribution is to be fetched.
     * @param callback The RAAPICallback that should accept the results of the API call.
     */
    public void GetAchievementDistribution(String gameID, final RAAPICallback callback) {
        if (gameID == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            new GetWeb(callback, RESPONSE_GET_ACHIEVEMENT_DISTRIBUTION).execute(Consts.BASE_URL + "/" + Consts.GAME_POSTFIX + "/" + gameID);
    }

    /* Inner Classes and Interfaces */

    private static class GetWeb extends AsyncTask<String, Void, Document> {

        final RAAPICallback callback;
        final int callbackCode;

        GetWeb(RAAPICallback callback, int code) {
            this.callback = callback;
            this.callbackCode = code;
        }

        @Override
        protected Document doInBackground(String... urls) {
            if (urls.length != 1) {
                Log.e(TAG, "The GetWeb task takes exactly one parameter, the URL to download.");
                return Jsoup.parse("");
            }
            String url = urls[0];
            Document document = null;
            try {
                document = Jsoup.connect(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return document;
        }

        @Override
        protected void onPostExecute(Document result) {
            callback.callback(result.outerHtml().length() > 0 ? callbackCode : RESPONSE_ERROR, result.outerHtml());
        }
    }

    private static class GetFile extends AsyncTask<Object, String, String> {

        RAAPICallback callback;

        @Override
        protected String doInBackground(Object... params) {
            Boolean useCache = (Boolean) params[0];
            final RAAPICallback callback = (RAAPICallback) params[1];
            this.callback = callback;
            final Context context = (Context) params[2];

            String response = "";
            if (useCache) {
                // Try to fetch cached file
                // TODO Check timestamp and re-download if file is too old
                try {
                    File f = new File(context.getFilesDir().getPath() + "/" + context.getString(R.string.file_leaderboards_cache));
                    FileInputStream is = new FileInputStream(f);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    if (is.read(buffer) == -1)
                        Log.i(TAG, "Retrieved cached data");
                    is.close();
                    response = new String(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "Error reading data", e);
                }
            }
            if (response.length() == 0) {
                final String url = Consts.BASE_URL + "/" + Consts.LEADERBOARDS_POSTFIX;
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Cache result
                        try {
                            FileWriter writer = new FileWriter(context.getFilesDir().getPath() + "/" + context.getString(R.string.file_leaderboards_cache));
                            writer.write(response);
                            writer.flush();
                            writer.close();
                            Log.i(TAG, "Wrote cached data");
                        } catch (IOException e) {
                            Log.e(TAG, "Error writing data", e);
                        }
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
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            callback.callback(RESPONSE_GET_LEADERBOARDS, response);
        }
    }

}
