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
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.findFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.fragment.HomeFragment
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
class MainActivity : AppCompatActivity() {

    private val navMenuItems = setOf(
            R.id.homeFragment,
            R.id.consoleListFragment,
            R.id.leaderboardsFragment,
            R.id.settingsFragment,
            R.id.aboutFragment)
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val drawer: DrawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val navView: NavigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get saved preferences
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(sharedPref.getInt(getString(R.string.theme_setting), R.style.BlankTheme))
        raUser = sharedPref.getString(getString(R.string.ra_user), "")!!
        raApiKey = sharedPref.getString(getString(R.string.ra_api_key), "")!!
        RetroAchievementsApi.setCredentials(raUser, raApiKey)

        // Set up UI
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<Toolbar>(R.id.toolbar)
        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        navView.setupWithNavController(navController)
        appBarConfiguration = AppBarConfiguration(navMenuItems, drawer)
        setupActionBarWithNavController(navController, appBarConfiguration)

        if (savedInstanceState == null) {
            if (raUser.isNotEmpty())
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.GetUserRankAndScore(applicationContext, raUser) { parseRankScore(it) }
                }
        } else {
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
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.GetUserWebProfile(applicationContext, raUser) {
                        findViewById<View>(R.id.nav_host_fragment).findFragment<HomeFragment>().parseUserWebProfile(findViewById(R.id.home_scrollview), it)
                    }
                    RetroAchievementsApi.GetUserSummary(applicationContext, raUser, HomeFragment.NUM_RECENT_GAMES) {
                        findViewById<View>(R.id.nav_host_fragment).findFragment<HomeFragment>().parseUserSummary(findViewById(R.id.home_scrollview), it)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (navController.currentBackStackEntry?.destination?.id in navMenuItems)
                drawer.openDrawer(GravityCompat.START)
            else
                navController.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
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
        navView.getHeaderView(0).findViewById<ImageView>(R.id.nav_profile_picture).setOnClickListener {
            drawer.closeDrawers()
            navController.navigate(R.id.loginFragment)
        }
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

    fun setCredentials(user: String, apiKey: String) {
        raUser = user
        raApiKey = apiKey
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        var raUser: String = ""
        private var raApiKey: String = ""
        var rank: String = ""
        var score: String = ""
    }
}