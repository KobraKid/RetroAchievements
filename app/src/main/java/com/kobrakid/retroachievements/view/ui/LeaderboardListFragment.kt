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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentLeaderboardListBinding
import com.kobrakid.retroachievements.view.adapter.LeaderboardListAdapter
import com.kobrakid.retroachievements.viewmodel.LeaderboardListViewModel

class LeaderboardListFragment : Fragment() {

    private val viewModel: LeaderboardListViewModel by viewModels()
    private var _binding: FragmentLeaderboardListBinding? = null
    private val binding get() = _binding!!
    private val leaderboardsAdapter by lazy { LeaderboardListAdapter(findNavController()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
            adapter = leaderboardsAdapter
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
                            leaderboardsAdapter.getUniqueConsoles().toList())
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
                leaderboardsAdapter.consoleFilter = adapterView.getItemAtPosition(pos).toString()
                leaderboardsAdapter.filter.filter(leaderboardsAdapter.filterTemplate)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                binding.leaderboardFastScroller.scrollTo(0, 0)
                leaderboardsAdapter.consoleFilter = ""
                leaderboardsAdapter.filter.filter(leaderboardsAdapter.filterTemplate)
            }
        }
        binding.leaderboardsFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(editable: Editable) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                binding.leaderboardFastScroller.scrollTo(0, 0)
                leaderboardsAdapter.titleFilter = charSequence.toString()
                leaderboardsAdapter.filter.filter(leaderboardsAdapter.filterTemplate)
            }
        })

        // Start populating the view
        viewModel.init("${context?.filesDir?.path}/${getString(R.string.file_leaderboards_cache)}")
    }
}