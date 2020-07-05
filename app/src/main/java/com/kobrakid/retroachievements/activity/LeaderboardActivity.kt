package com.kobrakid.retroachievements.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.adapter.ParticipantsAdapter
import com.kobrakid.retroachievements.ra.Leaderboard
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * Represents a single leaderboard instance. Displays information about the leaderboard
 * and lists the participating users.
 */
class LeaderboardActivity : AppCompatActivity() {

    private val participantsAdapter: ParticipantsAdapter = ParticipantsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up theme and title bar
        setTheme(getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getInt(getString(R.string.theme_setting), R.style.BlankTheme))
        setContentView(R.layout.activity_leaderboard)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        val bundle = intent.extras
        if (bundle != null) {
            val leaderboard = bundle.getParcelable("leaderboard") ?: Leaderboard()
            if (leaderboard.game.isNotEmpty() && leaderboard.title.isNotEmpty())
                title = "${leaderboard.game}: ${leaderboard.title}"
            Picasso.get()
                    .load(leaderboard.image)
                    .placeholder(R.drawable.game_placeholder)
                    .into(findViewById<ImageView>(R.id.leaderboard_game_icon))
            if (leaderboard.console.isNotEmpty())
                findViewById<TextView>(R.id.leaderboard_title).text = getString(R.string.leaderboard_title_template, leaderboard.title, leaderboard.console)
            else
                findViewById<TextView>(R.id.leaderboard_title).text = title
            findViewById<TextView>(R.id.leaderboard_description).text = leaderboard.description
            when {
                leaderboard.type.contains("Score") -> findViewById<TextView>(R.id.leaderboard_type).text = getString(R.string.type_score, leaderboard.type)
                leaderboard.type.contains("Time") -> findViewById<TextView>(R.id.leaderboard_type).text = getString(R.string.type_time, leaderboard.type)
                else -> findViewById<TextView>(R.id.leaderboard_type).text = leaderboard.type
            }
            val rankedUsers = findViewById<RecyclerView>(R.id.leaderboard_participants)
            rankedUsers.adapter = participantsAdapter
            rankedUsers.layoutManager = LinearLayoutManager(this)
            if (savedInstanceState == null) {
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.GetLeaderboard(applicationContext, leaderboard.id, leaderboard.numResults) { parseLeaderboard(it) }
                }
            } else {
                val savedUsers = savedInstanceState.getStringArrayList("users")
                val savedResults = savedInstanceState.getStringArrayList("results")
                val savedDates = savedInstanceState.getStringArrayList("dates")
                if (savedUsers?.isNotEmpty() == true && savedResults?.isNotEmpty() == true && savedDates?.isNotEmpty() == true) {
                    for (i in savedUsers.indices) {
                        CoroutineScope(Default).launch {
                            participantsAdapter.addParticipant(savedUsers[i], savedResults[i], savedDates[i])
                        }
                    }
                } else {
                    CoroutineScope(IO).launch {
                        RetroAchievementsApi.GetLeaderboard(applicationContext, leaderboard.id, leaderboard.numResults) { parseLeaderboard(it) }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("users", participantsAdapter.saveUsersInstanceState())
        outState.putStringArrayList("results", participantsAdapter.saveResultsInstanceState())
        outState.putStringArrayList("dates", participantsAdapter.saveDatesInstanceState())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var handled = false
        if (id == android.R.id.home) {
            handled = true
            finish()
        }
        return if (handled) true else super.onOptionsItemSelected(item)
    }

    private suspend fun parseLeaderboard(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_LEADERBOARD -> {
                withContext(Default) {
                    val document = Jsoup.parse(response.second)
                    val userData = document.select("td.lb_user")
                    val resultData = document.select("td.lb_result")
                    val dateData = document.select("td.lb_date")
                    for (i in userData.indices) {
                        participantsAdapter.addParticipant(
                                userData[i].text(),
                                resultData[i].text(),
                                dateData[i].text())
                    }
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    companion object {
        private val TAG = LeaderboardActivity::class.java.simpleName
    }

}