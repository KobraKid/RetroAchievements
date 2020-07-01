package com.kobrakid.retroachievements.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.ThemeManager.getTheme
import com.kobrakid.retroachievements.fragment.*
import com.kobrakid.retroachievements.fragment.SettingsFragment.OnFragmentInteractionListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * The entry point for the app, and the Activity that manages most of the basic Fragments used
 * throughout the app.
 */
class MainActivity : AppCompatActivity(), OnFragmentInteractionListener {

    private var fragment: Fragment? = null
    private var activeFragmentTag = "HomeFragment"
    private val drawer: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navView: NavigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up UI
        title = "Home"
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        navView.setNavigationItemSelectedListener { item: MenuItem -> selectDrawerItem(item) }

        // Get saved preferences
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(getTheme(this, sharedPref))
        raUser = sharedPref.getString(getString(R.string.ra_user), "")!!
        raApiKey = sharedPref.getString(getString(R.string.ra_api_key), "")!!
        RetroAchievementsApi.setCredentials(raUser, raApiKey)

        if (savedInstanceState == null) {
            // Set up home fragment
            activeFragmentTag = "HomeFragment"
            supportFragmentManager.beginTransaction().replace(R.id.flContent, HomeFragment(), activeFragmentTag).commit()
            // Set up drawer
            if (raUser.isNotEmpty())
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.GetUserRankAndScore(applicationContext, raUser) { parseRankScore(it) }
                }
        } else {
            // Reclaim reference to active fragment
            activeFragmentTag = savedInstanceState.getString("ActiveFragmentTag") ?: "HomeFragment"
            fragment = supportFragmentManager.findFragmentByTag(activeFragmentTag)
            populateViews()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Consts.BEGIN_LOGIN) when (resultCode) {
            Consts.SUCCESS -> {
                Log.d(TAG, "LOGIN SUCCESS")
                raUser = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getString(getString(R.string.ra_user), "")!!
                raApiKey = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getString(getString(R.string.ra_api_key), "")!!
                Log.v(TAG, "Logging in as $raUser")
                RetroAchievementsApi.setCredentials(raUser, raApiKey)
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.GetUserRankAndScore(applicationContext, raUser) { parseRankScore(it) }
                }
                if (fragment is HomeFragment) {
                    CoroutineScope(IO).launch {
                        RetroAchievementsApi.GetUserWebProfile(applicationContext, raUser) {
                            (fragment as HomeFragment).parseUserWebProfile(findViewById(R.id.home_scrollview), it)
                        }
                        RetroAchievementsApi.GetUserSummary(applicationContext, raUser, HomeFragment.NUM_RECENT_GAMES) {
                            (fragment as HomeFragment).parseUserSummary(findViewById(R.id.home_scrollview), it)
                        }
                    }
                }
            }
            Consts.CANCELLED -> Log.d(TAG, "LOGIN CANCELLED")
            Consts.FAILURE -> Log.d(TAG, "LOGIN FAILED")
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        drawer.closeDrawers()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("ActiveFragmentTag", activeFragmentTag)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /* Navigation-related functions */
    private fun selectDrawerItem(item: MenuItem): Boolean {
        val fragment: Fragment =
                when (item.itemId) {
                    R.id.nav_console_list_fragment -> ConsoleListFragment()
                    R.id.nav_leaderboards_fragment -> LeaderboardsFragment()
                    R.id.nav_settings_fragment -> SettingsFragment()
                    R.id.nav_about_fragment -> AboutFragment()
                    else -> HomeFragment()
                }
        activeFragmentTag = fragment.javaClass.simpleName
        supportFragmentManager.beginTransaction().replace(R.id.flContent, fragment, activeFragmentTag).commit()
        item.isChecked = true
        drawer.closeDrawers()
        return true
    }

    private suspend fun parseRankScore(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_USER_RANK_AND_SCORE -> {
                withContext(Default) {
                    try {
                        val reader = JSONObject(response.second)
                        score = reader.getString("Score")
                        rank = reader.getString("Rank")
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse user rank/score", e)
                    }
                }
                withContext(Main) {
                    populateViews()
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    private fun populateViews() {
        navView.getHeaderView(0).findViewById<TextView>(R.id.nav_username).text = raUser
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + raUser + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(navView.getHeaderView(0).findViewById<ImageView>(R.id.nav_profile_picture))
        navView.getHeaderView(0).findViewById<TextView>(R.id.nav_stats).text = getString(R.string.score_rank, score, rank)
        if (rank.isNotEmpty() && score.isNotEmpty())
            navView.getHeaderView(0).findViewById<View>(R.id.nav_stats).visibility = View.VISIBLE
        else
            navView.getHeaderView(0).findViewById<View>(R.id.nav_stats).visibility = View.GONE
    }

    fun showGameDetails(view: View) {
        startActivity(Intent(this, GameDetailsActivity::class.java).apply {
            putExtra("GameID", view.findViewById<TextView>(R.id.game_summary_game_id).text.toString())
        })
    }

    fun showLogin(@Suppress("UNUSED_PARAMETER") view: View?) {
        drawer.closeDrawers()
        startActivityForResult(Intent(this, LoginActivity::class.java), Consts.BEGIN_LOGIN)
    }

    fun showRecentGames(@Suppress("UNUSED_PARAMETER") view: View?) {
        startActivity(Intent(this, RecentGamesActivity::class.java))
    }

    fun toggleUsers(topTenUsersToggle: View) {
        val topTenUsers = findViewById<View>(R.id.leaderboards_users)
        topTenUsers.z = -1f
        if (topTenUsers.visibility == View.GONE) {
            (topTenUsersToggle as ImageButton).setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_down))
            topTenUsers
                    .animate()
                    .alpha(1.0f)
                    .translationYBy(topTenUsers.height.toFloat())
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            super.onAnimationEnd(animation)
                            topTenUsers.visibility = View.VISIBLE
                        }
                    })
        } else {
            (topTenUsersToggle as ImageButton).setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_up))
            topTenUsers
                    .animate()
                    .alpha(0.0f)
                    .translationYBy(-topTenUsers.height.toFloat())
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            topTenUsers.visibility = View.GONE
                        }
                    })
        }
    }

    override fun logout(view: View?) {
        (fragment as SettingsFragment).logout()
    }

    override fun applySettings(view: View?) {
        (fragment as SettingsFragment).applySettings()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        var raUser: String = ""
        private var raApiKey: String = ""
        var rank: String = ""
        var score: String = ""
    }
}