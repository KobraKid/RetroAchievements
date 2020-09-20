package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
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

class HomeFragment : Fragment(), View.OnClickListener {

    // Mastered games
    private val masteryIDs = mutableListOf<String>()
    private val masteryIcons = mutableListOf<String>()
    private val masteryGold = mutableListOf<Boolean>()

    // Recent games summary
    private val summaryIDs = mutableListOf<String>()
    private val summaryTitles = mutableListOf<String>()
    private val summaryIcons = mutableListOf<String>()
    private val summaryScores = mutableListOf<String>()

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        activity?.title = "Home"
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.home_view_more).setOnClickListener(this)
        view.findViewById<View>(R.id.home_username).visibility = View.VISIBLE
        if (MainActivity.raUser.isNotEmpty()) {
            view.findViewById<View>(R.id.home_scrollview).background = null
            (activity as MainActivity?)?.populateViews()
            if (savedInstanceState == null) {
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.GetUserWebProfile(context, MainActivity.raUser) { parseUserWebProfile(view, it) }
                    RetroAchievementsApi.GetUserSummary(context, MainActivity.raUser, NUM_RECENT_GAMES) { parseUserSummary(view, it) }
                }
            } else {
                fillUserWebProfile(view)
                fillUserSummary(view)
            }
        } else {
            view.findViewById<View>(R.id.home_scrollview).background = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_baseline_arrow_up_left) }
        }
    }

    private suspend fun parseUserWebProfile(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_WEB_PROFILE -> {
                // Parse response on the Default thread
                withContext(Default) {
                    val document = Jsoup.parse(response.second)
                    val elements = document.select("div[class=trophyimage]")
                    masteryIDs.clear()
                    masteryIcons.clear()
                    masteryGold.clear()
                    for (element in elements) {
                        var gameID = element.selectFirst("a[href]").attr("href")
                        if (gameID.length >= 6) {
                            gameID = gameID.substring(6)
                            val image = element.selectFirst("img[src]")
                            val imageIcon = image.attr("src")
                            masteryIDs.add(gameID)
                            masteryIcons.add(imageIcon)
                            masteryGold.add(image.className() == "goldimage")
                        }
                    }
                }
                withContext(Main) {
                    fillUserWebProfile(view)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private suspend fun parseUserSummary(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_USER_SUMMARY -> {
                withContext(Default) {
                    try {
                        val reader = JSONObject(response.second)
                        summaryIDs.clear()
                        summaryIcons.clear()
                        summaryTitles.clear()
                        summaryScores.clear()
                        val recentlyPlayed = reader.getJSONArray("RecentlyPlayed")
                        for (i in 0 until recentlyPlayed.length()) {
                            val gameObj = recentlyPlayed.getJSONObject(i)
                            val gameID = gameObj.getString("GameID")
                            val awards = reader.getJSONObject("Awarded").getJSONObject(gameID)
                            val possibleAchievements = awards.getString("NumPossibleAchievements")
                            val possibleScore = awards.getString("PossibleScore")
                            val awardedAchieve = awards.getString("NumAchieved").toInt()
                            val awardedAchieveHardcore = awards.getString("NumAchievedHardcore").toInt()
                            val score = if (awardedAchieve > awardedAchieveHardcore) awards.getString("ScoreAchieved") else awards.getString("ScoreAchievedHardcore")
                            summaryIDs.add(gameID)
                            summaryIcons.add(gameObj.getString("ImageIcon"))
                            summaryTitles.add(gameObj.getString("Title"))
                            summaryScores.add(resources.getString(R.string.game_stats,
                                    awardedAchieve.coerceAtLeast(awardedAchieveHardcore).toString(),
                                    possibleAchievements,
                                    score,
                                    possibleScore))
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, response.second, e)
                    }
                }
                withContext(Main) {
                    fillUserSummary(view)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private fun fillUserWebProfile(view: View) {
        // Populate UI on the Main thread
        val masteries = view.findViewById<LinearLayout>(R.id.masteries)
        masteries.removeAllViews()
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        params.marginEnd = 1
        for (i in masteryIDs.indices) {
            val imageView = ImageView(context)
            imageView.layoutParams = params
            imageView.adjustViewBounds = true
            if (masteryGold[i]) imageView.background = context?.let { ContextCompat.getDrawable(it, R.drawable.image_view_border) }
            Picasso.get()
                    .load(Consts.BASE_URL + masteryIcons[i])
                    .placeholder(R.drawable.game_placeholder)
                    .into(imageView)
            masteries.addView(imageView)
            try {
                imageView.id = masteryIDs[i].toInt()
                imageView.setOnClickListener(this@HomeFragment)
            } catch (e: NumberFormatException) {
                // This happens when parsing achievements like connecting one's account to FB,
                // developing achievements, etc.
                Log.e(TAG, "Trophy was not a valid RA game.", e)
            }
        }
        masteries.visibility = View.VISIBLE
    }

    private fun fillUserSummary(view: View) {
        view.findViewById<TextView>(R.id.home_username).text = MainActivity.raUser
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + MainActivity.raUser + ".png")
                .placeholder(R.drawable.favicon)
                .into(view.findViewById<ImageView>(R.id.home_profile_picture))
        view.findViewById<TextView>(R.id.home_stats).apply {
            text = getString(R.string.score_rank, MainActivity.score, MainActivity.rank)
            visibility = View.VISIBLE
        }
        // Fill out recently played games list
        with(view.findViewById<LinearLayout>(R.id.home_recent_games)) {
            if (childCount > 1)
                removeViews(0, childCount - 1)
            for (i in summaryIDs.indices) {
                val game = View.inflate(context, R.layout.view_holder_game_summary, null) as ConstraintLayout
                Picasso.get()
                        .load(Consts.BASE_URL + summaryIcons[i])
                        .placeholder(R.drawable.game_placeholder)
                        .into(game.findViewById<ImageView>(R.id.game_summary_image_icon))
                game.findViewById<TextView>(R.id.game_summary_title).text = summaryTitles[i]
                game.findViewById<TextView>(R.id.game_summary_stats).text = summaryScores[i]
                game.findViewById<TextView>(R.id.game_summary_game_id).text = summaryIDs[i]
                game.id = summaryIDs[i].toInt()
                game.setOnClickListener(this@HomeFragment)
                addView(game, i)
            }
        }
        view.findViewById<View>(R.id.home_view_more).visibility = View.VISIBLE
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.home_view_more -> navController.navigate(R.id.action_homeFragment_to_recentGamesFragment)
            else -> navController.navigate(HomeFragmentDirections.actionHomeFragmentToGameDetailsFragment(view.id.toString()))
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + HomeFragment::class.java.simpleName
        const val NUM_RECENT_GAMES = 5
    }
}