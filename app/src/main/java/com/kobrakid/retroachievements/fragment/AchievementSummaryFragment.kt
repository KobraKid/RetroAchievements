package com.kobrakid.retroachievements.fragment

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.activity.MainActivity
import com.kobrakid.retroachievements.adapter.AchievementAdapter
import com.kobrakid.retroachievements.ra.Achievement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat

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
    ) {
        fun print(resources: Resources): Spanned {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return Html.fromHtml(resources.getString(
                        R.string.user_summary,
                        numEarned,
                        totalAch,
                        numEarnedHC,
                        earnedPts,
                        earnedRatio,
                        totalPts * 2,  // Account for hardcore achievements worth double
                        totalRatio), Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)
            else
                return HtmlCompat.fromHtml(resources.getString(
                        R.string.user_summary,
                        numEarned,
                        totalAch,
                        numEarnedHC,
                        earnedPts,
                        earnedRatio,
                        totalPts * 2,
                        totalRatio), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

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
                RetroAchievementsApi.GetGameInfoAndUserProgress(requireContext(), MainActivity.raUser, id) { parseGameInfoAndUserProgress(view, it) }
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
        val progress = (achievementTotals.numEarned + achievementTotals.numEarnedHC).toFloat() / achievementTotals.totalAch.toFloat() * 100.0
        if (progress >= 0)
            view.findViewById<TextView>(R.id.game_details_progress_text).text = getString(R.string.completion, DecimalFormat("@@@@").format(progress))
        view.findViewById<TextView>(R.id.game_details_user_summary).text = context?.resources?.let { achievementTotals.print(it) }
        view.findViewById<ProgressBar>(R.id.game_details_progress).max = achievementTotals.totalAch
        view.findViewById<ProgressBar>(R.id.game_details_progress).progress = achievementTotals.numEarned
        view.findViewById<View>(R.id.game_details_progress).visibility = View.VISIBLE
        view.findViewById<View>(R.id.game_details_loading_bar).visibility = View.GONE
        view.findViewById<View>(R.id.game_details_achievements_recycler_view).visibility = View.VISIBLE
    }

    companion object {
        private val TAG = AchievementSummaryFragment::class.java.simpleName
    }
}