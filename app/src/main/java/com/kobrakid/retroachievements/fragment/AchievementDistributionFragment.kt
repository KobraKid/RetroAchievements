package com.kobrakid.retroachievements.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.regex.Pattern

class AchievementDistributionFragment : Fragment() {

    private var achievementDistributionChart: LineChart? = null
    private var chartData = mutableListOf<Entry>()
    private var isActive = false

    @SuppressLint("UseSparseArrays")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true
        val view = inflater.inflate(R.layout.view_pager_achievement_distribution, container, false)
        achievementDistributionChart = view.findViewById(R.id.game_details_achievement_distribution)
        achievementDistributionChart?.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                if (isActive) {
                    view.findViewById<TextView>(R.id.game_details_chart_hints).text = resources.getQuantityString(
                            R.plurals.achievement_chart_hints,
                            e.y.toInt(),
                            e.y.toInt(),
                            e.x.toInt())
                }
            }

            override fun onNothingSelected() {
                if (isActive) {
                    view.findViewById<TextView>(R.id.game_details_chart_hints).text = ""
                }
            }
        })
        if (savedInstanceState == null && arguments != null) {
            val ctx = context?.applicationContext
            val id = arguments?.getString("GameID", "0")
            CoroutineScope(IO).launch {
                if (ctx != null && id != null)
                    RetroAchievementsApi.ScrapeGameInfoFromWeb(ctx, id) { parseAchievementDistribution(view, it) }
            }
        } else {
            populateChartData(view)
        }
        return view
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onStart() {
        super.onStart()
        isActive = true
    }

    private suspend fun parseAchievementDistribution(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.SCRAPE_GAME_PAGE -> {
                withContext(Default) {
                    Jsoup.parse(response.second)
                            .getElementsByTag("script")
                            .filter { it.html().startsWith("google.load('visualization'") }
                            .map {
                                val rows = it.dataNodes()[0].wholeData
                                val p1 = Pattern.compile("v:(\\d+),")
                                val m1 = p1.matcher(rows)
                                val p2 = Pattern.compile(",\\s(\\d+)\\s]")
                                val m2 = p2.matcher(rows)
                                while (m1.find() && m2.find()) {
                                    chartData.add(
                                            Entry(
                                                    m1.group(1)?.toFloat() ?: 0f,
                                                    m2.group(1)?.toFloat() ?: 0f
                                            )
                                    )
                                }
                            }
                }
                withContext(Main) {
                    populateChartData(view)
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    private fun populateChartData(view: View) {
        val dataSet = LineDataSet(chartData, "")
        dataSet.setDrawFilled(true)
        val lineData = LineData(dataSet)
        lineData.setDrawValues(false)

        // Set chart colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val accentColor = TypedValue()
            val primaryColor = TypedValue()
            context?.theme?.resolveAttribute(R.attr.colorAccent, accentColor, true)
            context?.theme?.resolveAttribute(R.attr.colorPrimary, primaryColor, true)
            achievementDistributionChart?.axisLeft?.textColor = primaryColor.data
            achievementDistributionChart?.xAxis?.textColor = primaryColor.data
            dataSet.setCircleColor(accentColor.data)
            dataSet.color = accentColor.data
            dataSet.circleHoleColor = accentColor.data
            dataSet.fillColor = accentColor.data
        }

        // Set chart axes
        achievementDistributionChart?.axisRight?.isEnabled = false
        achievementDistributionChart?.legend?.isEnabled = false
        achievementDistributionChart?.xAxis?.position = XAxis.XAxisPosition.BOTTOM
        achievementDistributionChart?.axisLeft?.axisMinimum = 0f

        // Set chart description
        val description = Description()
        description.text = ""
        achievementDistributionChart?.description = description

        // Set chart finalized data
        achievementDistributionChart?.data = lineData

        // Redraw chart
        achievementDistributionChart?.invalidate()

        view.findViewById<View>(R.id.game_details_achievement_distro_loading).visibility = View.GONE
        achievementDistributionChart?.visibility = View.VISIBLE
    }

    companion object {
        private val TAG = AchievementDistributionFragment::class.java.simpleName
    }
}