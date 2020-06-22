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
import com.kobrakid.retroachievements.*
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*

class HomeFragment : Fragment(), RAAPICallback, View.OnClickListener {
    // TODO Only call API when the view is first started, or when the user asks for a manual refresh
    private var hasPopulatedGames = false
    private var hasPopulatedMasteries = false

    // Mastered games
    private val masteryIDs = ArrayList<String>()
    private val masteryIcons = ArrayList<String>()
    private val masteryGold = ArrayList<Boolean>()

    // Recent games summary
    private val summaryIDs = ArrayList<String>()
    private val summaryTitles = ArrayList<String>()
    private val summaryIcons = ArrayList<String>()
    private val summaryScores = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        activity?.title = "Home"
        if (MainActivity.ra_user != null) {
            Log.i(TAG, "ra_user != null")
            if (savedInstanceState == null) {
                Log.i(TAG, "savedInstanceState == null")
                Log.i(TAG, masteryIDs.size.toString() + " " + summaryTitles.size)
                // Call API in the case of no saved instance or recreation after login
                val apiConnection = (Objects.requireNonNull(activity) as MainActivity).apiConnection
                hasPopulatedGames = false
                hasPopulatedMasteries = false
                apiConnection.GetUserWebProfile(MainActivity.ra_user, this)
                apiConnection.GetUserSummary(MainActivity.ra_user, NUM_RECENT_GAMES, this)
            } else {
                Log.i(TAG, "savedInstanceState != null")
                populateUserInfo(view)
                populateMasteries(view)
                populateGames(view)
            }
        } else {
            Log.i(TAG, "ra_user == null")
            view.findViewById<View>(R.id.home_username).visibility = View.VISIBLE
        }
        return view
    }

    override fun onClick(view: View) {
        val intent = Intent(this.activity, GameDetailsActivity::class.java)
        val extras = Bundle()
        extras.putString("GameID",
                "" + view.id)
        intent.putExtras(extras)
        startActivity(intent)
    }

    /**
     * Handled callbacks:
     * RESPONSE_GET_USER_WEB_PROFILE
     * RESPONSE_GET_USER_SUMMARY
     *
     * @param responseCode The corresponding response code, which informs a callback on what kind of
     * API call was made.
     * @param response     The raw String response that was retrieved from the API call.
     */
    override fun callback(responseCode: Int, response: String) {
        val reader: JSONObject
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_WEB_PROFILE) {
            val document = Jsoup.parse(response)
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
            if (view != null) {
                populateMasteries(view)
            }
        }
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_SUMMARY) {
            try {
                reader = JSONObject(response)
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
                e.printStackTrace()
            }
            if (view != null) {
                populateGames(view)
                populateUserInfo(view)
            }
        }
    }

    private fun populateUserInfo(view: View?) {
        if (MainActivity.ra_user != null) {
            Log.i(TAG, "populateUserInfo")
            (view!!.findViewById<View>(R.id.home_username) as TextView).text = MainActivity.ra_user
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + MainActivity.ra_user + ".png")
                    .into(view.findViewById<View>(R.id.home_profile_picture) as ImageView)
        }
        (view!!.findViewById<View>(R.id.home_stats) as TextView).text = getString(R.string.nav_rank_score, MainActivity.score, MainActivity.rank)
        view.findViewById<View>(R.id.home_username).visibility = View.VISIBLE
        view.findViewById<View>(R.id.home_stats).visibility = View.VISIBLE
    }

    private fun populateMasteries(view: View?) {
        val masteries = view!!.findViewById<LinearLayout>(R.id.masteries)
        masteries.removeAllViews()
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        params.marginEnd = 1
        for (i in masteryIDs.indices) {
            val imageView = ImageView(context)
            imageView.layoutParams = params
            imageView.adjustViewBounds = true
            if (masteryGold[i]) imageView.background = activity?.getDrawable(R.drawable.image_view_border)
            Picasso.get()
                    .load(Consts.BASE_URL + masteryIcons[i])
                    .placeholder(R.drawable.favicon)
                    .into(imageView)
            masteries.addView(imageView)
            try {
                imageView.id = masteryIDs[i].toInt()
            } catch (e: NumberFormatException) {
                // This happens when parsing achievements like connecting one's account to FB,
                // developing achievements, etc.
                Log.e(TAG, "Trophy was not a valid RA game.", e)
            }
            imageView.setOnClickListener(this)
        }
        masteries.visibility = View.VISIBLE
        hasPopulatedMasteries = true
    }

    private fun populateGames(view: View?) {
        // Fill out recently played games list
        val recentGames = view!!.findViewById<LinearLayout>(R.id.home_recent_games)
        if (recentGames.childCount > 1) recentGames.removeViews(0, recentGames.childCount - 1)
        for (i in summaryIDs.indices) {
            val game = View.inflate(context, R.layout.view_holder_game_summary, null) as ConstraintLayout
            Picasso.get()
                    .load(Consts.BASE_URL + summaryIcons[i])
                    .into(game.findViewById<View>(R.id.game_summary_image_icon) as ImageView)
            (game.findViewById<View>(R.id.game_summary_title) as TextView).text = summaryTitles[i]
            (game.findViewById<View>(R.id.game_summary_stats) as TextView).text = summaryScores[i]
            (game.findViewById<View>(R.id.game_summary_game_id) as TextView).text = summaryIDs[i]
            recentGames.addView(game, i)
        }
        view.findViewById<View>(R.id.home_view_more).visibility = View.VISIBLE
        hasPopulatedGames = true
    }

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        const val NUM_RECENT_GAMES = 5
    }
}