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
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentLeaderboardBinding
import com.kobrakid.retroachievements.view.adapter.ParticipantsAdapter
import com.kobrakid.retroachievements.viewmodel.LeaderboardViewModel
import com.squareup.picasso.Picasso

class LeaderboardFragment : Fragment(), View.OnClickListener {

    private val args: LeaderboardFragmentArgs by navArgs()
    private val viewModel: LeaderboardViewModel by viewModels()
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.leaderboardParticipants.apply {
            adapter = ParticipantsAdapter(this@LeaderboardFragment, (activity as MainActivity?)?.user?.username)
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.leaderboard.observe(viewLifecycleOwner) {
            activity?.title = "${it.gameId}: ${it.title}"
            Picasso.get()
                    .load(Consts.BASE_URL + "/Images/" + it.icon)
                    .placeholder(R.drawable.game_placeholder)
                    .into(binding.leaderboardGameIcon)
            binding.leaderboardTitle.apply {
                text = if (it.console.isNotEmpty()) getString(R.string.leaderboard_title_template, it.title, it.console) else "${it.gameId}: ${it.title}"
                isSelected = true
            }
            binding.leaderboardDescription.text = it.description
            binding.leaderboardType.text = when {
                it.type.contains("Score") -> getString(R.string.type_score, it.type, it.numResults)
                it.type.contains("Time") -> getString(R.string.type_time, it.type, it.numResults)
                else -> it.type
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.leaderboardProgressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.participantCount.observe(viewLifecycleOwner) {
            binding.leaderboardProgressBar.max = it
        }
        viewModel.progress.observe(viewLifecycleOwner) {
            binding.leaderboardProgressBar.progress = it
        }
        viewModel.participants.observe(viewLifecycleOwner) {
            (binding.leaderboardParticipants.adapter as ParticipantsAdapter).setParticipants(it)
        }
        viewModel.setLeaderboard(args.leaderboard ?: "0")
    }

    override fun onClick(view: View?) {
        if (view != null) {
            findNavController().navigate(
                    LeaderboardFragmentDirections.actionLeaderboardFragmentToUserSummaryFragment(
                            view.findViewById<TextView>(R.id.participant_username).text.toString()))
        }
    }
}