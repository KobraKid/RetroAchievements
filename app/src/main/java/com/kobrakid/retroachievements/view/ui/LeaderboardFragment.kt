package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentLeaderboardBinding
import com.kobrakid.retroachievements.model.Leaderboard
import com.kobrakid.retroachievements.view.adapter.ParticipantsAdapter
import com.kobrakid.retroachievements.viewmodel.LeaderboardViewModel
import com.squareup.picasso.Picasso

class LeaderboardFragment : Fragment(), View.OnClickListener {

    private val args: LeaderboardFragmentArgs by navArgs()
    private val viewModel: LeaderboardViewModel by viewModels()
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private var leaderboard = Leaderboard()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboard = args.leaderboard ?: Leaderboard()
        activity?.title =
                if (leaderboard.game.isNotEmpty() && leaderboard.title.isNotEmpty()) "${leaderboard.game}: ${leaderboard.title}"
                else ""
        // Control progress bar with view model
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.leaderboardProgressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.leaderboardCount.observe(viewLifecycleOwner) {
            binding.leaderboardProgressBar.max = it
        }
        viewModel.currentLeaderboard.observe(viewLifecycleOwner) {
            binding.leaderboardProgressBar.progress = it
        }
        // Set up views from leaderboard argument
        Picasso.get()
                .load(leaderboard.image)
                .placeholder(R.drawable.game_placeholder)
                .into(binding.leaderboardGameIcon)
        binding.leaderboardTitle.apply {
            text = if (leaderboard.console.isNotEmpty()) getString(R.string.leaderboard_title_template, leaderboard.title, leaderboard.console) else activity?.title
            isSelected = true
        }
        binding.leaderboardDescription.text = leaderboard.description
        binding.leaderboardType.text = when {
            leaderboard.type.contains("Score") -> getString(R.string.type_score, leaderboard.type, leaderboard.numResults)
            leaderboard.type.contains("Time") -> getString(R.string.type_time, leaderboard.type, leaderboard.numResults)
            else -> leaderboard.type
        }
        binding.leaderboardParticipants.apply {
            adapter = ParticipantsAdapter(this@LeaderboardFragment, (activity as MainActivity?)?.user?.username)
            layoutManager = LinearLayoutManager(context)
        }
        // Perform view model work
        viewModel.participants.observe(viewLifecycleOwner) {
            (binding.leaderboardParticipants.adapter as ParticipantsAdapter).setParticipants(it)
        }
        viewModel.setLeaderboard(leaderboard)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            findNavController().navigate(
                    LeaderboardFragmentDirections.actionLeaderboardFragmentToUserSummaryFragment(
                            view.findViewById<TextView>(R.id.participant_username).text.toString()))
        }
    }
}