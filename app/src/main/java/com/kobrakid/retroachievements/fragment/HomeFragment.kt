package com.kobrakid.retroachievements.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.activity.GameDetailsActivity
import com.kobrakid.retroachievements.activity.MainActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        activity?.title = "Home"
        if (MainActivity.raUser.isNotEmpty()) {
            if (savedInstanceState == null) {
                val ctx = context?.applicationContext
                CoroutineScope(IO).launch {
                    if (ctx != null) {
                        RetroAchievementsApi.GetUserWebProfile(ctx, MainActivity.raUser) { parseUserWebProfile(view, it) }
                        RetroAchievementsApi.GetUserSummary(ctx, MainActivity.raUser, NUM_RECENT_GAMES) { parseUserSummary(view, it) }
                    }
                }
            } else {
                fillUserWebProfile(view)
                fillUserSummary(view)
            }
        } else {
            view.findViewById<View>(R.id.home_username).visibility = View.VISIBLE
        }
        return view
    }

    suspend fun parseUserWebProfile(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
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
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    suspend fun parseUserSummary(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
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
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    private fun fillUserWebProfile(view: View) {
        // Populate UI on the Main thread
        val masteries = view.findViewById<LinearLayout>(R.id.masteries)
        masteries.removeAllViews()
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        params.marginEnd = 1
        val ctx = context
        for (i in masteryIDs.indices) {
            if (ctx == null) break
            val imageView = ImageView(context)
            imageView.layoutParams = params
            imageView.adjustViewBounds = true
            if (masteryGold[i]) imageView.background = activity?.getDrawable(R.drawable.image_view_border)
            Picasso.get()
                    .load(Consts.BASE_URL + masteryIcons[i])
                    .placeholder(R.drawable.game_placeholder)
                    .into(imageView)
            masteries.addView(imageView)
            try {
                imageView.id = masteryIDs[i].toInt()
            } catch (e: NumberFormatException) {
                // This happens when parsing achievements like connecting one's account to FB,
                // developing achievements, etc.
                Log.e(TAG, "Trophy was not a valid RA game.", e)
            }
            imageView.setOnClickListener(this@HomeFragment)
        }
        masteries.visibility = View.VISIBLE
    }

    private fun fillUserSummary(view: View) {
        view.findViewById<TextView>(R.id.home_username).text = MainActivity.raUser
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + MainActivity.raUser + ".png")
                .placeholder(R.drawable.favicon)
                .into(view.findViewById<ImageView>(R.id.home_profile_picture))
        view.findViewById<TextView>(R.id.home_stats).text = getString(R.string.score_rank, MainActivity.score, MainActivity.rank)
        view.findViewById<View>(R.id.home_username).visibility = View.VISIBLE
        view.findViewById<View>(R.id.home_stats).visibility = View.VISIBLE
        // Fill out recently played games list
        val recentGames = view.findViewById<LinearLayout>(R.id.home_recent_games)
        if (recentGames.childCount > 1)
            recentGames.removeViews(0, recentGames.childCount - 1)
        for (i in summaryIDs.indices) {
            val game = View.inflate(context, R.layout.view_holder_game_summary, null) as ConstraintLayout
            Picasso.get()
                    .load(Consts.BASE_URL + summaryIcons[i])
                    .placeholder(R.drawable.game_placeholder)
                    .into(game.findViewById<ImageView>(R.id.game_summary_image_icon))
            game.findViewById<TextView>(R.id.game_summary_title).text = summaryTitles[i]
            game.findViewById<TextView>(R.id.game_summary_stats).text = summaryScores[i]
            game.findViewById<TextView>(R.id.game_summary_game_id).text = summaryIDs[i]
            recentGames.addView(game, i)
        }
        view.findViewById<View>(R.id.home_view_more).visibility = View.VISIBLE
    }

    override fun onClick(view: View) {
        val intent = Intent(this.activity, GameDetailsActivity::class.java)
        val extras = Bundle()
        extras.putString("GameID", view.id.toString())
        intent.putExtras(extras)
        startActivity(intent)
    }

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        const val NUM_RECENT_GAMES = 5
    }
}