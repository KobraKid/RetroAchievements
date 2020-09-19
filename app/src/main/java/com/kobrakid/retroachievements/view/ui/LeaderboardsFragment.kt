package com.kobrakid.retroachievements.view.ui

import android.animation.ObjectAnimator
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Leaderboard
import com.kobrakid.retroachievements.view.adapter.LeaderboardsAdapter
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class LeaderboardsFragment : Fragment() {

    private val leaderboardsAdapter by lazy { LeaderboardsAdapter(findNavController()) }
    private var consoleSpinner: Spinner? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        activity?.title = "Leaderboards"
        return inflater.inflate(R.layout.fragment_leaderboards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val leaderboardsRecycler: RecyclerView = view.findViewById(R.id.leaderboards_games)
        leaderboardsRecycler.adapter = leaderboardsAdapter
        leaderboardsRecycler.layoutManager = LinearLayoutManager(context)

        // Set up Filters
        consoleSpinner = view.findViewById(R.id.leaderboards_console_filter)
        val leaderboardsFilter = view.findViewById<EditText>(R.id.leaderboards_filter)
        consoleSpinner?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                view?.findViewById<RecyclerViewFastScroller>(R.id.leaderboard_fast_scroller)?.scrollTo(0, 0)
                leaderboardsAdapter.consoleFilter = adapterView.getItemAtPosition(pos).toString()
                leaderboardsAdapter.filter.filter(leaderboardsAdapter.buildFilter())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                view.findViewById<RecyclerViewFastScroller>(R.id.leaderboard_fast_scroller)?.scrollTo(0, 0)
                leaderboardsAdapter.consoleFilter = ""
                leaderboardsAdapter.filter.filter(leaderboardsAdapter.buildFilter())
            }
        }
        leaderboardsFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                view.findViewById<RecyclerViewFastScroller>(R.id.leaderboard_fast_scroller)?.scrollTo(0, 0)
                leaderboardsAdapter.titleFilter = charSequence.toString()
                leaderboardsAdapter.filter.filter(leaderboardsAdapter.buildFilter())
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        if (leaderboardsAdapter.itemCount == 0) {
            CoroutineScope(IO).launch {
                RetroAchievementsApi.GetLeaderboards(context, true) { parseLeaderboards(view, it) }
            }
        } else populateLeaderboardViews(view)
    }

    private suspend fun parseLeaderboards(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_LEADERBOARDS -> {
                withContext(Main) {
                    val animation = ObjectAnimator.ofInt(view.findViewById<View>(R.id.leaderboards_progress).apply { visibility = View.VISIBLE }, "secondaryProgress", 100)
                    animation.duration = 1000
                    animation.interpolator = AccelerateDecelerateInterpolator()
                    animation.start()
                }
                withContext(Default) {
                    val rows = Jsoup.parse(response.second).select("div[class=detaillist] > table > tbody > tr")
                    for ((i, row) in rows.withIndex()) {
                        // Skip the header row
                        if (i == 0) continue
                        // Helpers
                        val attr = row.select("td")[1].selectFirst("div").attr("onmouseover")
                        val start = attr.indexOf("<br>") + 5
                        val end = attr.indexOf("</div>") - 1
                        // Passed values
                        val id = row.select("td")[0].text()
                        val image = row.select("td")[1].selectFirst("img").attr("src")
                        val game = attr.substring(attr.indexOf("<b>") + 3, attr.indexOf("</b>"))
                        val console = if (start >= 0 && start < attr.length && end > start) attr.substring(start, end) else ""
                        val title = row.select("td")[3].text()
                        val description = row.select("td")[4].text()
                        val type = row.select("td")[5].text()
                        val numResults = row.select("td")[6].text()
                        postNewLeaderboard(i - 1, Leaderboard(id, image, game, console, title, description, type, numResults), view, i * 100 / rows.size)
                    }
                    withContext(Main) { populateLeaderboardViews(view) }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private suspend fun postNewLeaderboard(index: Int, leaderboard: Leaderboard, view: View, progress: Int) {
        withContext(Main) {
            leaderboardsAdapter.addLeaderboard(index, leaderboard)
            view.findViewById<ProgressBar>(R.id.leaderboards_progress).progress = progress
        }
    }

    private fun populateLeaderboardViews(view: View) {
        view.findViewById<View>(R.id.leaderboards_progress).visibility = View.GONE
        view.findViewById<View>(R.id.leaderboard_populating_fade).visibility = View.GONE
        view.findViewById<RecyclerViewFastScroller>(R.id.leaderboard_fast_scroller).isFastScrollEnabled = true
        context?.let { consoleSpinner?.adapter = ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, leaderboardsAdapter.getUniqueConsoles()) }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + LeaderboardsFragment::class.java.simpleName
    }
}