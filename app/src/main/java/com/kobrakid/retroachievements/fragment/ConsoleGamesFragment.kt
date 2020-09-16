package com.kobrakid.retroachievements.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter
import com.kobrakid.retroachievements.ra.Console
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

/**
 * This Activity lists all of the games of a certain console.
 */
class ConsoleGamesFragment : Fragment(), View.OnClickListener {

    private val args: ConsoleGamesFragmentArgs by navArgs()
    private val gameSummaryAdapter: GameSummaryAdapter by lazy {
        GameSummaryAdapter(this, requireContext().getDrawable(R.drawable.image_view_border))
    }
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true
        return inflater.inflate(R.layout.fragment_console_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val console = args.console ?: Console()
        val id = console.id
        requireActivity().title = console.name

        val gameListRecyclerView = view.findViewById<RecyclerView>(R.id.list_games)
        gameListRecyclerView.adapter = gameSummaryAdapter
        gameListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val gamesFilter = view.findViewById<EditText>(R.id.list_games_filter)
        gamesFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                gameSummaryAdapter.filter.filter(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.GetGameList(requireContext(), id) { parseGameList(it) }
        }
    }

    override fun onClick(view: View) {
        navController.navigate(ConsoleGamesFragmentDirections.actionConsoleGamesFragmentToGameDetailsFragment(
                view.findViewById<TextView>(R.id.game_summary_game_id).text.toString()))
    }

    private suspend fun parseGameList(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
                try {
                    val reader = JSONArray(response.second)
                    if (reader.length() > 0) {
                        view?.findViewById<View>(R.id.list_no_games)?.visibility = View.GONE
                        for (i in 0 until reader.length()) {
                            gameSummaryAdapter.addGame(
                                    reader.getJSONObject(i).getString("ID"),
                                    reader.getJSONObject(i).getString("ImageIcon"),
                                    reader.getJSONObject(i).getString("Title")
                            )
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse game list", e)
                }
                withContext(Dispatchers.Main) {
                    populateGamesView()
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private fun populateGamesView() {
        if (gameSummaryAdapter.numGames == 0) view?.findViewById<View>(R.id.list_no_games)?.visibility = View.VISIBLE
    }

    companion object {
        private val TAG = Consts.BASE_TAG + ConsoleGamesFragment::class.java.simpleName
    }
}