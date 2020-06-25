package com.kobrakid.retroachievements.fragment

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.RowSortedTable
import com.google.common.collect.TreeBasedTable
import com.kobrakid.retroachievements.*
import com.kobrakid.retroachievements.adapter.LeaderboardsAdapter
import com.kobrakid.retroachievements.adapter.UserRankingAdapter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.lang.ref.WeakReference
import java.util.*

class LeaderboardsFragment : Fragment(), RAAPICallback {

    private var apiConnectionDeprecated: RAAPIConnectionDeprecated? = null
    private val userRankings = mutableListOf<String>()
    private val userNames = mutableListOf<String>()
    private val userScores = mutableListOf<String>()
    private val userRatios = mutableListOf<String>()
    private val userRankingAdapter = UserRankingAdapter(userRankings, userNames, userScores, userRatios)
    private var table: RowSortedTable<Int, String, String>? = null
    private var tableFiltered: RowSortedTable<Int, String, String>? = null
    private var leaderboardsAdapter: LeaderboardsAdapter? = null
    private var consoleSpinner: Spinner? = null
    private var filteredConsole = ""
    private var filteredTitle = ""
    private var uniqueColumns: MutableList<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_leaderboards, container, false)
        activity?.title = "Leaderboards"
        val topUsers: RecyclerView = view.findViewById(R.id.leaderboards_users)
        val leaderboardsRecycler: RecyclerView = view.findViewById(R.id.leaderboards_games)
        if (savedInstanceState == null) {
            apiConnectionDeprecated = (activity as MainActivity).apiConnectionDeprecated
            // TODO: remove the need to create these lists here, generate them in their respective adapters
            table = TreeBasedTable.create()
            tableFiltered = TreeBasedTable.create()
            leaderboardsAdapter = LeaderboardsAdapter(this, table!!, tableFiltered!!)
            uniqueColumns = ArrayList()
        }
        topUsers.adapter = userRankingAdapter
        topUsers.layoutManager = LinearLayoutManager(context)
        leaderboardsRecycler.adapter = leaderboardsAdapter
        leaderboardsRecycler.layoutManager = LinearLayoutManager(context)

        // Set up Filters
        consoleSpinner = view.findViewById(R.id.leaderboards_console_filter)
        val leaderboardsFilter = view.findViewById<EditText>(R.id.leaderboards_filter)
        consoleSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                filteredConsole = adapterView.getItemAtPosition(pos).toString()
                leaderboardsAdapter?.filter?.filter(filteredConsole + "\t" + filteredTitle)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                filteredConsole = ""
                leaderboardsAdapter?.filter?.filter(filteredConsole + "\t" + filteredTitle)
            }
        }
        leaderboardsFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                filteredTitle = charSequence.toString()
                leaderboardsAdapter?.filter?.filter(filteredConsole + "\t" + filteredTitle)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        if (savedInstanceState == null) {
            apiConnectionDeprecated?.GetTopTenUsers(this)
            apiConnectionDeprecated?.GetLeaderboards(true, this)
        } else {
            // Will add `populateTopTenUserViews(view)` here once needed
            populateLeaderboardViews(view)
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        isActive = true
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun callback(responseCode: Int, response: String) {
        if (!isActive) return
        if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_TOP_TEN_USERS) {
            try {
                val reader = JSONArray(response)
                val count = userRankings.size
                userRankings.clear()
                userNames.clear()
                userScores.clear()
                userRatios.clear()
                userRankingAdapter.notifyItemRangeRemoved(0, count)
                for (i in 0 until reader.length()) {
                    userRankings.add((i + 1).toString())
                    userNames.add((reader[i] as JSONObject).getString("1"))
                    userScores.add((reader[i] as JSONObject).getString("2"))
                    userRatios.add((reader[i] as JSONObject).getString("3"))
                    userRankingAdapter.notifyItemInserted(i)
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Couldn't parse top ten users", e)
            }
            if (!userNames.contains(MainActivity.raUser)) apiConnectionDeprecated?.GetUserSummary(MainActivity.raUser, 0, this)
        } else if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_USER_SUMMARY) {
            try {
                val reader = JSONObject(response)
                userRankings.add(reader.getString("Rank"))
                userNames.add(MainActivity.raUser)
                userScores.add(reader.getString("TotalPoints"))
                userRatios.add(reader.getString("TotalTruePoints"))
                userRankingAdapter.notifyItemInserted(userNames.size - 1)
            } catch (e: JSONException) {
                Log.e(TAG, "Couldn't parse user summary", e)
            }
        } else if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_LEADERBOARDS) {
            val animation = ObjectAnimator.ofInt(view?.findViewById(R.id.leaderboards_progress), "secondaryProgress", 100)
            animation.duration = 1000
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.start()
            LeaderboardsAsyncTask(this, uniqueColumns, leaderboardsAdapter, table, tableFiltered).execute(response)
        }
    }

    fun onClick(leaderboard: Map<String?, String?>) {
        val intent = Intent(this.activity, LeaderboardActivity::class.java)
        val extras = Bundle()
        extras.putString("ID", leaderboard["ID"])
        extras.putString("GAME", leaderboard["GAME"])
        extras.putString("IMAGE", leaderboard["IMAGE"])
        extras.putString("CONSOLE", leaderboard["CONSOLE"])
        extras.putString("TITLE", leaderboard["TITLE"])
        extras.putString("DESCRIPTION", leaderboard["DESCRIPTION"])
        extras.putString("TYPE", leaderboard["TYPE"])
        extras.putString("NUMRESULTS", leaderboard["NUMRESULTS"])
        intent.putExtras(extras)
        Log.v(TAG, leaderboard.toString())
        startActivity(intent)
    }

    /* Inner Classes and Interfaces */
    private class LeaderboardsAsyncTask internal constructor(fragment: LeaderboardsFragment, uniqueColumns: MutableList<String>?, adapter: LeaderboardsAdapter?, table: RowSortedTable<Int, String, String>?, tableFiltered: RowSortedTable<Int, String, String>?) : AsyncTask<String?, Int?, RowSortedTable<Int, String, String>?>() {
        private val fragmentReference: WeakReference<LeaderboardsFragment> = WeakReference(fragment)
        private val uniqueColumnsReference: WeakReference<MutableList<String>?> = WeakReference(uniqueColumns)
        private val adapterReference: WeakReference<LeaderboardsAdapter?> = WeakReference(adapter)
        private val mTable: WeakReference<RowSortedTable<Int, String, String>?> = WeakReference(table)
        private val mTableFiltered: WeakReference<RowSortedTable<Int, String, String>?> = WeakReference(tableFiltered)
        override fun doInBackground(vararg strings: String?): RowSortedTable<Int, String, String>? {
            val leaderboards = mTable.get()
            val tableFiltered = mTableFiltered.get()
            if (leaderboards != null && tableFiltered != null) {
                val document = Jsoup.parse(strings[0])
                val rows = document.select("div[class=detaillist] > table > tbody > tr")
                for (i in 1 until rows.size) {
                    val row = rows[i]
                    leaderboards.put(i - 1, "ID", row.select("td")[0].text())
                    leaderboards.put(i - 1, "IMAGE", row.select("td")[1].selectFirst("img").attr("src"))
                    val attr = row.select("td")[1].selectFirst("div").attr("onmouseover")
                    leaderboards.put(i - 1, "GAME", attr.substring(attr.indexOf("<b>") + 3, attr.indexOf("</b>")))
                    val start = attr.indexOf("<br>") + 5
                    val end = attr.indexOf("</div>") - 1
                    if (start >= 0 && start < attr.length && end > start) leaderboards.put(i - 1, "CONSOLE", attr.substring(start, end)) else leaderboards.put(i - 1, "CONSOLE", "")
                    leaderboards.put(i - 1, "TITLE", row.select("td")[3].text())
                    leaderboards.put(i - 1, "DESCRIPTION", row.select("td")[4].text())
                    leaderboards.put(i - 1, "TYPE", row.select("td")[5].text())
                    leaderboards.put(i - 1, "NUMRESULTS", row.select("td")[6].text())
                    publishProgress(i * 100 / rows.size)
                }
                // FIXME Can be concurrently modified, clashing with {@Link LeaderboardsAdapter.java} line 93
                tableFiltered.putAll(leaderboards)
            }
            return leaderboards
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            val fragment: Fragment? = fragmentReference.get()
            fragment?.view?.let {
                it.findViewById<View>(R.id.leaderboards_progress).visibility = View.VISIBLE
                (it.findViewById<View>(R.id.leaderboards_progress) as ProgressBar).progress = values[0]
                        ?: 0
            }
        }

        override fun onPostExecute(result: RowSortedTable<Int, String, String>?) {
            super.onPostExecute(result)
            val adapter = adapterReference.get()
            adapter?.notifyDataSetChanged()
            val fragment = fragmentReference.get()
            if (fragment != null) {
                val uniqueCols = uniqueColumnsReference.get()
                if (uniqueCols != null) {
                    uniqueCols.clear()
                    uniqueCols.add(0, "")
                    uniqueCols.addAll(TreeSet(result!!.column("CONSOLE").values))
                    if (fragment.view != null) {
                        fragment.populateLeaderboardViews(fragment.view!!)
                    }
                }
            }
        }

    }

    private fun populateLeaderboardViews(view: View) {
        view.findViewById<View>(R.id.leaderboards_progress).visibility = View.GONE
        consoleSpinner?.adapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, uniqueColumns!!) }
    }

    companion object {
        private val TAG = LeaderboardsFragment::class.java.simpleName
        private var isActive = false
    }
}