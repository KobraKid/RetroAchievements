package com.kobrakid.retroachievements.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.activity.MainActivity
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

class RecentGamesFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var offset = 0
    private val gamesPerAPICall = 15

    // Initially ask for 15 games (prevent spamming API)
    private var gamesAskedFor = 15

    private lateinit var navController: NavController
    private val gameSummaryAdapter: GameSummaryAdapter by lazy { GameSummaryAdapter(this, getDrawable(requireContext(), R.drawable.image_view_border)) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true
        requireActivity().title = getString(R.string.recent_games_title)
        return inflater.inflate(R.layout.fragment_recent_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recent_games_recycler_view)
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gameSummaryAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // Try to catch user reaching end of list early and append past the screen.
                // If the user has already scrolled to the end, the scrolling will halt while more entries are added.
                if (layoutManager.findLastVisibleItemPosition() >= offset + gamesPerAPICall - 2 && gameSummaryAdapter.itemCount == gamesAskedFor) {
                    offset += gamesPerAPICall
                    gamesAskedFor += gamesPerAPICall
                    CoroutineScope(Dispatchers.IO).launch {
                        RetroAchievementsApi.GetUserRecentlyPlayedGames(requireContext(), MainActivity.raUser, gamesPerAPICall, offset) { parseRecentlyPlayedGames(it) }
                    }
                }

            }
        })

        // Set up refresh action
        (view as SwipeRefreshLayout).setOnRefreshListener(this)
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.GetUserRecentlyPlayedGames(requireContext(), MainActivity.raUser, gamesPerAPICall, offset) { parseRecentlyPlayedGames(it) }
        }
    }

    override fun onRefresh() {
        offset = 0
        gamesAskedFor = gamesPerAPICall
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.GetUserRecentlyPlayedGames(requireContext(), MainActivity.raUser, gamesPerAPICall, offset) { parseRecentlyPlayedGames(it) }
        }
    }

    private suspend fun parseRecentlyPlayedGames(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_USER_RECENTLY_PLAYED_GAMES -> {
                if (offset == 0) gameSummaryAdapter.clear()
                try {
                    // The user requested a refresh, so clear previously listed games
                    val reader = JSONArray(response.second)
                    for (i in 0 until reader.length()) {
                        gameSummaryAdapter.addGame(
                                i + offset,
                                reader.getJSONObject(i).getString("GameID"),
                                reader.getJSONObject(i).getString("ImageIcon"),
                                reader.getJSONObject(i).getString("Title"),
                                getString(R.string.game_stats,
                                        reader.getJSONObject(i).getString("NumAchieved"),
                                        reader.getJSONObject(i).getString("NumPossibleAchievements"),
                                        reader.getJSONObject(i).getString("ScoreAchieved"),
                                        reader.getJSONObject(i).getString("PossibleScore")),
                                reader.getJSONObject(i).getString("NumAchieved") != "0"
                                        && reader.getJSONObject(i).getString("NumAchieved") == reader.getJSONObject(i).getString("NumPossibleAchievements"))
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Failed to parse recenntly played games", e)
                } finally {
                    (view as SwipeRefreshLayout).isRefreshing = false
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    override fun onClick(view: View) {
        navController.navigate(RecentGamesFragmentDirections.actionRecentGamesFragmentToGameDetailsFragment(
                view.findViewById<TextView>(R.id.game_summary_game_id).text.toString()))
    }

    companion object {
        private val TAG = RecentGamesFragment::class.java.simpleName
    }
}