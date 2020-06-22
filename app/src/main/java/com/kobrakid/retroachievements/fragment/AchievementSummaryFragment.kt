package com.kobrakid.retroachievements.fragment

import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.MainActivity
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RAAPICallback
import com.kobrakid.retroachievements.RAAPIConnection
import com.kobrakid.retroachievements.adapter.AchievementAdapter
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*

/**
 * This class is responsible for displaying summary information on all the achievements for a
 * particular game.
 */
class AchievementSummaryFragment : Fragment(), RAAPICallback {
    private var adapter: AchievementAdapter? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    private var numEarned = 0
    private var numEarnedHC = 0
    private var totalAch = 0
    private var earnedPts = 0
    private var totalPts = 0
    private var earnedRatio = 0
    private var totalRatio = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        val view = inflater.inflate(R.layout.view_pager_achievements_summary, container, false)
        if (savedInstanceState == null) {
            adapter = AchievementAdapter(this)
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.game_details_achievements_recycler_view)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        if (savedInstanceState == null && arguments != null) {
            RAAPIConnection(Objects.requireNonNull(context)).GetGameInfoAndUserProgress(MainActivity.ra_user, arguments!!.getString("GameID"), this)
        } else {
            populateViews(view)
        }
        return view
    }

    override fun callback(responseCode: Int, response: String) {
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS) {
            try {
                val reader = JSONObject(response)
                adapter!!.setNumDistinctCasual(reader.getString("NumDistinctPlayersCasual").toDouble())
                if (reader.getString("NumAchievements") == "0") {
                    if (view != null) {
                        view!!.findViewById<View>(R.id.game_details_loading_bar).visibility = View.GONE
                        view!!.findViewById<View>(R.id.game_details_no_achievements).visibility = View.VISIBLE
                    }
                } else {
                    val achievements = reader.getJSONObject("Achievements")
                    var achievement: JSONObject
                    numEarned = 0
                    numEarnedHC = 0
                    totalAch = 0
                    earnedPts = 0
                    totalPts = 0
                    earnedRatio = 0
                    totalRatio = 0
                    val keys = achievements.keys()
                    while (keys.hasNext()) {
                        val achievementID = keys.next()
                        achievement = achievements.getJSONObject(achievementID)
                        if (achievement.has("DateEarnedHardcore")) {
                            numEarned++
                            numEarnedHC++
                            earnedPts += 2 * achievement.getString("Points").toInt()
                            earnedRatio += achievement.getString("TrueRatio").toInt()
                        } else if (achievement.has("DateEarned")) {
                            numEarned++
                            earnedPts += achievement.getString("Points").toInt()
                            earnedRatio += achievement.getString("TrueRatio").toInt()
                        }
                        totalAch++
                        totalPts += achievement.getString("Points").toInt()
                        totalRatio += achievement.getString("TrueRatio").toInt()
                    }
                    adapter!!.clear()
                    AchievementDetailsAsyncTask(this).execute(response)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun populateViews(view: View?) {
        (view!!.findViewById<View>(R.id.game_details_progress_text) as TextView).text = getString(
                R.string.completion,
                DecimalFormat("@@@@")
                        .format((numEarned + numEarnedHC).toFloat() / totalAch.toFloat() * 100.0))
        (view.findViewById<View>(R.id.game_details_user_summary) as TextView).text = Html.fromHtml(getString(
                R.string.user_summary,
                numEarned,
                totalAch,
                numEarnedHC,
                earnedPts,
                earnedRatio,
                totalPts * 2,  // Account for hardcore achievements worth double
                totalRatio))
        (view.findViewById<View>(R.id.game_details_progress) as ProgressBar).progress = (numEarned.toFloat() / totalAch.toFloat() * 10000.0).toInt()
        view.findViewById<View>(R.id.game_details_progress).visibility = View.VISIBLE
        view.findViewById<View>(R.id.game_details_loading_bar).visibility = View.GONE
        view.findViewById<View>(R.id.game_details_achievements_recycler_view).visibility = View.VISIBLE
    }

    private class AchievementDetailsAsyncTask internal constructor(fragment: Fragment) : AsyncTask<String?, Any?, Array<String>?>() {
        val fragmentReference: WeakReference<Fragment> = WeakReference(fragment)
        override fun doInBackground(vararg strings: String?): Array<String>? {
            try {
                val reader = JSONObject(strings[0])
                val achievements = reader.getJSONObject("Achievements")
                var achievement: JSONObject
                var count: Int
                val displayOrder: MutableList<Int> = ArrayList()
                val displayOrderEarned: MutableList<Int> = ArrayList()
                var totalAch = 0
                val keys = achievements.keys()
                while (keys.hasNext()) {
                    val achievementID = keys.next()
                    achievement = achievements.getJSONObject(achievementID)

                    // Set up ordering of achievements
                    var dateEarned = ""
                    var earnedHC = false
                    when {
                        achievement.has("DateEarnedHardcore") -> {
                            dateEarned = achievement.getString("DateEarnedHardcore")
                            displayOrderEarned.add(achievement.getString("DisplayOrder").toInt())
                            displayOrderEarned.sort()
                            count = displayOrderEarned.indexOf(achievement.getString("DisplayOrder").toInt())
                            earnedHC = true
                        }
                        achievement.has("DateEarned") -> {
                            dateEarned = achievement.getString("DateEarned")
                            displayOrderEarned.add(achievement.getString("DisplayOrder").toInt())
                            displayOrderEarned.sort()
                            count = displayOrderEarned.indexOf(achievement.getString("DisplayOrder").toInt())
                        }
                        else -> {
                            displayOrder.add(achievement.getString("DisplayOrder").toInt())
                            displayOrder.sort()
                            count = displayOrder.indexOf(achievement.getString("DisplayOrder").toInt()) + displayOrderEarned.size
                        }
                    }
                    if (dateEarned == "") {
                        dateEarned = "NoDate:$count"
                        earnedHC = false
                    }
                    if (count == 0) count = totalAch
                    publishProgress(
                            count,
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
                    totalAch++
                }
            } catch (e: JSONException) {
                if (e.toString().contains("Value null at Achievements of type org.json.JSONObject$1 cannot be converted to JSONObject")) Log.d(TAG, "This game has no achievements") else e.printStackTrace()
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Any?) {
            val fragment = fragmentReference.get() as AchievementSummaryFragment?
            if (fragment != null) {
                fragment.adapter!!.addAchievement(
                        values[0] as Int,
                        (values[1] as String),
                        (values[2] as String),
                        (values[3] as String),
                        (values[4] as String),
                        (values[5] as String),
                        (values[6] as String),
                        (values[7] as String),
                        values[8] as Boolean,
                        (values[9] as String),
                        (values[10] as String),
                        (values[11] as String),
                        (values[12] as String),
                        (values[13] as String))
            }
        }

        override fun onPostExecute(strings: Array<String>?) {
            val fragment = fragmentReference.get() as AchievementSummaryFragment?
            if (fragment != null && fragment.view != null) fragment.populateViews(fragment.view)
        }

    }

    companion object {
        private val TAG = AchievementSummaryFragment::class.java.simpleName
    }
}