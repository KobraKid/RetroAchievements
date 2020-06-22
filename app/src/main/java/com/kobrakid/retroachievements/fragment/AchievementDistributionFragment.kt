package com.kobrakid.retroachievements.fragment

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.SparseIntArray
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
import com.kobrakid.retroachievements.RAAPICallback
import com.kobrakid.retroachievements.RAAPIConnection
import org.jsoup.Jsoup
import java.lang.ref.WeakReference
import java.util.*
import java.util.regex.Pattern

class AchievementDistributionFragment : Fragment(), RAAPICallback {
    private var achievementDistro: LineChart? = null
    private var data: SortedMap<Int, Int>? = null
    private var isActive = false
    private var isAPIActive = false

    @SuppressLint("UseSparseArrays")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true
        val view = inflater.inflate(R.layout.view_pager_achievement_distribution, container, false)
        achievementDistro = view.findViewById(R.id.game_details_achievement_distribution)
        achievementDistro?.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                if (isActive) {
                    (view.findViewById<View>(R.id.game_details_chart_hints) as TextView).text = resources.getQuantityString(
                            R.plurals.achievement_chart_hints,
                            e.y.toInt(),
                            e.y.toInt(),
                            e.x.toInt())
                }
            }

            override fun onNothingSelected() {
                if (isActive) {
                    (view.findViewById<View>(R.id.game_details_chart_hints) as TextView).text = ""
                }
            }
        })
        if (savedInstanceState == null && arguments != null) {
            data = TreeMap()
            isAPIActive = true
            RAAPIConnection(Objects.requireNonNull(context)).GetAchievementDistribution(arguments!!.getString("GameID"), this)
        } else if (!isAPIActive) {
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

    override fun callback(responseCode: Int, response: String) {
        if (!isActive) return
        if (responseCode == RAAPIConnection.RESPONSE_GET_ACHIEVEMENT_DISTRIBUTION) {
            AchievementDistributionChartAsyncTask(this, data).execute(response)
        }
        isAPIActive = false
    }

    private fun populateChartData(view: View?) {
        val context = context
        if (context != null) {
            if (data!!.size > 0) {
                // Set chart data
                val entries: MutableList<Entry> = ArrayList()
                for (key in data!!.keys) {
                    data!![key]?.let { entries.add(Entry(key.toFloat(), it.toFloat())) }
                }
                val dataSet = LineDataSet(entries, "")
                dataSet.setDrawFilled(true)
                val lineData = LineData(dataSet)
                lineData.setDrawValues(false)

                // Set chart colors
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val accentColor = TypedValue()
                    val primaryColor = TypedValue()
                    context.theme.resolveAttribute(R.attr.colorAccent, accentColor, true)
                    context.theme.resolveAttribute(R.attr.colorPrimary, primaryColor, true)
                    achievementDistro!!.axisLeft.textColor = primaryColor.data
                    achievementDistro!!.xAxis.textColor = primaryColor.data
                    dataSet.setCircleColor(accentColor.data)
                    dataSet.color = accentColor.data
                    dataSet.circleHoleColor = accentColor.data
                    dataSet.fillColor = accentColor.data
                }

                // Set chart axes
                achievementDistro!!.axisRight.isEnabled = false
                achievementDistro!!.legend.isEnabled = false
                achievementDistro!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
                achievementDistro!!.axisLeft.axisMinimum = 0f

                // Set chart description
                val description = Description()
                description.text = ""
                achievementDistro!!.description = description

                // Set chart finalized data
                achievementDistro!!.data = lineData

                // Redraw chart
                achievementDistro!!.invalidate()
            }
            view!!.findViewById<View>(R.id.game_details_achievement_distro_loading).visibility = View.GONE
            achievementDistro!!.visibility = View.VISIBLE
        }
    }

    private class AchievementDistributionChartAsyncTask internal constructor(fragment: AchievementDistributionFragment, data: SortedMap<Int, Int>?) : AsyncTask<String?, Int?, SortedMap<Int, Int>>() {
        private val fragmentReference: WeakReference<AchievementDistributionFragment> = WeakReference(fragment)
        private val dataReference: WeakReference<SortedMap<Int, Int>?> = WeakReference(data)
        override fun doInBackground(vararg strings: String?): SortedMap<Int, Int> {
            val response = strings[0]
            val document = Jsoup.parse(response)
            val scripts = document.getElementsByTag("script")
            var scriptIndex = -1
            for (i in scripts.indices) {
                if (scripts[i].html().startsWith("google.load('visualization'")) {
                    scriptIndex = i
                }
            }
            if (scriptIndex == -1) {
                return TreeMap()
            }
            var rows = scripts[scriptIndex].dataNodes()[0].wholeData
            rows = rows.substring(rows.indexOf("dataTotalScore.addRows("))
            rows = rows.substring(0, rows.indexOf(");"))
            val p1 = Pattern.compile("v:(\\d+),")
            val m1 = p1.matcher(rows)
            val p2 = Pattern.compile(",\\s(\\d+)\\s]")
            val m2 = p2.matcher(rows)
            val achievementTotals = SparseIntArray()
            while (m1.find() && m2.find()) {
                achievementTotals.put(m1.group(1).toInt(), m2.group(1).toInt())
            }
            @SuppressLint("UseSparseArrays") val chartData: SortedMap<Int, Int> = TreeMap()
            for (i in 0 until achievementTotals.size()) {
                chartData[i + 1] = achievementTotals[i + 1]
            }
            return chartData
        }

        override fun onPostExecute(chartData: SortedMap<Int, Int>) {
            super.onPostExecute(chartData)
            val fragment = fragmentReference.get()
            val data = dataReference.get()
            if (fragment != null && data != null) {
                data.putAll(chartData)
                fragment.populateChartData(fragment.view)
            }
        }

    }
}