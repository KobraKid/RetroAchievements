package com.kobrakid.retroachievements.view.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentConsoleListBinding
import com.kobrakid.retroachievements.model.Console
import com.kobrakid.retroachievements.view.adapter.ConsoleAdapter
import com.kobrakid.retroachievements.viewmodel.ConsoleListViewModel
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ConsoleListFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private val viewModel: ConsoleListViewModel by viewModels()
    private var _binding: FragmentConsoleListBinding? = null
    private val binding get() = _binding!!
    private var consoleAdapter = ConsoleAdapter(this)
    private var gameList = Collections.synchronizedList(ArrayList<GameSuggestion>())
//    private val gameListFileExists: Boolean by lazy {
//        File(context?.filesDir, "RALIST").exists()
//    }
//    private val gameListFileIsPopulated: Boolean by lazy {
//        if (gameListFileExists) {
//            // TODO: check timestamp of file
//            (context?.openFileInput("RALIST")?.bufferedReader()?.readLine() ?: "").isNotEmpty()
//        } else {
//            false
//        }
//    }

    @Suppress("unused")
    private fun clearGameListFile() {
        if (File(context?.filesDir, "RALIST").exists()) File(context?.filesDir, "RALIST").delete()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentConsoleListBinding.inflate(inflater, container, false)
        activity?.title = "Consoles"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        viewModel.loading.observe(viewLifecycleOwner, {
            if (it) {
                binding.listHidingFade.visibility = View.VISIBLE
                binding.listHidingProgress.visibility = View.VISIBLE
            } else {
                binding.listHidingFade.visibility = View.GONE
                binding.listHidingProgress.visibility = View.GONE
            }
        })
        viewModel.consoleList.observe(viewLifecycleOwner, {
            consoleAdapter.setData(it)
        })
        viewModel.gameList.observe(viewLifecycleOwner, {
            gameList.clear()
            gameList.addAll(it)
        })
        viewModel.init(context)
        // Initialize views
        binding.listConsole.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = consoleAdapter
        }
        try {
            val gameSuggestionAdapter = object : ArrayAdapter<GameSuggestion>(requireContext(), android.R.layout.simple_list_item_1, gameList) {
                override fun getItemId(position: Int): Long {
                    return getItem(position)?.id?.toLong() ?: 0L
                }
            }
            binding.gameSearch.apply {
                setAdapter(gameSuggestionAdapter)
                // Open a game details page when an item is tapped
                setOnItemClickListener { _, _, _, id ->
                    (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
                    navController.navigate(ConsoleListFragmentDirections.actionConsoleListFragmentToGameDetailsFragment(id.toString()))
                }
                // Open the first search result when "Go" ime button tapped
                setOnEditorActionListener { view, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_GO && !gameSuggestionAdapter.isEmpty) {
                        (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
                        navController.navigate(ConsoleListFragmentDirections.actionConsoleListFragmentToGameDetailsFragment(
                                gameSuggestionAdapter.getItem(0)?.id.toString()))
                        true
                    } else false
                }
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Context was null", e)
            return // no need to continue if this fragment is not attached to a context
        }
//        if (consoleAdapter.itemCount == 0) {
//            if (hideEmptyConsoles) {
//                binding.listHidingFade.visibility = View.VISIBLE
//                binding.listHidingProgress.visibility = View.VISIBLE
//            }
//            CoroutineScope(IO).launch {
//                RetroAchievementsApi.GetConsoleIDs(context) { parseConsoles(it) }
//            }
//        }
//        if (gameList.isEmpty() && gameListFileIsPopulated) {
//            populateGameListSuggestionsFromFile()
//        }
    }

    override fun onClick(view: View) {
        navController.navigate(ConsoleListFragmentDirections.actionConsoleListFragmentToConsoleGamesFragment(
                Console(view.findViewById<TextView>(R.id.console_id).text.toString(),
                        view.findViewById<TextView>(R.id.console_name).text.toString())))
    }

//    private suspend fun parseConsoles(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
//        when (response.first) {
//            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
//            RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
//                try {
//                    val reader = JSONArray(response.second)
//
//                    // Loop once to add all consoles to view
//                    for (i in 0 until reader.length()) {
//                        // Fill game list to provide suggestions to the user,
//                        // only if the list doesn't exist or was outdated
//                        if (!gameListFileIsPopulated) {
//                            populateGameListWithGamesFrom(reader.getJSONObject(i).getString("ID"))
//                        }
//                        consoleAdapter.addConsole(reader.getJSONObject(i).getString("ID"), reader.getJSONObject(i).getString("Name"))
//                    }
//                    // Loop twice if we wish to hide empty consoles
//                    if (hideEmptyConsoles) {
//                        val db = context?.let { RetroAchievementsDatabase.getInstance(it) }
//                        db?.let {
//                            for (i in 0 until reader.length()) {
//                                val id = reader.getJSONObject(i).getString("ID")
//                                val name = reader.getJSONObject(i).getString("Name")
//                                parseConsoleHelper(it, id.toInt(), name)
//                            }
//                        }
//                    }
//                } catch (e: JSONException) {
//                    Log.e(TAG, "Couldn't parse console IDs", e)
//                }
//            }
//            else -> Log.v(TAG, "${response.first}: ${response.second}")
//        }
//    }

    /**
     * Only needed because problems arise when using withContext blocks in try-catch blocks.
     */
//    private suspend fun parseConsoleHelper(db: RetroAchievementsDatabase, id: Int, name: String) {
//        withContext(IO) {
//            val current = db.consoleDao()?.getConsoleWithID(id)
//            if (current?.isNotEmpty() == true && current[0]?.gameCount == 0) {
//                consoleAdapter.removeConsole(name)
//            }
//            withContext(Main) {
//                populateConsolesView()
//            }
//        }
//    }
//
//    private fun populateConsolesView() {
//        binding.listConsole.scrollToPosition(0)
//        binding.listHidingFade.visibility = View.GONE
//        binding.listHidingProgress.visibility = View.GONE
//    }
//
//    /**
//     * Fire off an API call to get the list of games for this console
//     *
//     * @param console The console to count
//     */
//    private suspend fun populateGameListWithGamesFrom(console: String) {
//        withContext(IO) {
//            RetroAchievementsApi.GetGameList(context, console) { populateGameListFile(it) }
//        }
//    }
//
//    /**
//     * Populate the game list file with games from each console
//     *
//     * @param response
//     */
//    private fun populateGameListFile(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
//        when (response.first) {
//            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
//            RetroAchievementsApi.RESPONSE.GET_GAME_LIST -> {
//                try {
//                    val reader = JSONArray(response.second)
//                    if (reader.length() == 0) return
//                    context?.openFileOutput("RALIST", Context.MODE_APPEND).use {
//                        val console = reader.getJSONObject(0).getString("ConsoleName")
//                        it?.write(("\u0001$console\n").toByteArray())
//                        for (i in 0 until reader.length()) {
//                            val title = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                                Html.fromHtml(reader.getJSONObject(i).getString("Title"), Html.FROM_HTML_MODE_COMPACT).toString()
//                            } else {
//                                reader.getJSONObject(i).getString("Title")
//                            }
//                            val id = reader.getJSONObject(i).getString("ID")
//                            it?.write(("$id\u0002$title\n").toByteArray())
//                            gameList.add(GameSuggestion(id, title, console))
//                        }
//                    }
//                } catch (e: JSONException) {
//                    Log.e(TAG, "Couldn't parse game list", e)
//                }
//            }
//            else -> Log.v(TAG, "${response.first}: ${response.second}")
//        }
//    }
//
//    private fun populateGameListSuggestionsFromFile() {
//        var console = ""
//        context?.openFileInput("RALIST")?.bufferedReader()?.forEachLine { line ->
//            if (line.first() == '\u0001')
//                console = line.substring(1)
//            else
//                gameList.add(GameSuggestion(
//                        line.substringBefore('\u0002'),
//                        line.substringAfter('\u0002'),
//                        console))
//        }
//    }

    public data class GameSuggestion(val id: String, val title: String, val console: String?) {
        override fun toString(): String {
            return title + (if (console.isNullOrEmpty()) "" else " | $console")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + ConsoleListFragment::class.java.simpleName
    }

}