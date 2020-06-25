package com.kobrakid.retroachievements

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
import com.kobrakid.retroachievements.ThemeManager.getTheme
import com.kobrakid.retroachievements.fragment.*
import com.kobrakid.retroachievements.fragment.SettingsFragment.OnFragmentInteractionListener
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 * The entry point for the app, and the Activity that manages most of the basic Fragments used
 * throughout the app.
 */
class MainActivity : AppCompatActivity(), RAAPICallback, OnFragmentInteractionListener {

    val apiConnectionDeprecated: RAAPIConnectionDeprecated by lazy { RAAPIConnectionDeprecated(this) }
    private var fragment: Fragment? = null
    private var activeFragmentTag: String? = null
    private val drawer: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navView: NavigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Try to get saved preferences and log in
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(getTheme(this, sharedPref))
        raUser = sharedPref.getString(getString(R.string.ra_user), "")!!
        setContentView(R.layout.activity_main)
        title = "Home"

        // Set up UI
        setSupportActionBar(findViewById(R.id.main_toolbar))
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        navView.setNavigationItemSelectedListener { item: MenuItem -> selectDrawerItem(item) }

        if (savedInstanceState == null) {
            RetroAchievementsApi.setCredentials(this)
            if (raUser.isNotEmpty())
                apiConnectionDeprecated.GetUserRankAndScore(raUser, this)
            // Set up home fragment
            activeFragmentTag = "HomeFragment"
            supportFragmentManager.beginTransaction().replace(R.id.flContent, HomeFragment(), activeFragmentTag).commit()
        } else {
            // Reclaim reference to active fragment
            activeFragmentTag = savedInstanceState.getString("ActiveFragmentTag")
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
                Log.v(TAG, "Logging in as $raUser")
                RetroAchievementsApi.setCredentials(this)
                apiConnectionDeprecated.reinitializeAPIConnection()
                apiConnectionDeprecated.GetUserRankAndScore(raUser, this)
                if (fragment is HomeFragment) {
                    CoroutineScope(Dispatchers.IO).launch {
                        RetroAchievementsApi.GetUserWebProfile((fragment as HomeFragment).context!!, raUser) {
                            (fragment as HomeFragment).parseUserWebProfile(findViewById(R.id.home_scrollview), it)
                        }
                        RetroAchievementsApi.GetUserSummary((fragment as HomeFragment).context!!, raUser, HomeFragment.NUM_RECENT_GAMES) {
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
            if (fragment is ListsFragment && (fragment as ListsFragment).isShowingGames) {
                if (fragment?.view != null)
                    (fragment as ListsFragment).onBackPressed(fragment?.view!!)
            } else {
                drawer.openDrawer(GravityCompat.START)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // Special case for when showing a list of games -> back pressed returns to list of consoles
        if (fragment is ListsFragment && (fragment as ListsFragment).isShowingGames && fragment?.view != null)
            (fragment as ListsFragment).onBackPressed(fragment?.view!!)
        else
            super.onBackPressed()
    }

    override fun callback(responseCode: Int, response: String) {
        if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_USER_RANK_AND_SCORE) {
            val reader: JSONObject
            try {
                reader = JSONObject(response)
                score = reader.getString("Score")
                rank = reader.getString("Rank")
                populateViews()
            } catch (e: JSONException) {
                Log.e(TAG, response, e)
            }
        }
    }

    /* Navigation-related functions */
    private fun selectDrawerItem(item: MenuItem): Boolean {
        fragment = null
        val fragmentClass: Class<out Fragment>
        when (item.itemId) {
            R.id.nav_lists_fragment -> {
                fragmentClass = ListsFragment::class.java
                activeFragmentTag = "ListsFragment"
            }
            R.id.nav_leaderboards_fragment -> {
                fragmentClass = LeaderboardsFragment::class.java
                activeFragmentTag = "LeaderboardsFragment"
            }
            R.id.nav_settings_fragment -> {
                fragmentClass = SettingsFragment::class.java
                activeFragmentTag = "SettingsFragment"
            }
            R.id.nav_about_fragment -> {
                fragmentClass = AboutFragment::class.java
                activeFragmentTag = "AboutFragment"
            }
            R.id.nav_home_fragment -> {
                fragmentClass = HomeFragment::class.java
                activeFragmentTag = "HomeFragment"
            }
            else -> {
                fragmentClass = HomeFragment::class.java
                activeFragmentTag = "HomeFragment"
            }
        }
        fragment = try {
            fragmentClass.newInstance()
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "Error accessing Fragment's newInstance() method", e)
            return false
        } catch (e: InstantiationException) {
            Log.e(TAG, "Error instantiating the Fragment", e)
            return false
        }

        // Show new Fragment in main view
        supportFragmentManager.beginTransaction().replace(R.id.flContent, fragment!!, activeFragmentTag).commit()
        item.isChecked = true
        drawer.closeDrawers()
        return true
    }

    private fun populateViews() {
        navView.getHeaderView(0).findViewById<TextView>(R.id.nav_username).text = raUser
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + raUser + ".png")
                .into(navView.getHeaderView(0).findViewById<ImageView>(R.id.nav_profile_picture))
        if (rank != null && score != null) {
            navView.getHeaderView(0).findViewById<TextView>(R.id.nav_stats).text = getString(R.string.score_rank, score, rank)
            navView.getHeaderView(0).findViewById<View>(R.id.nav_stats).visibility = View.VISIBLE
        }
    }

    fun showGameDetails(view: View) {
        val intent = Intent(this, GameDetailsActivity::class.java)
        val extras = Bundle()
        extras.putString("GameID",
                view.findViewById<TextView>(R.id.game_summary_game_id).text.toString())
        intent.putExtras(extras)
        startActivity(intent)
    }

    /* Home Fragment Interface Implementation */
    fun showLogin(@Suppress("UNUSED_PARAMETER") view: View?) {
        drawer.closeDrawers()
        startActivityForResult(Intent(this, LoginActivity::class.java), Consts.BEGIN_LOGIN)
    }

    fun showRecentGames(@Suppress("UNUSED_PARAMETER") view: View?) {
        startActivity(Intent(this, RecentGamesActivity::class.java))
    }

    /* Leaderboards Fragment Interface Implementations */
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

    /* Settings Fragment Interface implementations */
    override fun logout(view: View?) {
        (fragment as SettingsFragment).logout()
    }

    override fun applySettings(view: View?) {
        (fragment as SettingsFragment).applySettings()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        var raUser: String = ""
        var rank: String? = null
        var score: String? = null
    }
}