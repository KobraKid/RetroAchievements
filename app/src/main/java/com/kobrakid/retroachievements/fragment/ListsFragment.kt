package com.kobrakid.retroachievements.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.AppExecutors.Companion.instance
import com.kobrakid.retroachievements.MainActivity
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RAAPICallback
import com.kobrakid.retroachievements.RAAPIConnectionDeprecated
import com.kobrakid.retroachievements.adapter.ConsoleAdapter
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import org.json.JSONArray
import org.json.JSONException

class ListsFragment : Fragment(), RAAPICallback {

    // TODO: Find a more Kotlin-oriented approach to this field
    @JvmField
    var isShowingGames = false
    private var apiConnectionDeprecated: RAAPIConnectionDeprecated? = null
    private var isActive = false
    private var hideEmptyConsoles = false
    private var hideEmptyGames = false
    private var consoleListRecyclerView: RecyclerView? = null
    private var consoleListLayoutManager = LinearLayoutManager(context)
    private val consoleAdapter = ConsoleAdapter(this)
    // TODO: Remove the need to pass context around. What do I REALLY need from context? Pass that instead
    private val gameSummaryAdapter: GameSummaryAdapter by lazy { GameSummaryAdapter(context!!) }
    private var consoleName = ""
    private var scrollPosition = 0
    private var p = Point()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_lists, container, false)
        if (savedInstanceState == null) {
            activity?.title = "Consoles"
            apiConnectionDeprecated = (activity as MainActivity).apiConnectionDeprecated
            hideEmptyConsoles = activity?.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)?.getBoolean(getString(R.string.empty_console_hide_setting), false)!!
            hideEmptyGames = activity?.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)?.getBoolean(getString(R.string.empty_game_hide_setting), false)!!
        }

        // Initialize views
        consoleListRecyclerView = view.findViewById(R.id.list_console)
        consoleListRecyclerView?.adapter = consoleAdapter
        consoleListRecyclerView?.layoutManager = consoleListLayoutManager
        val gameListRecyclerView: RecyclerView = view.findViewById(R.id.list_games)
        gameListRecyclerView.adapter = gameSummaryAdapter
        gameListRecyclerView.layoutManager = LinearLayoutManager(context)
        val gamesFilter = view.findViewById<EditText>(R.id.list_games_filter)
        gamesFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                gameSummaryAdapter.filter.filter(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        activity?.windowManager?.defaultDisplay?.getSize(p)
        if (savedInstanceState == null) {
            if (hideEmptyConsoles) {
                view.findViewById<View>(R.id.list_hiding_fade).visibility = View.VISIBLE
                view.findViewById<View>(R.id.list_hiding_progress).visibility = View.VISIBLE
            }
            apiConnectionDeprecated?.GetConsoleIDs(this)
        } else {
            if (isShowingGames) {
                populateGamesView(view, false)
            } else {
                populateConsolesView(view)
            }
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
        if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_CONSOLE_IDS) {
            consoleAdapter.clear()
            try {
                val reader = JSONArray(response)
                // Loop once to add all consoles to view
                for (i in 0 until reader.length()) {
                    // Get console information
                    val console = reader.getJSONObject(i)
                    consoleAdapter.addConsole(console.getString("ID"), console.getString("Name"))
                }
                // Loop twice if we wish to hide empty consoles
                if (hideEmptyConsoles) {
                    for (i in 0 until reader.length()) {
                        // Get console information
                        val console = reader.getJSONObject(i)
                        val id = console.getString("ID")
                        val name = console.getString("Name")
                        val max = reader.length() - 1
                        val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
                        instance?.diskIO()?.execute {

                            // Get current console
                            val current = db?.consoleDao()?.getConsoleWithID(id.toInt())
                            // If it exists and has 0 games
                            if (current?.isNotEmpty() == true && current[0]?.gameCount == 0) {
                                instance!!.mainThread().execute { consoleAdapter.removeConsole(name) }
                            }
                            if (i == max) {
                                instance!!.mainThread().execute {
                                    if (view != null) {
                                        populateConsolesView(view!!)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Couldn't parse console IDs", e)
            }
        } else if (responseCode == RAAPIConnectionDeprecated.RESPONSE_GET_GAME_LIST) {
            try {
                val reader = JSONArray(response)
                if (reader.length() > 0) {
                    view?.findViewById<View>(R.id.list_no_games)?.visibility = View.GONE
                    for (i in 0 until reader.length()) {
                        val game = reader.getJSONObject(i)
                        gameSummaryAdapter.addGame(
                                game.getString("ID"),
                                game.getString("ImageIcon"),
                                game.getString("Title")
                        )
                    }
                    gameSummaryAdapter.updateGameSummaries(0, 0)
                }
                if (view != null)
                    populateGamesView(view!!, true)
            } catch (e: JSONException) {
                Log.e(TAG, "Couldn't parse game list", e)
            }
        }
    }

    private fun populateConsolesView(view: View) {
        view.findViewById<View>(R.id.list_hiding_fade).visibility = View.GONE
        view.findViewById<View>(R.id.list_hiding_progress).visibility = View.GONE
    }

    private fun populateGamesView(view: View, animate: Boolean) {
        activity?.title = consoleName
        (activity as MainActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        // Show Game List RecyclerView
        if (animate) {
            view.findViewById<View>(R.id.list_games_fast_scroller)
                    .animate()
                    .setDuration(375)
                    .translationX(0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            super.onAnimationStart(animation)
                            view.findViewById<View>(R.id.list_games_fast_scroller).visibility = View.VISIBLE
                        }
                    })
            view.findViewById<View>(R.id.list_games_filter)
                    .animate()
                    .setDuration(375)
                    .translationX(0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            super.onAnimationStart(animation)
                            view.findViewById<View>(R.id.list_games_filter).visibility = View.VISIBLE
                        }
                    })
        } else {
            consoleListRecyclerView
                    ?.animate()
                    ?.setDuration(0)
                    ?.translationX(-p.x.toFloat())
                    ?.setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            super.onAnimationStart(animation)
                            consoleListRecyclerView?.visibility = View.GONE
                        }
                    })
            view.findViewById<View>(R.id.list_games_fast_scroller).visibility = View.VISIBLE
            view.findViewById<View>(R.id.list_games_filter).visibility = View.VISIBLE
        }
        if (gameSummaryAdapter.numGames == 0) view.findViewById<View>(R.id.list_no_games).visibility = View.VISIBLE
    }

    fun onBackPressed(view: View) {
        isShowingGames = false
        activity?.title = "Consoles"
        (activity as MainActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        view.findViewById<View>(R.id.list_games_fast_scroller)
                .animate()
                .setDuration(375)
                .translationX(p.x.toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        view.findViewById<View>(R.id.list_games_fast_scroller).visibility = View.GONE
                    }
                })
        view.findViewById<View>(R.id.list_games_filter)
                .animate()
                .setDuration(375)
                .translationX(p.x.toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        view.findViewById<View>(R.id.list_games_filter).visibility = View.GONE
                        (view.findViewById<View>(R.id.list_games_filter) as EditText).setText("")
                    }
                })
        consoleListRecyclerView
                ?.animate()
                ?.setDuration(375)
                ?.translationX(0f)
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        consoleListRecyclerView?.visibility = View.VISIBLE
                        consoleListLayoutManager.scrollToPositionWithOffset(scrollPosition, 0)
                    }
                })
        view.findViewById<View>(R.id.list_no_games).visibility = View.GONE
    }

    fun onConsoleSelected(position: Int, console: String?, consoleName: String) {
        scrollPosition = position
        this.consoleName = consoleName

        // Hide Console List RecyclerView
        consoleListRecyclerView
                ?.animate()
                ?.setDuration(375)
                ?.translationX(-p.x.toFloat())
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        consoleListRecyclerView?.visibility = View.GONE
                    }
                })

        // Set up Game List RecyclerView
        isShowingGames = true
        gameSummaryAdapter.clear()
        gameSummaryAdapter.updateGameSummaries(0, 0)
        apiConnectionDeprecated?.GetGameList(console, this)
    }

    companion object {
        private val TAG = ListsFragment::class.java.simpleName
    }
}