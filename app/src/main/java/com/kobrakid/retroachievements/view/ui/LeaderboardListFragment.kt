package com.kobrakid.retroachievements.view.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Filterable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.databinding.FragmentLeaderboardListBinding
import com.kobrakid.retroachievements.view.adapter.LeaderboardListAdapter
import com.kobrakid.retroachievements.viewmodel.LeaderboardListViewModel

class LeaderboardListFragment : Fragment(), View.OnClickListener {

    private val viewModel: LeaderboardListViewModel by viewModels()
    private var _binding: FragmentLeaderboardListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardListBinding.inflate(inflater, container, false)
        activity?.title = "Leaderboards"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.leaderboardsGames.apply {
            adapter = LeaderboardListAdapter(this@LeaderboardListFragment)
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.leaderboardsProgress.visibility = if (it) View.VISIBLE else View.GONE
            binding.leaderboardPopulatingFade.visibility = if (it) View.VISIBLE else View.GONE
            binding.leaderboardFastScroller.isFastScrollEnabled = !it
            if (it) {
                ObjectAnimator.ofInt(binding.leaderboardsProgress, "secondaryProgress", 100).apply {
                    duration = 1000
                    interpolator = AccelerateDecelerateInterpolator()
                }.start()
            } else {
                context?.let { context ->
                    binding.leaderboardsConsoleFilter.adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_dropdown_item,
                            (binding.leaderboardsGames.adapter as LeaderboardListAdapter).getUniqueConsoles().toList())
                }
            }
        }
        viewModel.max.observe(viewLifecycleOwner) {
            binding.leaderboardsProgress.max = it
        }
        viewModel.progress.observe(viewLifecycleOwner) {
            binding.leaderboardsProgress.progress = it
        }
        viewModel.leaderboards.observe(viewLifecycleOwner) {
            (binding.leaderboardsGames.adapter as LeaderboardListAdapter).setLeaderboards(it)
        }

        // Set up Filters
        binding.leaderboardsConsoleFilter.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                binding.leaderboardFastScroller.scrollTo(0, 0)
                (binding.leaderboardsGames.adapter as LeaderboardListAdapter).consoleFilter = adapterView.getItemAtPosition(pos).toString()
                (binding.leaderboardsGames.adapter as Filterable).filter.filter((binding.leaderboardsGames.adapter as LeaderboardListAdapter).filterTemplate)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                binding.leaderboardFastScroller.scrollTo(0, 0)
                (binding.leaderboardsGames.adapter as LeaderboardListAdapter).consoleFilter = ""
                (binding.leaderboardsGames.adapter as Filterable).filter.filter((binding.leaderboardsGames.adapter as LeaderboardListAdapter).filterTemplate)
            }
        }
        binding.leaderboardsFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                binding.leaderboardFastScroller.scrollTo(0, 0)
                (binding.leaderboardsGames.adapter as LeaderboardListAdapter).titleFilter = charSequence.toString()
                (binding.leaderboardsGames.adapter as Filterable).filter.filter((binding.leaderboardsGames.adapter as LeaderboardListAdapter).filterTemplate)
            }
        })
        viewModel.getLeaderboardsForGame(arguments?.getString("GameID") ?: "0")
    }

    override fun onClick(view: View) {
        Navigation.findNavController(view).navigate(
                GameDetailsFragmentDirections.actionGameDetailsFragmentToLeaderboardFragment(view.id.toString()))
    }
}