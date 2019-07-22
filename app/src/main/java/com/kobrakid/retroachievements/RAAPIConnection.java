package com.kobrakid.retroachievements;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class RAAPIConnection {

    private static final String BASE_URL = "https://retroachievements.org/API/";

    private final String ra_user;
    private final String ra_api_key;
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
    public static final int RESPONSE_GET_USER_WEB_PROFILE = 14;

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
     * Returns the top ten users.
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
     * Returns summary information about a game.
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
     * Returns more extensive information on a particular game.
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
     * Returns a list of Console IDs.
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
     * Returns the list of games available for a given console.
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
     * Returns the rank and score of a given user.
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
     * Returns summary information on a given user's progress in a given game.
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
     * Returns a list of games the given user has recently played.
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
     * Returns detailed information about a given user.
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
     * Returns progress of a given user towards a given game.
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
     * Returns the list of achievements earned by a given user on a given day.
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
     * Returns the list of achievements earned by a given user between a range of dates.
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

    public void GetUserWebProfile(String user, final RAAPICallback callback) {
        if (user == null)
            callback.callback(RESPONSE_ERROR, "No user");
        else
            new GetWeb(user, callback).execute();
    }

    private static class GetWeb extends AsyncTask<Void, Void, Document> {

        final RAAPICallback callback;
        final String user;

        GetWeb(String user, RAAPICallback callback) {
            this.user = user;
            this.callback = callback;
        }

        @Override
        protected Document doInBackground(Void... voids) {
            String url = "https://retroachievements.org/user/" + user;
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
            callback.callback(RESPONSE_GET_USER_WEB_PROFILE, result.outerHtml());
        }
    }

}
