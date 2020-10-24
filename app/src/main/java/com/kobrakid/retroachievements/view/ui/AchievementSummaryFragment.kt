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
        }
        viewModel.game.observe(viewLifecycleOwner) { game ->
            val visibleIfAchievements = if (game.totalAchievements == 0) View.GONE else View.VISIBLE
            binding.gameDetailsAchievementsEarned.apply {
                visibility = visibleIfAchievements
                maximum = game.totalAchievements.toFloat()
                progress = game.numAchievementsEarned.toFloat()
            }
            binding.gameDetailsAchievementsEarnedText.apply {
                visibility = visibleIfAchievements
                text = if (game.totalAchievements == 0) "" else getString(R.string.percent, (game.numAchievementsEarned * 100).div(game.totalAchievements))
            }
            binding.gameDetailsAchievementsEarnedHc.apply {
                visibility = visibleIfAchievements
                maximum = game.totalAchievements.toFloat()
                progress = game.numAchievementsEarnedHC.toFloat()
            }
            binding.gameDetailsAchievementsEarnedHcText.apply {
                visibility = visibleIfAchievements
                text = if (game.totalAchievements == 0) "" else getString(R.string.diminished_percent, (game.numAchievementsEarnedHC * 200).div(game.totalAchievements))
            }
            binding.gameDetailsPoints.apply {
                visibility = visibleIfAchievements
                maximum = game.totalPoints.toFloat()
                progress = game.earnedPoints.toFloat()
            }
            binding.gameDetailsPointsText.apply {
                visibility = visibleIfAchievements
                text = game.earnedPoints.toString()
            }
            binding.gameDetailsPointsTotalText.apply {
                visibility = visibleIfAchievements
                text = getString(R.string.out_of, game.totalPoints.toString())
            }
            binding.gameDetailsRatio.apply {
                visibility = visibleIfAchievements
                maximum = game.totalTruePoints.toFloat()
                progress = game.earnedTruePoints.toFloat()
            }
            binding.gameDetailsRatioText.apply {
                visibility = visibleIfAchievements
                text = game.earnedTruePoints.toString()
            }
            binding.gameDetailsRatioTotalText.apply {
                visibility = visibleIfAchievements
                text = getString(R.string.out_of, game.totalTruePoints.toString())
            }
            binding.gameDetailsAchievementsRecyclerView.visibility = visibleIfAchievements
            binding.gameDetailsNoAchievements.visibility = if (game.totalAchievements == 0) View.VISIBLE else View.GONE
            binding.gameDetailsAchievementsTitle.visibility = visibleIfAchievements
            binding.gameDetailsAchievementsEarnedSubtitle.visibility = visibleIfAchievements
            binding.gameDetailsPointsSubtitle.visibility = visibleIfAchievements
            binding.gameDetailsAchievementsRatioSubtitle.visibility = visibleIfAchievements
            (binding.gameDetailsAchievementsRecyclerView.adapter as AchievementAdapter).setNumDistinctCasual(game.numDistinctCasual)
        }
        viewModel.achievements.observe(viewLifecycleOwner) {
            (binding.gameDetailsAchievementsRecyclerView.adapter as AchievementAdapter).setAchievements(it)
        }
        viewModel.getGameInfoForUser((activity as MainActivity?)?.user?.username, arguments?.getString("GameID", "0"))
    }
}