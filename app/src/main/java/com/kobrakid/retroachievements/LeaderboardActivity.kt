package com.kobrakid.retroachievements

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.ThemeManager.getTheme
import com.kobrakid.retroachievements.adapter.ParticipantsAdapter
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.*

/**
 * Represents a single leaderboard instance. Displays information about the leaderboard
 * and lists the participating users.
 */
class LeaderboardActivity : AppCompatActivity(), RAAPICallback {

    private val participantsAdapter: ParticipantsAdapter = ParticipantsAdapter(this)
    private var isActive = false

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up theme and title bar
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(getTheme(this, sharedPref))
        setContentView(R.layout.activity_leaderboard)
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        val bundle = intent.extras
        if (bundle != null) {
            val id = bundle.getString("ID")
            val game = bundle.getString("GAME")
            val image = bundle.getString("IMAGE")
            val console = bundle.getString("CONSOLE")
            val title = bundle.getString("TITLE")
            val description = bundle.getString("DESCRIPTION")
            val type = bundle.getString("TYPE")
            val count = bundle.getString("NUMRESULTS")
            setTitle("$game: $title")
            Picasso.get()
                    .load(image)
                    .into(findViewById<ImageView>(R.id.leaderboard_game_icon))
            if (console != null && console == "")
                findViewById<TextView>(R.id.leaderboard_title).text = title
            else
                findViewById<TextView>(R.id.leaderboard_title).text = getString(R.string.leaderboard_title, title, console)
            findViewById<TextView>(R.id.leaderboard_description).text = description
            if (type != null && type.contains("Score"))
                findViewById<TextView>(R.id.leaderboard_type).text = getString(R.string.type_score, type)
            else if (type != null && type.contains("Time"))
                findViewById<TextView>(R.id.leaderboard_type).text = getString(R.string.type_time, type)
            else
                findViewById<TextView>(R.id.leaderboard_type).text = type
            val rankedUsers = findViewById<RecyclerView>(R.id.leaderboard_participants)
            rankedUsers.adapter = participantsAdapter
            rankedUsers.layoutManager = LinearLayoutManager(this)
            if (savedInstanceState == null) {
                RAAPIConnectionDeprecated(this).GetLeaderboard(id, count, this)
            } else {
                val savedUsers = savedInstanceState.getSerializable("users") as ArrayList<String>?
                val savedResults = savedInstanceState.getSerializable("results") as ArrayList<String>?
                val savedDates = savedInstanceState.getSerializable("dates") as ArrayList<String>?
                if (savedUsers != null && savedUsers.isNotEmpty() && savedResults != null && savedResults.isNotEmpty() && savedDates != null && savedDates.isNotEmpty()) {
                    participantsAdapter.users.addAll(savedUsers)
                    participantsAdapter.results.addAll(savedResults)
                    participantsAdapter.dates.addAll(savedDates)
                    participantsAdapter.notifyDataSetChanged()
                } else {
                    RAAPIConnectionDeprecated(this).GetLeaderboard(id, count, this)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("users", participantsAdapter.users as Serializable)
        outState.putSerializable("results", participantsAdapter.results as Serializable)
        outState.putSerializable("dates", participantsAdapter.dates as Serializable)
    }

    override fun onStart() {
        super.onStart()
        isActive = true
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
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

    override fun callback(responseCode: Int, response: String) {
        if (!isActive) return
        if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_LEADERBOARD) {
            ParseHTMLAsyncTask(participantsAdapter).execute(response)
        }
    }

    private class ParseHTMLAsyncTask internal constructor(adapter: ParticipantsAdapter?) : AsyncTask<String?, String?, Void?>() {
        private val adapterReference: WeakReference<ParticipantsAdapter?> = WeakReference(adapter)
        override fun doInBackground(vararg strings: String?): Void? {
            val document = Jsoup.parse(strings[0])
            val userData = document.select("td.lb_user")
            val resultData = document.select("td.lb_result")
            val dateData = document.select("td.lb_date")
            for (i in userData.indices) {
                publishProgress(userData[i].text(), resultData[i].text(), dateData[i].text())
            }
            return null
        }

        override fun onProgressUpdate(vararg values: String?) {
            val adapter = adapterReference.get()
            adapter?.addParticipant(values[0] ?: "", values[1] ?: "", values[2] ?: "")
        }

    }
}