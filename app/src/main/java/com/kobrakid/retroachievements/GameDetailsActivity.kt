package com.kobrakid.retroachievements

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
import com.kobrakid.retroachievements.ThemeManager.getTheme
import com.kobrakid.retroachievements.adapter.GameDetailsPagerAdapter
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

/**
 * This class will display detailed information about a single game.
 */
class GameDetailsActivity : AppCompatActivity(), RAAPICallback {

    private var apiConnectionDeprecated: RAAPIConnectionDeprecated? = null
    private var gameID: String? = null
    private var console: String? = null
    private var imageIcon: String? = null
    private var title: String? = null
    private var developer: String? = null
    private var publisher: String? = null
    private var genre: String? = null
    private var released: String? = null
    private var forumTopicID: String? = null
    private var isActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up theme and title bar
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(getTheme(this, sharedPref))
        setContentView(R.layout.activity_game_details)
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        if (intent.extras != null) {
            gameID = intent.extras?.getString("GameID")

            // Set up API connection
            apiConnectionDeprecated = RAAPIConnectionDeprecated(this)
            val viewPager = findViewById<ViewPager>(R.id.game_details_view_pager)
            viewPager.adapter = GameDetailsPagerAdapter(supportFragmentManager, gameID!!)
            viewPager.offscreenPageLimit = GameDetailsPagerAdapter.GAME_DETAILS_PAGES - 1
            findViewById<ImageButton>(R.id.game_details_button_page_0).setOnClickListener { viewPager.currentItem = 0 }
            findViewById<ImageButton>(R.id.game_details_button_page_1).setOnClickListener { viewPager.currentItem = 1 }
            findViewById<ImageButton>(R.id.game_details_button_page_2).setOnClickListener { viewPager.currentItem = 2 }
            if (savedInstanceState?.getString("forumTopicID") == null) {
                apiConnectionDeprecated?.GetGameInfoAndUserProgress(MainActivity.raUser, gameID, this)
                // TODO Linked hashes requires login
                //  apiConnection.GetLinkedHashes(gameID, this);
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("gameID", gameID)
        outState.putString("console", console)
        outState.putString("imageIcon", imageIcon)
        outState.putString("title", title)
        outState.putString("developer", developer)
        outState.putString("publisher", publisher)
        outState.putString("genre", genre)
        outState.putString("released", released)
        outState.putString("forumTopicID", forumTopicID)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        gameID = savedInstanceState.getString("gameID")
        console = savedInstanceState.getString("console")
        imageIcon = savedInstanceState.getString("imageIcon")
        title = savedInstanceState.getString("title")
        developer = savedInstanceState.getString("developer")
        publisher = savedInstanceState.getString("publisher")
        genre = savedInstanceState.getString("genre")
        released = savedInstanceState.getString("released")
        forumTopicID = savedInstanceState.getString("forumTopicID")
        if (gameID != null) populateElements()
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var handled = false
        when (id) {
            android.R.id.home -> {
                handled = true
                finish()
            }
            R.id.action_forum -> {
                handled = true
                val forumUrl = Consts.BASE_URL + "/" + Consts.FORUM_POSTFIX + forumTopicID
                val forumIntent = Intent(Intent.ACTION_VIEW)
                forumIntent.data = Uri.parse(forumUrl)
                startActivity(forumIntent)
            }
            R.id.action_webpage -> {
                handled = true
                val raUrl = Consts.BASE_URL + "/" + Consts.GAME_POSTFIX + "/" + gameID
                val raIntent = Intent(Intent.ACTION_VIEW)
                raIntent.data = Uri.parse(raUrl)
                startActivity(raIntent)
            }
        }
        return if (handled) true else super.onOptionsItemSelected(item)
    }

    override fun callback(responseCode: Int, response: String) {
        if (!isActive) return
        if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS) {
            val reader: JSONObject
            try {
                reader = JSONObject(response)
                title = Jsoup.parse(reader.getString("Title").trim { it <= ' ' }).text()
                if (title?.contains(", The") == true)
                    title = "The " + title?.indexOf(", The")?.let { title?.substring(0, it) + title?.substring(it + 5) }
                console = reader.getString("ConsoleName")
                imageIcon = reader.getString("ImageIcon")
                developer = reader.getString("Developer")
                developer = if (developer == "null") "????" else Jsoup.parse(developer).text()
                publisher = reader.getString("Publisher")
                publisher = if (publisher == "null") "????" else Jsoup.parse(publisher).text()
                genre = reader.getString("Genre")
                genre = if (genre == "null") "????" else Jsoup.parse(genre).text()
                released = reader.getString("Released")
                released = if (released == "null") "????" else Jsoup.parse(released).text()
                forumTopicID = reader.getString("ForumTopicID")
                populateElements()
            } catch (e: JSONException) {
                Log.e(TAG, "Unable to parse game details", e)
            }
        }
    }

    private fun populateElements() {
        setTitle("$title ($console)")
        Picasso.get()
                .load(Consts.BASE_URL + imageIcon)
                .placeholder(R.drawable.game_placeholder)
                .into(findViewById<ImageView>(R.id.game_details_image_icon))
        findViewById<TextView>(R.id.game_details_developer).text = getString(R.string.developed, developer)
        findViewById<TextView>(R.id.game_details_publisher).text = getString(R.string.published, publisher)
        findViewById<TextView>(R.id.game_details_genre).text = getString(R.string.genre, genre)
        findViewById<TextView>(R.id.game_details_release_date).text = getString(R.string.released, released)
    }

    companion object {
        private val TAG = GameDetailsActivity::class.java.simpleName
    }
}