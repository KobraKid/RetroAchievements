package com.kobrakid.retroachievements.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.ThemeManager.getTheme
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

/**
 * This class will display a more comprehensive list of recent games, rather than the
 * quick 5-game summary present on the home screen.
 */
class RecentGamesActivity : AppCompatActivity(), OnRefreshListener {

    private var offset = 0
    private val gamesPerAPICall = 15

    // Initially ask for 15 games (prevent spamming API)
    private var gamesAskedFor = 15

    // TODO: Determine if lazy is really required here
    private val gameSummaryAdapter: GameSummaryAdapter by lazy { GameSummaryAdapter(getDrawable(R.drawable.image_view_border)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(getTheme(this, sharedPref))
        setContentView(R.layout.activity_recent_games)
        setTitle(R.string.recent_games_title)
        overridePendingTransition(R.anim.slide_in, android.R.anim.fade_out)

        // Set up title bar
        setSupportActionBar(findViewById(R.id.recent_games_toolbar))
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recent_games_recycler_view)
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gameSummaryAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // Try to catch user reaching end of list early and append past the screen.
                // If the user has already scrolled to the end, the scrolling will halt while more entries are added.
                if (layoutManager.findLastVisibleItemPosition() >= offset + gamesPerAPICall - 2 && gameSummaryAdapter.itemCount == gamesAskedFor) {
                    offset += gamesPerAPICall
                    gamesAskedFor += gamesPerAPICall
                    CoroutineScope(IO).launch {
                        RetroAchievementsApi.GetUserRecentlyPlayedGames(applicationContext, MainActivity.raUser, gamesPerAPICall, offset) { parseRecentlyPlayedGames(it) }
                    }
                }

            }
        })

        // Set up refresh action
        findViewById<SwipeRefreshLayout>(R.id.recent_games_refresh).setOnRefreshListener(this)
        CoroutineScope(IO).launch {
            RetroAchievementsApi.GetUserRecentlyPlayedGames(applicationContext, MainActivity.raUser, gamesPerAPICall, offset) { parseRecentlyPlayedGames(it) }
        }
    }

    override fun onRefresh() {
        offset = 0
        gamesAskedFor = gamesPerAPICall
        CoroutineScope(IO).launch {
            RetroAchievementsApi.GetUserRecentlyPlayedGames(applicationContext, MainActivity.raUser, gamesPerAPICall, offset) { parseRecentlyPlayedGames(it) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out)
    }

    private suspend fun parseRecentlyPlayedGames(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_USER_RECENTLY_PLAYED_GAMES -> {
                if (offset == 0) gameSummaryAdapter.clear()
                try {
                    // The user requested a refresh, so clear previously listed games
                    val reader = JSONArray(response.second)
                    for (i in 0 until reader.length()) {
                        gameSummaryAdapter.addGame(
                                i + offset,
                                reader.getJSONObject(i).getString("GameID"),
                                reader.getJSONObject(i).getString("ImageIcon"),
                                reader.getJSONObject(i).getString("Title"),
                                getString(R.string.game_stats,
                                        reader.getJSONObject(i).getString("NumAchieved"),
                                        reader.getJSONObject(i).getString("NumPossibleAchievements"),
                                        reader.getJSONObject(i).getString("ScoreAchieved"),
                                        reader.getJSONObject(i).getString("PossibleScore")),
                                reader.getJSONObject(i).getString("NumAchieved") != "0"
                                        && reader.getJSONObject(i).getString("NumAchieved") == reader.getJSONObject(i).getString("NumPossibleAchievements"))
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Failed to parse recenntly played games", e)
                } finally {
                    findViewById<SwipeRefreshLayout>(R.id.recent_games_refresh).isRefreshing = false
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    /**
     * Sets up a new activity to show more details on a particular game.
     *
     * @param view The game the user tapped on.
     */
    fun showGameDetails(view: View) {
        startActivity(Intent(this, GameDetailsActivity::class.java).apply {
            putExtra("GameID", view.findViewById<TextView>(R.id.game_summary_game_id).text.toString())
        })
    }

    companion object {
        private val TAG = RecentGamesActivity::class.java.simpleName
    }
}