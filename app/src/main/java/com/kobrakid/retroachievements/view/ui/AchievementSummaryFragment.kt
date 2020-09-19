package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Achievement
import com.kobrakid.retroachievements.view.adapter.AchievementAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * This class is responsible for displaying summary information on all the achievements for a
 * particular game.
 */
class AchievementSummaryFragment : Fragment(R.layout.view_pager_achievements_summary) {

    private data class AchievementTotals(
            var numEarned: Int,
            var numEarnedHC: Int,
            var totalAch: Int,
            var earnedPts: Int,
            var totalPts: Int,
            var earnedRatio: Int,
            var totalRatio: Int
    )

    private val adapter by lazy { AchievementAdapter(this, resources) }
    private val achievementTotals = AchievementTotals(0, 0, 0, 0, 0, 0, 0)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true
        val id = arguments?.getString("GameID", "0") ?: "0"
        val recyclerView: RecyclerView = view.findViewById(R.id.game_details_achievements_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        if (adapter.itemCount == 0) {
            CoroutineScope(IO).launch {
                RetroAchievementsApi.GetGameInfoAndUserProgress(context, MainActivity.raUser, id) { parseGameInfoAndUserProgress(view, it) }
            }
        } else populateViews(view)
    }

    private suspend fun parseGameInfoAndUserProgress(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                withContext(Default) {
                    try {
                        val reader = JSONObject(response.second)
                        if (reader.getString("NumAchievements") == "0") {
                            withContext(Main) {
                                view.findViewById<View>(R.id.game_details_achivements_earned)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_achievements_earned_text)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_achievements_earned_hc)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_achievements_earned_hc_text)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_points)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_points_text)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_points_total_text)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_ratio)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_ratio_text)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_ratio_total_text)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_loading_bar)?.visibility = View.GONE
                                view.findViewById<View>(R.id.game_details_no_achievements)?.visibility = View.VISIBLE
                            }
                        } else {
                            adapter.setNumDistinctCasual(reader.getString("NumDistinctPlayersCasual").toDouble())
                            val achievements = reader.getJSONObject("Achievements")
                            val displayOrder = mutableListOf<Int>()
                            val displayOrderEarned = mutableListOf<Int>()
                            var count: Int
                            var dateEarned: String
                            var earnedHC: Boolean
                            for (achievementID in achievements.keys()) {
                                val achievement = achievements.getJSONObject(achievementID)
                                when {
                                    achievement.has("DateEarnedHardcore") -> {
                                        achievementTotals.numEarned++
                                        achievementTotals.numEarnedHC++
                                        achievementTotals.earnedPts += 2 * achievement.getString("Points").toInt()
                                        achievementTotals.earnedRatio += achievement.getString("TrueRatio").toInt()
                                        dateEarned = achievement.getString("DateEarnedHardcore")
                                        displayOrderEarned.add(achievement.getString("DisplayOrder").toInt())
                                        displayOrderEarned.sort()
                                        count = displayOrderEarned.indexOf(achievement.getString("DisplayOrder").toInt())
                                        earnedHC = true
                                    }
                                    achievement.has("DateEarned") -> {
                                        achievementTotals.numEarned++
                                        achievementTotals.earnedPts += achievement.getString("Points").toInt()
                                        achievementTotals.earnedRatio += achievement.getString("TrueRatio").toInt()
                                        dateEarned = achievement.getString("DateEarned")
                                        displayOrderEarned.add(achievement.getString("DisplayOrder").toInt())
                                        displayOrderEarned.sort()
                                        count = displayOrderEarned.indexOf(achievement.getString("DisplayOrder").toInt())
                                        earnedHC = false
                                    }
                                    else -> {
                                        dateEarned = ""
                                        displayOrder.add(achievement.getString("DisplayOrder").toInt())
                                        displayOrder.sort()
                                        count = displayOrder.indexOf(achievement.getString("DisplayOrder").toInt()) + displayOrderEarned.size
                                        earnedHC = false
                                    }
                                }
                                if (dateEarned.isEmpty()) {
                                    dateEarned = "NoDate:$count"
                                    earnedHC = false
                                }
                                if (count == 0)
                                    count = achievementTotals.totalAch

                                // Workaround to avoid `Verifier rejected class` error
                                val newAchievement = Achievement(
                                        achievementID,
                                        achievement.getString("BadgeName"),
                                        achievement.getString("Title"),
                                        achievement.getString("Points"),
                                        achievement.getString("TrueRatio"),
                                        achievement.getString("Description"),
                                        dateEarned,
                                        earnedHC,
                                        achievement.getString("NumAwarded"),
                                        achievement.getString("NumAwardedHardcore"),
                                        achievement.getString("Author"),
                                        achievement.getString("DateCreated"),
                                        achievement.getString("DateModified"))

                                withContext(Main) {
                                    adapter.addAchievement(count, newAchievement)
                                }

                                achievementTotals.totalAch++
                                achievementTotals.totalPts += achievement.getString("Points").toInt()
                                achievementTotals.totalRatio += achievement.getString("TrueRatio").toInt()
                            }
                            withContext(Main) {
                                view.findViewById<View>(R.id.game_details_achievements_title)?.visibility = View.VISIBLE
                                view.findViewById<View>(R.id.game_details_achievements_earned_subtitle)?.visibility = View.VISIBLE
                                view.findViewById<View>(R.id.game_details_points_subtitle)?.visibility = View.VISIBLE
                                view.findViewById<View>(R.id.game_details_achievements_ratio_subtitle)?.visibility = View.VISIBLE
                            }
                        }
                    } catch (e: JSONException) {
                        if (e.toString().contains("Value null at Achievements of type org.json.JSONObject$1 cannot be converted to JSONObject"))
                            Log.d(TAG, "This game has no achievements")
                        else
                            Log.e(TAG, response.second, e)
                    }
                }
                withContext(Main) {
                    populateViews(view)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private fun populateViews(view: View) {
        // Achievement progress
        view.findViewById<CircularProgressBar>(R.id.game_details_achivements_earned).apply {
            maximum = achievementTotals.totalAch.toFloat()
            progress = achievementTotals.numEarned.toFloat()
        }
        view.findViewById<TextView>(R.id.game_details_achievements_earned_text).text = getString(R.string.percent, (achievementTotals.numEarned * 100).div(achievementTotals.totalAch))
        // Hardcore achievement progress
        view.findViewById<CircularProgressBar>(R.id.game_details_achievements_earned_hc).apply {
            maximum = achievementTotals.totalAch.toFloat()
            progress = achievementTotals.numEarnedHC.toFloat()
        }
        view.findViewById<TextView>(R.id.game_details_achievements_earned_hc_text).text = getString(R.string.diminished_percent, (achievementTotals.numEarnedHC * 200).div(achievementTotals.totalAch))
        // Points progress
        view.findViewById<CircularProgressBar>(R.id.game_details_points).apply {
            maximum = achievementTotals.totalPts.toFloat()
            progress = achievementTotals.earnedPts.toFloat()
        }
        view.findViewById<TextView>(R.id.game_details_points_text).text = achievementTotals.earnedPts.toString()
        view.findViewById<TextView>(R.id.game_details_points_total_text).text = getString(R.string.diminished_text, achievementTotals.totalPts)
        // True ratio points progress
        view.findViewById<CircularProgressBar>(R.id.game_details_ratio).apply {
            maximum = achievementTotals.totalRatio.toFloat()
            progress = achievementTotals.earnedRatio.toFloat()
        }
        view.findViewById<TextView>(R.id.game_details_ratio_text).text = achievementTotals.earnedRatio.toString()
        view.findViewById<TextView>(R.id.game_details_ratio_total_text).text = getString(R.string.diminished_text, achievementTotals.totalRatio.toString())

        view.findViewById<View>(R.id.game_details_loading_bar).visibility = View.GONE
        view.findViewById<View>(R.id.game_details_achievements_recycler_view).visibility = View.VISIBLE
    }

    companion object {
        private val TAG = Consts.BASE_TAG + AchievementSummaryFragment::class.java.simpleName
    }
}