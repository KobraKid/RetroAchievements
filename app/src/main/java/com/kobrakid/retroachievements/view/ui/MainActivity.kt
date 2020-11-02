package com.kobrakid.retroachievements.view.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import com.kobrakid.retroachievements.databinding.ActivityMainBinding
import com.kobrakid.retroachievements.databinding.NavHeaderBinding
import com.kobrakid.retroachievements.model.User
import com.kobrakid.retroachievements.viewmodel.MainViewModel
import com.squareup.picasso.Picasso

/**
 * The entry point for the app, and the Activity that manages most of the basic Fragments used
 * throughout the app.
 */
class MainActivity : AppCompatActivity() {

    private val navMenuItems = setOf(
            R.id.homeFragment,
            R.id.consoleListFragment,
            R.id.rankingsFragment,
            R.id.settingsFragment,
            R.id.aboutFragment)
    private lateinit var navController: NavController
    private lateinit var api: RetroAchievementsApi
    private lateinit var db: RetroAchievementsDatabase
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    val user: User? get() = viewModel.user.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get saved preferences
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
        setTheme(sharedPref.getInt(getString(R.string.theme_setting), R.style.BlankTheme))

        // Initialize API and Database
        api = RetroAchievementsApi.getInstance(this)
        api.setCredentials(sharedPref.getString(getString(R.string.ra_user), "")!!, sharedPref.getString(getString(R.string.ra_api_key), "")!!)
        db = RetroAchievementsDatabase.getInstance(this)

        // Get Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHeader = NavHeaderBinding.bind(binding.navView.getHeaderView(0))

        // Get View Model
        viewModel.user.observe(this) { user ->
            if (user.username.isEmpty()) { // Set defaults
                navHeader.navUsername.text = getString(R.string.login_prompt)
                Picasso.get()
                        .load(R.drawable.user_placeholder)
                        .into(navHeader.navProfilePicture)
                navHeader.navStats.visibility = View.GONE
            } else {
                navHeader.navUsername.text = user.username
                Picasso.get()
                        .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + user.username + ".png")
                        .placeholder(R.drawable.user_placeholder)
                        .into(navHeader.navProfilePicture)
                navHeader.navStats.apply {
                    text = getString(R.string.score_rank, user.totalPoints, user.rank)
                    visibility = if (user.rank.isNotEmpty() && user.totalPoints.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        // Set up UI
        setSupportActionBar(binding.toolbar)
        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
        binding.navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, AppBarConfiguration(navMenuItems, binding.drawerLayout))
        navHeader.navProfilePicture.setOnClickListener {
            binding.drawerLayout.closeDrawers()
            navController.navigate(R.id.loginFragment)
        }

        viewModel.setUsername(sharedPref.getString(getString(R.string.ra_user), ""))
    }

    override fun onResume() {
        super.onResume()
        binding.drawerLayout.closeDrawers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (navController.currentBackStackEntry?.destination?.id in navMenuItems)
                binding.drawerLayout.openDrawer(GravityCompat.START)
            else
                navController.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    fun setCredentials(user: String, apiKey: String) {
        viewModel.setUsername(user)
        api.setCredentials(user, apiKey)
    }

}