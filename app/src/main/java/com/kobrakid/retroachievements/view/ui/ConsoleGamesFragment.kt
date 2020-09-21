package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentConsoleGamesBinding
import com.kobrakid.retroachievements.model.Console
import com.kobrakid.retroachievements.view.adapter.GameSummaryAdapter
import com.kobrakid.retroachievements.viewmodel.ConsoleGamesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * This Activity lists all of the games of a certain console.
 */
class ConsoleGamesFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController
    private val args: ConsoleGamesFragmentArgs by navArgs()
    private val viewModel: ConsoleGamesViewModel by viewModels()
    private var _binding: FragmentConsoleGamesBinding? = null
    private val binding get() = _binding!!
    private val gameSummaryAdapter: GameSummaryAdapter by lazy {
        GameSummaryAdapter(this, context?.let { ContextCompat.getDrawable(it, R.drawable.image_view_border) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentConsoleGamesBinding.inflate(inflater, container, false)
        retainInstance = true
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val console = args.console ?: Console()
        activity?.title = console.name
        val gameListRecyclerView = binding.listGames
        gameListRecyclerView.adapter = gameSummaryAdapter
        gameListRecyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.loading.observe(viewLifecycleOwner, {
            binding.listHidingProgress.visibility = if (it) View.VISIBLE else View.GONE
            binding.listNoGames.visibility = if (!it && gameSummaryAdapter.itemCount == 0) View.VISIBLE else View.GONE
        })
        viewModel.consoleGamesList.observe(viewLifecycleOwner, {
            gameSummaryAdapter.setData(it)
        })
        binding.listGamesFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                gameSummaryAdapter.filter.filter(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        CoroutineScope(Main).launch { viewModel.setConsoleID(context, console.id) }
    }

    override fun onClick(view: View) {
        navController.navigate(ConsoleGamesFragmentDirections.actionConsoleGamesFragmentToGameDetailsFragment(
                view.findViewById<TextView>(R.id.game_summary_game_id).text.toString()))
    }
}