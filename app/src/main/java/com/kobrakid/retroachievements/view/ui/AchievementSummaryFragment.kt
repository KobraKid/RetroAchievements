package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentAchievementsSummaryBinding
import com.kobrakid.retroachievements.view.adapter.AchievementAdapter
import com.kobrakid.retroachievements.viewmodel.AchievementSummaryViewModel

/**
 * This class is responsible for displaying summary information on all the achievements for a
 * particular game.
 */
class AchievementSummaryFragment : Fragment() {

    private val viewModel: AchievementSummaryViewModel by viewModels()
    private var _binding: FragmentAchievementsSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAchievementsSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gameDetailsAchievementsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = AchievementAdapter(this@AchievementSummaryFragment)
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.gameDetailsLoadingBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.gameDetailsNoAchievements.visibility = if (!loading && binding.gameDetailsAchievementsRecyclerView.adapter?.itemCount ?: 0 == 0) View.VISIBLE else View.GONE
        }
        viewModel.game.observe(viewLifecycleOwner) { game ->
            val visibleIfAchievements = if (game.numAchievements == 0) View.GONE else View.VISIBLE
            binding.gameDetailsAchievementsEarned.apply {
                visibility = visibleIfAchievements
                maximum = game.numAchievements.toFloat()
                progress = game.numAwardedToUser.toFloat()
            }
            binding.gameDetailsAchievementsEarnedText.apply {
                visibility = visibleIfAchievements
                text = if (game.numAchievements == 0) "" else getString(R.string.percent, (game.numAwardedToUser * 100).div(game.numAchievements))
            }
            binding.gameDetailsAchievementsEarnedHc.apply {
                visibility = visibleIfAchievements
                maximum = game.numAchievements.toFloat()
                progress = game.numAwardedToUserHardcore.toFloat()
            }
            binding.gameDetailsAchievementsEarnedHcText.apply {
                visibility = visibleIfAchievements
                text = if (game.numAchievements == 0) "" else getString(R.string.diminished_percent, (game.numAwardedToUserHardcore * 200).div(game.numAchievements))
            }
            binding.gameDetailsPoints.visibility = visibleIfAchievements
            binding.gameDetailsPointsText.visibility = visibleIfAchievements
            binding.gameDetailsPointsTotalText.visibility = visibleIfAchievements
            binding.gameDetailsTruePoints.visibility = visibleIfAchievements
            binding.gameDetailsTruePointsText.visibility = visibleIfAchievements
            binding.gameDetailsTruePointsTotalText.visibility = visibleIfAchievements
            binding.gameDetailsAchievementsRecyclerView.visibility = visibleIfAchievements
            binding.gameDetailsAchievementsTitle.visibility = visibleIfAchievements
            binding.gameDetailsAchievementsEarnedSubtitle.visibility = visibleIfAchievements
            binding.gameDetailsPointsSubtitle.visibility = visibleIfAchievements
            binding.gameDetailsAchievementsTruePointsSubtitle.visibility = visibleIfAchievements
            (binding.gameDetailsAchievementsRecyclerView.adapter as AchievementAdapter).setNumDistinctCasual(game.numDistinctPlayersCasual)
        }
        viewModel.achievements.observe(viewLifecycleOwner) {
            (binding.gameDetailsAchievementsRecyclerView.adapter as AchievementAdapter).setAchievements(it)
        }
        viewModel.totalPoints.observe(viewLifecycleOwner) {
            binding.gameDetailsPoints.maximum = it
            binding.gameDetailsPointsTotalText.text = getString(R.string.out_of, it.toInt().toString())
        }
        viewModel.totalTruePoints.observe(viewLifecycleOwner) {
            binding.gameDetailsTruePoints.maximum = it
            binding.gameDetailsTruePointsTotalText.text = getString(R.string.out_of, it.toInt().toString())
        }
        viewModel.earnedPoints.observe(viewLifecycleOwner) {
            binding.gameDetailsPoints.progress = it
            binding.gameDetailsPointsText.text = it.toInt().toString()
        }
        viewModel.earnedTruePoints.observe(viewLifecycleOwner) {
            binding.gameDetailsTruePoints.progress = it
            binding.gameDetailsTruePointsText.text = it.toInt().toString()
        }
        viewModel.getGameInfoForUser((activity as MainActivity?)?.user?.username, arguments?.getString("GameID", "0"))
    }
}