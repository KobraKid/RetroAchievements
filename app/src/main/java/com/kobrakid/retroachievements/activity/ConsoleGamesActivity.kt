package com.kobrakid.retroachievements.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.ThemeManager
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter
import com.kobrakid.retroachievements.ra.Console
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

/**
 * This Activity lists all of the games of a certain console.
 */
class ConsoleGamesActivity : AppCompatActivity() {

    private val gameSummaryAdapter: GameSummaryAdapter by lazy { GameSummaryAdapter(getDrawable(R.drawable.image_view_border)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(ThemeManager.getTheme(this, sharedPref))
        setContentView(R.layout.activity_console_games)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        val bundle = intent.extras
        if (bundle != null) {
            val console = bundle.getParcelable("console") ?: Console()
            val id = console.id
            title = console.name

            val gameListRecyclerView = findViewById<RecyclerView>(R.id.list_games)
            gameListRecyclerView.adapter = gameSummaryAdapter
            gameListRecyclerView.layoutManager = LinearLayoutManager(this)
            val gamesFilter = findViewById<EditText>(R.id.list_games_filter)
            gamesFilter.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                    gameSummaryAdapter.filter.filter(charSequence.toString())
                }

                override fun afterTextChanged(editable: Editable) {}
            })

            CoroutineScope(IO).launch {
                RetroAchievementsApi.GetGameList(applicationContext, id) { parseGameList(it) }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun parseGameList(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    if (reader.length() > 0) {
                        findViewById<View>(R.id.list_no_games).visibility = View.GONE
                        for (i in 0 until reader.length()) {
                            gameSummaryAdapter.addGame(
                                    reader.getJSONObject(i).getString("ID"),
                                    reader.getJSONObject(i).getString("ImageIcon"),
                                    reader.getJSONObject(i).getString("Title")
                            )
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse game list", e)
                }
                withContext(Main) {
                    populateGamesView()
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    private fun populateGamesView() {
        if (gameSummaryAdapter.numGames == 0) findViewById<View>(R.id.list_no_games).visibility = View.VISIBLE
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
        private val TAG = ConsoleGamesActivity::class.java.simpleName
    }
}