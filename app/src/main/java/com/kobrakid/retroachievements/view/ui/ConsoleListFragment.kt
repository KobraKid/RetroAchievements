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
import com.kobrakid.retroachievements.model.IGame
import com.kobrakid.retroachievements.view.adapter.ConsoleAdapter
import com.kobrakid.retroachievements.viewmodel.ConsoleListViewModel

class ConsoleListFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private val viewModel: ConsoleListViewModel by viewModels()
    private var _binding: FragmentConsoleListBinding? = null
    private val binding get() = _binding!!
    private val consoleAdapter = ConsoleAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentConsoleListBinding.inflate(inflater, container, false)
        activity?.title = "Consoles"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.listHidingFade.visibility = if (loading) View.VISIBLE else View.GONE
            binding.listHidingProgress.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.consoleList.observe(viewLifecycleOwner) {
            consoleAdapter.setData(it)
        }
        // Initialize views
        binding.listConsole.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = consoleAdapter
        }
        viewModel.gameSuggestions.observe(viewLifecycleOwner) {
            try {
                val gameSuggestionAdapter = object : ArrayAdapter<IGame>(requireContext(), android.R.layout.simple_list_item_1, it) {
                    override fun getItemId(position: Int): Long {
                        return getItem(position)?.id?.toLong() ?: 0L
                    }
                }
                binding.gameSearch.apply {
                    setAdapter(gameSuggestionAdapter)
                    // Open a game details page when an item is tapped
                    setOnItemClickListener { _, _, _, id ->
                        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
                        navController.navigate(ConsoleListFragmentDirections.actionConsoleListFragmentToGameDetailsFragment(id.toString()))
                    }
                    // Open the first search result when "Go" ime button tapped
                    setOnEditorActionListener { view, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_GO && !gameSuggestionAdapter.isEmpty) {
                            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
                            navController.navigate(ConsoleListFragmentDirections.actionConsoleListFragmentToGameDetailsFragment(
                                    gameSuggestionAdapter.getItem(0)?.id.toString()))
                            true
                        } else false
                    }
                }
            } catch (e: IllegalStateException) {
                // TODO check if this should really be tried/caught
                Log.e(TAG, "Context was null", e)
            }
        }
        viewModel.getConsoles(context
                ?.getSharedPreferences(context?.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                ?.getBoolean(context?.getString(R.string.empty_console_hide_setting), false)
                ?: false)
        viewModel.getGameSuggestions()
    }

    override fun onClick(view: View) {
        navController.navigate(ConsoleListFragmentDirections.actionConsoleListFragmentToConsoleGamesFragment(
                Console(view.findViewById<TextView>(R.id.console_id).text.toString(),
                        view.findViewById<TextView>(R.id.console_name).text.toString())))
    }

    companion object {
        private val TAG = Consts.BASE_TAG + ConsoleListFragment::class.java.simpleName
    }

}