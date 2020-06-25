package com.kobrakid.retroachievements

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
import com.kobrakid.retroachievements.ThemeManager.getTheme
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter
import org.json.JSONArray
import org.json.JSONException

/**
 * This class will display a more comprehensive list of recent games, rather than the
 * quick 5-game summary present on the home screen.
 */
class RecentGamesActivity : AppCompatActivity(), RAAPICallback, OnRefreshListener {

    private val apiConnectionDeprecated: RAAPIConnectionDeprecated by lazy { RAAPIConnectionDeprecated(this) }
    private var isActive = false
    private var offset = 0
    private val gamesPerAPICall = 15
    private var hasParsed = false // Prevent spam API calls while scrolling repeatedly
    // TODO: Determine if lazy is really required here
    private val gameSummaryAdapter: GameSummaryAdapter by lazy { GameSummaryAdapter(this) }

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
                // If the user has already scrolled to the end, the scrolling will halt while more
                // entries are added.
                if (layoutManager.findLastVisibleItemPosition() >= offset + gamesPerAPICall - 2 && hasParsed) {
                    hasParsed = false
                    offset += gamesPerAPICall
                    apiConnectionDeprecated.GetUserRecentlyPlayedGames(MainActivity.raUser, gamesPerAPICall, offset, this@RecentGamesActivity)
                }
            }
        })

        // Set up refresh action
        findViewById<SwipeRefreshLayout>(R.id.recent_games_refresh).setOnRefreshListener(this)
        offset = 0
        hasParsed = false
        apiConnectionDeprecated.GetUserRecentlyPlayedGames(MainActivity.raUser, gamesPerAPICall, offset, this)
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onRefresh() {
        hasParsed = false
        offset = 0
        apiConnectionDeprecated.GetUserRecentlyPlayedGames(MainActivity.raUser, gamesPerAPICall, offset, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
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

    override fun callback(responseCode: Int, response: String) {
        if (!isActive) {
            offset = 0.coerceAtLeast(offset - gamesPerAPICall)
        }
        else if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_USER_RECENTLY_PLAYED_GAMES) {
            val reader: JSONArray
            try {
                reader = JSONArray(response)
                if (offset == 0) gameSummaryAdapter.clear()
                for (i in 0 until reader.length()) {
                    val game = reader.getJSONObject(i)
                    gameSummaryAdapter.addGame(
                            i + offset,
                            game.getString("GameID"),
                            game.getString("ImageIcon"),
                            game.getString("Title"),
                            getString(R.string.game_stats,
                                    game.getString("NumAchieved"),
                                    game.getString("NumPossibleAchievements"),
                                    game.getString("ScoreAchieved"),
                                    game.getString("PossibleScore")), game.getString("NumAchieved") != "0"
                            && game.getString("NumAchieved") == game.getString("NumPossibleAchievements"))
                }
                findViewById<SwipeRefreshLayout>(R.id.recent_games_refresh).isRefreshing = false
            } catch (e: JSONException) {
                Log.e(TAG, "Failed to parse recenntly played games", e)
            }
            gameSummaryAdapter.updateGameSummaries(offset, gamesPerAPICall)
            hasParsed = true
        }
    }

    /**
     * Sets up a new activity to show more details on a particular game.
     *
     * @param view The game the user tapped on.
     */
    fun showGameDetails(view: View) {
        val intent = Intent(this, GameDetailsActivity::class.java)
        val extras = Bundle()
        extras.putString("GameID",
                (view.findViewById<View>(R.id.game_summary_game_id) as TextView).text.toString())
        intent.putExtras(extras)
        startActivity(intent)
    }

    companion object {
        private val TAG = RecentGamesActivity::class.java.simpleName
    }
}