package com.kobrakid.retroachievements.fragment

import android.annotation.SuppressLint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.ra.Achievement
import com.kobrakid.retroachievements.viewpager.ToggleableViewPager
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

/**
 * This class is responsible for showing more detailed information on a particular achievement.
 */
class AchievementDetailsFragment : Fragment() {

    private val tapDetector: GestureDetector = GestureDetector(context, GestureTap())

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_achievement_details, container, false).apply {
            setOnTouchListener { _: View?, e: MotionEvent? ->
                tapDetector.onTouchEvent(e)
                true
            }
        }
        // FIXME: Shared transition from [@link AchievementAdapter.AchievementViewHolderListenerImpl] not working
        view.findViewById<ImageView>(R.id.achievement_details_badge).transitionName = arguments?.getString("transitionName")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true
        val achievement = arguments?.getParcelable("achievement") ?: Achievement()

        // Set fields from transferred data
        view.findViewById<TextView>(R.id.achievement_details_title).text = achievement.title
        view.findViewById<TextView>(R.id.achievement_details_description).text = achievement.description
        if (!achievement.dateEarned.startsWith("NoDate")) {
            view.findViewById<TextView>(R.id.achievement_details_date).text = getString(R.string.date_earned_upper, achievement.dateEarned)
        }
        view.findViewById<TextView>(R.id.achievement_details_completion_text).text = getString(
                R.string.earned_by_details,
                achievement.numAwarded,
                achievement.numDistinctCasual.toInt(),
                DecimalFormat("@@@@")
                        .format(achievement.numAwarded.toDouble() / achievement.numDistinctCasual * 100.0))
        view.findViewById<TextView>(R.id.achievement_details_completion_hardcore_text).text = getString(
                R.string.earned_by_hc_details,
                achievement.numAwardedHC,
                DecimalFormat("@@@@")
                        .format(achievement.numAwardedHC.toDouble() / achievement.numDistinctCasual * 100.0))
        view.findViewById<TextView>(R.id.achievement_details_metadata).text = getString(R.string.metadata,
                achievement.author,
                achievement.dateCreated,
                achievement.dateModified)
        var progressBar = view.findViewById<ProgressBar>(R.id.achievement_details_completion_hardcore)
        progressBar.max = achievement.numDistinctCasual.toInt()
        progressBar.progress = achievement.numAwardedHC.toInt()
        progressBar = view.findViewById(R.id.achievement_details_completion)
        progressBar.max = achievement.numDistinctCasual.toInt()
        progressBar.progress = achievement.numAwarded.toInt()

        val badge = view.findViewById<ImageView>(R.id.achievement_details_badge)
        ResourcesCompat.getDrawable(resources, R.drawable.favicon, activity?.theme)?.let {
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + achievement.badge + ".png")
                    .placeholder(it)
                    .into(badge, object : Callback {
                        override fun onSuccess() {
                            if (achievement.dateEarned.startsWith("NoDate")) {
                                val matrix = ColorMatrix()
                                matrix.setSaturation(0f)
                                (view.findViewById<ImageView>(R.id.achievement_details_badge)).colorFilter = ColorMatrixColorFilter(matrix)
                            } else {
                                (view.findViewById<ImageView>(R.id.achievement_details_badge)).clearColorFilter()
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