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
    private var tapDetector: GestureDetector? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_achievement_details, container, false)
        view.transitionName = "achievement_" + arguments?.getString("Position")

        // Set fields from transferred data
        (view.findViewById<View>(R.id.achievement_details_title) as TextView).text = arguments!!.getString("Title")
        (view.findViewById<View>(R.id.achievement_details_description) as TextView).text = arguments!!.getString("Description")
        if (!arguments!!.getString("DateEarned")?.startsWith("NoDate")!!) {
            (view.findViewById<View>(R.id.achievement_details_date) as TextView).text = context?.getString(R.string.date_earned, arguments!!.getString("DateEarned"))
        }
        (view.findViewById<View>(R.id.achievement_details_completion_text) as TextView).text = context?.getString(
                R.string.earned_by_details,
                arguments!!.getString("NumAwarded"),
                arguments!!.getDouble("NumDistinctPlayersCasual").toInt(),
                DecimalFormat("@@@@")
                        .format(arguments!!.getString("NumAwarded")?.toDouble()
                                ?: 0 / arguments!!.getDouble("NumDistinctPlayersCasual") * 100.0))
        (view.findViewById<View>(R.id.achievement_details_completion_hardcore_text) as TextView).text = context!!.getString(
                R.string.earned_by_hc_details,
                arguments!!.getString("NumAwardedHardcore"),
                DecimalFormat("@@@@")
                        .format(arguments!!.getString("NumAwardedHardcore")?.toDouble()
                                ?: 0 / arguments!!.getDouble("NumDistinctPlayersCasual") * 100.0))
        (view.findViewById<View>(R.id.achievement_details_metadata) as TextView).text = getString(R.string.metadata,
                arguments!!.getString("Author"),
                arguments!!.getString("DateCreated"),
                arguments!!.getString("DateModified"))
        var progressBar = view.findViewById<ProgressBar>(R.id.achievement_details_completion_hardcore)
        progressBar.progress = (arguments!!.getString("NumAwardedHardcore")?.toDouble()
                ?: 0 / arguments!!.getDouble("NumDistinctPlayersCasual") * 10000.0).toInt()
        progressBar = view.findViewById(R.id.achievement_details_completion)
        progressBar.progress = (arguments!!.getString("NumAwarded")?.toDouble()
                ?: 0 / arguments!!.getDouble("NumDistinctPlayersCasual") * 10000.0).toInt()

        // postponeEnterTransition();
        val badge = view.findViewById<ImageView>(R.id.achievement_details_badge)
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + arguments!!.getString("ImageIcon") + ".png")
                .placeholder(resources.getDrawable(R.drawable.favicon, activity?.theme))
                .into(badge, object : Callback {
                    override fun onSuccess() {
                        if (arguments!!.getString("DateEarned")?.startsWith("NoDate") == true) {
                            val matrix = ColorMatrix()
                            matrix.setSaturation(0f)
                            (view.findViewById<View>(R.id.achievement_details_badge) as ImageView).colorFilter = ColorMatrixColorFilter(matrix)
                        } else {
                            (view.findViewById<View>(R.id.achievement_details_badge) as ImageView).clearColorFilter()
                        }
                        prepareSharedElementTransition(view)
                        // startPostponedEnterTransition();
                    }

                    override fun onError(e: Exception) {
                        prepareSharedElementTransition(view)
                        // startPostponedEnterTransition();
                    }
                })
        tapDetector = GestureDetector(context, GestureTap())
        view.setOnTouchListener { _: View?, e: MotionEvent? ->
            tapDetector?.onTouchEvent(e)
            true
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        (activity?.findViewById<View>(R.id.game_details_view_pager) as ToggleableViewPager).setPagingEnabled(false)
    }

    override fun onStop() {
        super.onStop()
        (activity?.findViewById<View>(R.id.game_details_view_pager) as ToggleableViewPager).setPagingEnabled(true)
    }

    private fun prepareSharedElementTransition(view: View) {
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