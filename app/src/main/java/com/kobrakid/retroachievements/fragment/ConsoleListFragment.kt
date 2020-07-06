package com.kobrakid.retroachievements.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.adapter.ConsoleAdapter
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import com.kobrakid.retroachievements.ra.Console
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class ConsoleListFragment : Fragment(), View.OnClickListener {

    private val hideEmptyConsoles by lazy {
        requireContext().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getBoolean(getString(R.string.empty_console_hide_setting), false)
    }
    private var consoleAdapter = ConsoleAdapter(this)
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        requireActivity().title = "Consoles"
        return inflater.inflate(R.layout.fragment_console_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        // Initialize views
        val consoleListRecyclerView = view.findViewById<RecyclerView>(R.id.list_console)
        consoleListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        consoleListRecyclerView.adapter = consoleAdapter
        if (consoleAdapter.itemCount == 0) {
            if (hideEmptyConsoles) {
                view.findViewById<View>(R.id.list_hiding_fade).visibility = View.VISIBLE
                view.findViewById<View>(R.id.list_hiding_progress).visibility = View.VISIBLE
            }
            CoroutineScope(IO).launch {
                RetroAchievementsApi.GetConsoleIDs(requireContext()) { parseConsoles(view, it) }
            }
        }
    }

    override fun onClick(view: View) {
        navController.navigate(ConsoleListFragmentDirections.actionConsoleListFragmentToConsoleGamesFragment(
                Console(view.findViewById<TextView>(R.id.console_id).text.toString(),
                        view.findViewById<TextView>(R.id.console_name).text.toString())))
    }

    private suspend fun parseConsoles(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_CONSOLE_IDS -> {
                try {
                    val reader = JSONArray(response.second)

                    // Loop once to add all consoles to view
                    for (i in 0 until reader.length()) {
                        consoleAdapter.addConsole(reader.getJSONObject(i).getString("ID"), reader.getJSONObject(i).getString("Name"))
                    }
                    // Loop twice if we wish to hide empty consoles
                    if (hideEmptyConsoles) {
                        val db = requireContext().let { RetroAchievementsDatabase.getInstance(it) }
                        db?.let {
                            for (i in 0 until reader.length()) {
                                val id = reader.getJSONObject(i).getString("ID")
                                val name = reader.getJSONObject(i).getString("Name")
                                parseConsoleHelper(it, id.toInt(), name, if (i == reader.length() - 1) view else null)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Couldn't parse console IDs", e)
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    /**
     * Only needed because problems arise when using withContext blocks in try-catch blocks.
     */
    private suspend fun parseConsoleHelper(db: RetroAchievementsDatabase, id: Int, name: String, view: View?) {
        withContext(IO) {
            val current = db.consoleDao()?.getConsoleWithID(id)
            if (current?.isNotEmpty() == true && current[0]?.gameCount == 0) {
                consoleAdapter.removeConsole(name)
            }
            view?.let {
                withContext(Main) {
                    populateConsolesView(view)
                }
            }
        }
    }

    private fun populateConsolesView(view: View) {
        view.findViewById<RecyclerView>(R.id.list_console).scrollToPosition(0)
        view.findViewById<View>(R.id.list_hiding_fade).visibility = View.GONE
        view.findViewById<View>(R.id.list_hiding_progress).visibility = View.GONE
    }

    companion object {
        private val TAG = ConsoleListFragment::class.java.simpleName
    }
}