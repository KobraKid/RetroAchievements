package com.kobrakid.retroachievements.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.ThemeManager.getTheme
import com.kobrakid.retroachievements.adapter.GameDetailsPagerAdapter
import com.kobrakid.retroachievements.ra.Game
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

/**
 * This class will display detailed information about a single game.
 */
class GameDetailsActivity : AppCompatActivity() {

    private var game = Game()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up theme and title bar
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(getTheme(this, sharedPref))
        setContentView(R.layout.activity_game_details)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        if (intent.extras != null) {
            game.id = intent.extras?.getString("GameID") ?: "0"

            val viewPager = findViewById<ViewPager>(R.id.game_details_view_pager)
            viewPager.adapter = GameDetailsPagerAdapter(supportFragmentManager, game.id)
            viewPager.offscreenPageLimit = GameDetailsPagerAdapter.GAME_DETAILS_PAGES - 1

            // These views only exist in landscape
            findViewById<ImageButton>(R.id.game_details_button_page_0)?.setOnClickListener { viewPager.currentItem = 0 }
            findViewById<ImageButton>(R.id.game_details_button_page_1)?.setOnClickListener { viewPager.currentItem = 1 }
            findViewById<ImageButton>(R.id.game_details_button_page_2)?.setOnClickListener { viewPager.currentItem = 2 }

            if (savedInstanceState == null) {
                // TODO Linked hashes requires login
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.GetGameInfoAndUserProgress(applicationContext, MainActivity.raUser, game.id) { parseGameInfoUserProgress(it) }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("game", game)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        game = savedInstanceState.getParcelable("game") ?: Game()
        populateElements()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.action_forum -> {
                val forumUrl = Consts.BASE_URL + "/" + Consts.FORUM_POSTFIX + game.forumTopicID
                val forumIntent = Intent(Intent.ACTION_VIEW)
                forumIntent.data = Uri.parse(forumUrl)
                startActivity(forumIntent)
            }
            R.id.action_webpage -> {
                val raUrl = Consts.BASE_URL + "/" + Consts.GAME_POSTFIX + "/" + game.id
                val raIntent = Intent(Intent.ACTION_VIEW)
                raIntent.data = Uri.parse(raUrl)
                startActivity(raIntent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private suspend fun parseGameInfoUserProgress(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                withContext(Default) {
                    try {
                        val reader = JSONObject(response.second)
                        game.title = Jsoup.parse(reader.getString("Title").trim { it <= ' ' }).text()
                        if (game.title.contains(", The"))
                            game.title = "The " + game.title.indexOf(", The").let {
                                game.title.substring(0, it) + game.title.substring(it + 5)
                            }
                        game.console = reader.getString("ConsoleName")
                        game.imageIcon = reader.getString("ImageIcon")
                        game.developer = reader.getString("Developer")
                        game.developer = if (game.developer == "null") "????" else Jsoup.parse(game.developer).text()
                        game.publisher = reader.getString("Publisher")
                        game.publisher = if (game.publisher == "null") "????" else Jsoup.parse(game.publisher).text()
                        game.genre = reader.getString("Genre")
                        game.genre = if (game.genre == "null") "????" else Jsoup.parse(game.genre).text()
                        game.released = reader.getString("Released")
                        game.released = if (game.released == "null") "????" else Jsoup.parse(game.released).text()
                        game.forumTopicID = reader.getString("ForumTopicID")
                    } catch (e: JSONException) {
                        Log.e(TAG, "unable to parse game details", e)
                    }
                }
                withContext(Main) {
                    populateElements()
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    private fun populateElements() {
        title = "${game.title} (${game.console})"
        Picasso.get()
                .load(Consts.BASE_URL + game.imageIcon)
                .placeholder(R.drawable.game_placeholder)
                .into(findViewById<ImageView>(R.id.game_details_image_icon))
        findViewById<TextView>(R.id.game_details_developer).text = getString(R.string.developed, game.developer)
        findViewById<TextView>(R.id.game_details_publisher).text = getString(R.string.published, game.publisher)
        findViewById<TextView>(R.id.game_details_genre).text = getString(R.string.genre, game.genre)
        findViewById<TextView>(R.id.game_details_release_date).text = getString(R.string.released, game.released)
    }

    companion object {
        private val TAG = GameDetailsActivity::class.java.simpleName
    }
}