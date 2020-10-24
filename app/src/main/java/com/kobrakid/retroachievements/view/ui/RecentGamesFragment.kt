package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentRecentGamesBinding
import com.kobrakid.retroachievements.view.adapter.GameSummaryAdapter
import com.kobrakid.retroachievements.viewmodel.RecentGamesViewModel

class RecentGamesFragment : Fragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private val viewModel: RecentGamesViewModel by viewModels()
    private var _binding: FragmentRecentGamesBinding? = null
    private val binding get() = _binding!!

    private var offset = 0
    private val gamesPerAPICall = 15

    // Initially ask for 15 games (prevent spamming API)
    private var gamesAskedFor = 15

    private lateinit var navController: NavController
    private val gameSummaryAdapter: GameSummaryAdapter by lazy { GameSummaryAdapter(this, context) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecentGamesBinding.inflate(inflater, container, false)
        activity?.title = getString(R.string.recent_games_title)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        // Set up RecyclerView
        binding.recentGamesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = gameSummaryAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val scrollPosition = (layoutManager as LinearLayoutManager?)?.findLastVisibleItemPosition()
                    viewModel.getRecentGames(
                            (activity as MainActivity?)?.user?.username,
                            gameSummaryAdapter.itemCount,
                            scrollPosition ?: 0)
                }
            })
        }
        viewModel.recentGames.observe(viewLifecycleOwner) {
            (binding.recentGamesRecyclerView.adapter as GameSummaryAdapter).setGameSummaries(it)
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            (view as SwipeRefreshLayout).isRefreshing = it
        }
        (view as SwipeRefreshLayout).setOnRefreshListener(this)
        viewModel.getRecentGames((activity as MainActivity?)?.user?.username, 0, 0)
    }

    override fun onRefresh() {
        offset = 0
        gamesAskedFor = gamesPerAPICall
        viewModel.onRefresh()
        viewModel.getRecentGames((activity as MainActivity?)?.user?.username, 0, 0)
    }

    override fun onClick(view: View) {
        navController.navigate(RecentGamesFragmentDirections.actionRecentGamesFragmentToGameDetailsFragment(view.id.toString()))
    }
}