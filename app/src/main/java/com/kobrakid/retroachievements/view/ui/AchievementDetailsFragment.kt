package com.kobrakid.retroachievements.view.ui

import android.annotation.SuppressLint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentAchievementDetailsBinding
import com.kobrakid.retroachievements.model.Achievement
import com.kobrakid.retroachievements.view.viewpager.ToggleableViewPager
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

/**
 * This class is responsible for showing more detailed information on a particular achievement.
 */
class AchievementDetailsFragment : Fragment() {

    private var _binding: FragmentAchievementDetailsBinding? = null
    private val binding get() = _binding!!
    private val tapDetector = GestureDetector(context, GestureTap())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentAchievementDetailsBinding.inflate(inflater, container, false)
        // FIXME: Shared transition from [@link AchievementAdapter.AchievementViewHolderListenerImpl] not working
        binding.achievementDetailsBadge.transitionName = arguments?.getString("transitionName")
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val achievement = arguments?.getParcelable("achievement") ?: Achievement()
        val numDistinctCasual = arguments?.getDouble("numDistinctCasual") ?: 0.0

        view.setOnTouchListener { _: View?, e: MotionEvent? ->
            tapDetector.onTouchEvent(e)
            true
        }

        // Set fields from transferred data
        binding.achievementDetailsTitle.text = achievement.title
        binding.achievementDetailsDescription.text = achievement.description
        binding.achievementDetailsDate.text = if (achievement.dateEarned.isNotEmpty()) getString(R.string.date_earned_upper, achievement.dateEarned) else ""
        binding.achievementDetailsCompletionText.text = getString(
                R.string.earned_by_details,
                achievement.numAwarded,
                numDistinctCasual.toInt(),
                DecimalFormat("@@@@")
                        .format(achievement.numAwarded.toDouble() / numDistinctCasual * 100.0))
        binding.achievementDetailsCompletionHardcoreText.text = getString(
                R.string.earned_by_hc_details,
                achievement.numAwardedHardcore,
                DecimalFormat("@@@@")
                        .format(achievement.numAwardedHardcore.toDouble() / numDistinctCasual * 100.0))
        binding.achievementDetailsMetadata.text = getString(R.string.metadata,
                achievement.author,
                achievement.dateCreated,
                achievement.dateModified)
        binding.achievementDetailsCompletion.apply {
            max = numDistinctCasual.toInt()
            progress = achievement.numAwarded.toInt()
        }
        binding.achievementDetailsCompletionHardcore.apply {
            max = numDistinctCasual.toInt()
            progress = achievement.numAwardedHardcore.toInt()
        }
        ResourcesCompat.getDrawable(resources, R.drawable.favicon, activity?.theme)?.let {
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + achievement.badgeName + ".png")
                    .placeholder(it)
                    .into(binding.achievementDetailsBadge, object : Callback {
                        override fun onSuccess() {
                            if (achievement.dateEarned.isEmpty()) {
                                val matrix = ColorMatrix()
                                matrix.setSaturation(0f)
                                binding.achievementDetailsBadge.colorFilter = ColorMatrixColorFilter(matrix)
                            } else {
                                binding.achievementDetailsBadge.clearColorFilter()
                            }
                        }

                        override fun onError(e: Exception) {}
                    })
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.findViewById<ToggleableViewPager>(R.id.game_details_view_pager)?.setPagingEnabled(false)
    }

    override fun onStop() {
        super.onStop()
        activity?.findViewById<ToggleableViewPager>(R.id.game_details_view_pager)?.setPagingEnabled(true)
    }

    private inner class GestureTap : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            activity
                    ?.findViewById<View>(R.id.achievement_summary)
                    ?.findFragment<AchievementSummaryFragment>()
                    ?.childFragmentManager
                    ?.popBackStack()
            return true
        }
    }
}