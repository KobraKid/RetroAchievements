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
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_achievement_details, container, false)
        view.transitionName = "achievement_" + arguments?.getString("Position")

        // Set fields from transferred data
        view.findViewById<TextView>(R.id.achievement_details_title).text = arguments?.getString("Title")
        view.findViewById<TextView>(R.id.achievement_details_description).text = arguments?.getString("Description")
        if (arguments?.getString("DateEarned")?.startsWith("NoDate") != true) {
            view.findViewById<TextView>(R.id.achievement_details_date).text = context?.getString(R.string.date_earned_upper, arguments?.getString("DateEarned"))
        }
        view.findViewById<TextView>(R.id.achievement_details_completion_text).text = context?.getString(
                R.string.earned_by_details,
                arguments?.getString("NumAwarded"),
                arguments?.getDouble("NumDistinctPlayersCasual")?.toInt(),
                DecimalFormat("@@@@")
                        .format(arguments?.getString("NumAwarded")?.toDouble()?.div(arguments?.getDouble("NumDistinctPlayersCasual")
                                ?: 1.0)?.times(100.0)))
        view.findViewById<TextView>(R.id.achievement_details_completion_hardcore_text).text = context!!.getString(
                R.string.earned_by_hc_details,
                arguments?.getString("NumAwardedHardcore"),
                DecimalFormat("@@@@")
                        .format(arguments?.getString("NumAwardedHardcore")?.toDouble()?.div(arguments?.getDouble("NumDistinctPlayersCasual")
                                ?: 1.0)?.times(100.0)))
        view.findViewById<TextView>(R.id.achievement_details_metadata).text = getString(R.string.metadata,
                arguments?.getString("Author"),
                arguments?.getString("DateCreated"),
                arguments?.getString("DateModified"))
        var progressBar = view.findViewById<ProgressBar>(R.id.achievement_details_completion_hardcore)
        progressBar.max = arguments?.getDouble("NumDistinctPlayersCasual")?.toInt() ?: 0
        progressBar.progress = arguments?.getString("NumAwardedHardcore")?.toInt() ?: 0
        progressBar = view.findViewById(R.id.achievement_details_completion)
        progressBar.max = arguments?.getDouble("NumDistinctPlayersCasual")?.toInt() ?: 0
        progressBar.progress = arguments?.getString("NumAwarded")?.toInt() ?: 0

        // postponeEnterTransition();
        val badge = view.findViewById<ImageView>(R.id.achievement_details_badge)
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + arguments!!.getString("ImageIcon") + ".png")
                .placeholder(resources.getDrawable(R.drawable.favicon, activity?.theme))
                .into(badge, object : Callback {
                    override fun onSuccess() {
                        if (arguments?.getString("DateEarned")?.startsWith("NoDate") == true) {
                            val matrix = ColorMatrix()
                            matrix.setSaturation(0f)
                            (view.findViewById<ImageView>(R.id.achievement_details_badge)).colorFilter = ColorMatrixColorFilter(matrix)
                        } else {
                            (view.findViewById<ImageView>(R.id.achievement_details_badge)).clearColorFilter()
                        }
                        prepareSharedElementTransition(view)
                        // startPostponedEnterTransition();
                    }

                    override fun onError(e: Exception) {
                        prepareSharedElementTransition(view)
                        // startPostponedEnterTransition();
                    }
                })
        view.setOnTouchListener { _: View?, e: MotionEvent? ->
            tapDetector.onTouchEvent(e)
            true
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        activity?.findViewById<ToggleableViewPager>(R.id.game_details_view_pager)?.setPagingEnabled(false)
    }

    override fun onStop() {
        super.onStop()
        activity?.findViewById<ToggleableViewPager>(R.id.game_details_view_pager)?.setPagingEnabled(true)
    }

    private fun prepareSharedElementTransition(@Suppress("UNUSED_PARAMETER") view: View) {
//         TODO Figure out why transitions (and/or recycler views) are so awful and hard to work with
//        Transition transition = TransitionInflater.from(getContext()).inflateTransition(R.transition.image_shared_element_transition);
//        setSharedElementEnterTransition(transition);
//        setEnterSharedElementCallback(new SharedElementCallback() {
//            @Override
//            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                sharedElements.put(names.get(0), view);
//            }
//        });
    }

    private inner class GestureTap : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            fragmentManager?.popBackStack()
            return true
        }
    }
}