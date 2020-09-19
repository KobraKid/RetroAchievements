package com.kobrakid.retroachievements.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.adapter.ParticipantsAdapter
import com.kobrakid.retroachievements.ra.Leaderboard
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {

    private val args: LeaderboardFragmentArgs by navArgs()
    private var leaderboard = Leaderboard()
    private val participantsAdapter: ParticipantsAdapter = ParticipantsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboard = args.leaderboard ?: Leaderboard()
        requireActivity().title = if (leaderboard.game.isNotEmpty() && leaderboard.title.isNotEmpty()) "${leaderboard.game}: ${leaderboard.title}" else ""
        Picasso.get()
                .load(leaderboard.image)
                .placeholder(R.drawable.game_placeholder)
                .into(view.findViewById<ImageView>(R.id.leaderboard_game_icon))
        if (leaderboard.console.isNotEmpty())
            view.findViewById<TextView>(R.id.leaderboard_title).text = getString(R.string.leaderboard_title_template, leaderboard.title, leaderboard.console)
        else
            view.findViewById<TextView>(R.id.leaderboard_title).text = requireActivity().title
        view.findViewById<TextView>(R.id.leaderboard_description).text = leaderboard.description
        when {
            leaderboard.type.contains("Score") -> view.findViewById<TextView>(R.id.leaderboard_type).text = getString(R.string.type_score, leaderboard.type)
            leaderboard.type.contains("Time") -> view.findViewById<TextView>(R.id.leaderboard_type).text = getString(R.string.type_time, leaderboard.type)
            else -> view.findViewById<TextView>(R.id.leaderboard_type).text = leaderboard.type
        }
        val rankedUsers = view.findViewById<RecyclerView>(R.id.leaderboard_participants)
        rankedUsers.adapter = participantsAdapter
        rankedUsers.layoutManager = LinearLayoutManager(requireContext())
        if (participantsAdapter.itemCount == 0) {
            CoroutineScope(Dispatchers.IO).launch {
                RetroAchievementsApi.GetLeaderboard(requireContext(), leaderboard.id, leaderboard.numResults) { parseLeaderboard(view, it) }
            }
        }
    }

    private suspend fun parseLeaderboard(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_LEADERBOARD -> {
                withContext(Dispatchers.Default) {
                    val document = Jsoup.parse(response.second)
                    val userData = document.select("td.lb_user")
                    val resultData = document.select("td.lb_result")
                    val dateData = document.select("td.lb_date")
                    val spinner = view.findViewById<ProgressBar>(R.id.leaderboard_progress_spinner)
                    withContext(Main) {
                        spinner.max = userData.size
                    }
                    for (i in userData.indices) {
                        updateProgressBar(view, i)
                        participantsAdapter.addParticipant(
                                userData[i].text(),
                                resultData[i].text(),
                                dateData[i].text())
                    }
                    withContext(Main) {
                        spinner.visibility = View.GONE
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private suspend fun updateProgressBar(view: View, i: Int) {
        withContext(Main) {
            view.findViewById<ProgressBar>(R.id.leaderboard_progress_spinner).progress = i
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + LeaderboardFragment::class.java.simpleName
    }
}