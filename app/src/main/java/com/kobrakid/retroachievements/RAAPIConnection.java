package com.kobrakid.retroachievements;

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

    public final String ra_user;
    public final String ra_api_key;
    private final Context context;

    public RAAPIConnection(String ra_user, String ra_api_key, Context context) {
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

    private void GetRAURL(String target, RAAPICallback callback) {
        GetRAURL(target, "", callback);
    }

    /**
     * Perform the HTTP GET Request on a constructed URL.
     *
     * @param target The target API call.
     * @param params Additional parameters that the API call may require.
     */
    private void GetRAURL(String target, String params, final RAAPICallback callback) {
        final String url = BASE_URL + target + AuthQS() + params;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // TODO Return response
                callback.result = url + "\n ---- \n" + response;
                callback.run();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Return error
                callback.result = "Error!";
                callback.run();
            }
        });

        queue.add(stringRequest);
    }

    public void GetTopTenUsers(RAAPICallback callback) {
        GetRAURL(
                "API_GetTopTenUsers.php",
                callback
        );
    }

    public void GetGameInfo(String gameID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGame.php",
                "&i=" + gameID,
                callback
        );
    }

    public void GetGameInfoExtended(String gameID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGameExtended.php",
                "&i=" + gameID,
                callback
        );
    }

    public void GetConsoleIDs(RAAPICallback callback) {
        GetRAURL(
                "API_GetConsoleIDs.php",
                callback
        );
    }

    public void GetGameList(String consoleID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGameList.php",
                "&i=" + consoleID,
                callback
        );
    }

    public void GetFeedFor(String user, int count, int offset, RAAPICallback callback) {
        GetRAURL(
                "API_GetFeed.php",
                "&u=" + user + "&c=" + Integer.toString(count) + "&o=" + Integer.toString(offset),
                callback
        );
    }

    public void GetUserRankAndScore(String user, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserRankAndScore.php",
                "&u=" + user,
                callback
        );
    }

    public void GetUserProgress(String user, String gameIDCSV, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserProgress.php",
                "&u=" + user + "&i=" + gameIDCSV,
                callback
        );
    }

    public void GetUserRecentlyPlayedGames(String user, int count, int offset, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserRecentlyPlayedGames.php",
                "&u=" + user + "&c=" + Integer.toString(count) + "&o=" + Integer.toString(offset),
                callback
        );
    }

    public void GetUserSummary(String user, int numRecentGames, RAAPICallback callback) {
        GetRAURL(
                "API_GetUserSummary.php",
                "&u=" + user + "&g=" + Integer.toString(numRecentGames) + "&a=5",
                callback
        );
    }

    public void GetGameInfoAndUserProgress(String user, String gameID, RAAPICallback callback) {
        GetRAURL(
                "API_GetGameInfoAndUserProgress.php",
                "&u=" + user + "&g=" + gameID,
                callback
        );
    }

    public void GetAchievementsEarnedOnDay(String user, String dateInput, RAAPICallback callback) {
        GetRAURL(
                "API_GetAchievementsEarnedOnDay.php",
                "&u=" + user + "&d=" + dateInput,
                callback
        );
    }

    public void GetAchievementsEarnedBetween(String user, Date dateStart, Date dateEnd, RAAPICallback callback) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GetRAURL(
                "API_GetAchievementsEarnedBetween.php",
                "&u=" + user
                        + "&f=" + Long.toString(dateStart.getTime() / 1000)
                        + "&t=" + Long.toString(dateEnd.getTime() / 1000),
                callback
        );
    }

}
