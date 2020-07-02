package com.kobrakid.retroachievements.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.AchievementAdapter.AchievementViewHolder
import com.kobrakid.retroachievements.fragment.AchievementDetailsFragment
import com.kobrakid.retroachievements.ra.Achievement
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class AchievementAdapter(private val fragment: Fragment) : RecyclerView.Adapter<AchievementViewHolder>() {

    private val ids = mutableListOf<String>()
    private val badges = mutableListOf<String>()
    private val titles = mutableListOf<String>()
    private val points = mutableListOf<String>()
    private val trueRatios = mutableListOf<String>()
    private val descriptions = mutableListOf<String>()
    private val datesEarned = mutableListOf<String>()
    private val numsAwarded = mutableListOf<String>()
    private val numsAwardedHC = mutableListOf<String>()
    private val authors = mutableListOf<String>()
    private val datesCreated = mutableListOf<String>()
    private val datesModified = mutableListOf<String>()
    private val hardcoreEarnings = mutableListOf<Boolean>()
    private var numDistinctCasual = 1.0
    private val viewHolderListener = AchievementViewHolderListenerImpl(fragment, this)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val layout = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_holder_achievement_summary,
                        parent,
                        false) as ConstraintLayout
        return AchievementViewHolder(layout, viewHolderListener)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {

        // Hidden Text Views
        holder.layout.findViewById<TextView>(R.id.recycler_view_position).text = position.toString()
        holder.layout.findViewById<TextView>(R.id.achievement_summary_author).text = authors[position]
        holder.layout.findViewById<TextView>(R.id.achievement_summary_created).text = datesCreated[position]
        holder.layout.findViewById<TextView>(R.id.achievement_summary_modified).text = datesModified[position]

        // Badge
        if (hardcoreEarnings[position]) {
            holder.layout.findViewById<View>(R.id.achievement_summary_badge).background = holder.itemView.context.getDrawable(R.drawable.image_view_border)
        } else {
            holder.layout.findViewById<View>(R.id.achievement_summary_badge).background = null
        }
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + badges[position] + ".png")
                .placeholder(R.drawable.favicon)
                .into(holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge))
        holder.layout.findViewById<View>(R.id.achievement_summary_badge).transitionName = "achievement_$position"
        holder.layout.findViewById<TextView>(R.id.achievement_summary_badge_id).text = badges[position]

        // Text descriptions
        holder.layout.findViewById<TextView>(R.id.achievement_summary_id).text = ids[position]
        holder.layout.findViewById<TextView>(R.id.achievement_summary_title).text = fragment.getString(R.string.achievement_summary_title, titles[position], points[position], trueRatios[position])
        holder.layout.findViewById<TextView>(R.id.achievement_summary_desc).text = descriptions[position]
        if (datesEarned[position].startsWith("NoDate")) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge).colorFilter = ColorMatrixColorFilter(matrix)
            holder.layout.findViewById<TextView>(R.id.achievement_summary_date).text = ""
        } else {
            holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge).clearColorFilter()
            holder.layout.findViewById<TextView>(R.id.achievement_summary_date).text = fragment.getString(R.string.date_earned_lower, datesEarned[position])
        }
        holder.layout.findViewById<TextView>(R.id.achievement_summary_stats).text = fragment.getString(R.string.won_by,
                numsAwarded[position],
                numsAwardedHC[position],
                numDistinctCasual.toInt(),
                DecimalFormat("@@@@")
                        .format(numsAwarded[position].toDouble() / numDistinctCasual * 100.0))

        // Double-layered Progress Bar
        val progressBar = holder.layout.findViewById<ProgressBar>(R.id.achievement_summary_progress)
        progressBar.progress = (numsAwardedHC[position].toDouble() / numDistinctCasual * 10000.0).toInt()
        progressBar.secondaryProgress = (numsAwarded[position].toDouble() / numDistinctCasual * 10000.0).toInt()
    }

    override fun getItemCount(): Int {
        return ids.size
    }

    fun addAchievement(index: Int, achievement: Achievement) {
        ids.add(index, achievement.id)
        badges.add(index, achievement.badge)
        titles.add(index, achievement.title)
        points.add(index, achievement.point)
        trueRatios.add(index, achievement.trueRatio)
        descriptions.add(index, achievement.description)
        datesEarned.add(index, achievement.dateEarned)
        hardcoreEarnings.add(index, achievement.earnedHardcore)
        numsAwarded.add(index, achievement.numAwarded)
        numsAwardedHC.add(index, achievement.numAwardedHC)
        authors.add(index, achievement.author)
        datesCreated.add(index, achievement.dateCreated)
        datesModified.add(index, achievement.dateModified)
        notifyItemInserted(index)
    }

    fun setNumDistinctCasual(n: Double) {
        numDistinctCasual = n
    }

    /* Inner Classes and Interfaces */
    interface AchievementViewHolderListener {
        fun onItemClicked(view: View, adapterPosition: Int)
    }

    class AchievementViewHolderListenerImpl internal constructor(private val fragment: Fragment, private val adapter: AchievementAdapter) : AchievementViewHolderListener {
        override fun onItemClicked(view: View, adapterPosition: Int) { // Create a new transition
            /*
            val transitionSet = TransitionSet()
            transitionSet.ordering = TransitionSet.ORDERING_TOGETHER
            // Get the adapter position of the first child
            val firstChildIndex =
                    (fragment as AchievementSummaryFragment).layoutManager
                            ?.getChildAt(0)
                            ?.findViewById<TextView>(R.id.recycler_view_position)
                            ?.text.toString().toInt()
            // FIXME Animation no longer occurring 😢 (but was already pretty janky)
            // Custom logic to slide higher achievements up, lower ones down
            for (i in 0 until adapter.itemCount) {
                val slide = Slide()
                slide.duration =
                        fragment
                                .getActivity()
                                ?.resources
                                ?.getInteger(R.integer.animation_duration)
                                ?.toLong()
                                ?: 0
                slide.addTarget(fragment.layoutManager?.getChildAt(i))
                if (i + firstChildIndex < adapterPosition) {
                    slide.slideEdge = Gravity.TOP
                    transitionSet.addTransition(slide)
                } else if (i + firstChildIndex > adapterPosition) {
                    slide.slideEdge = Gravity.BOTTOM
                    transitionSet.addTransition(slide)
                }
            }
            fragment.setExitTransition(transitionSet)
            */
            // Set up the target fragment
            val transitionBadge = view.findViewById<ImageView>(R.id.achievement_summary_badge)
            val detailsFragment: Fragment = AchievementDetailsFragment()
            val bundle = Bundle()
            bundle.putString("Position", "" + adapterPosition)
            bundle.putString("GameID", adapter.ids[adapterPosition])
            bundle.putString("ImageIcon", adapter.badges[adapterPosition])
            bundle.putString("Title", adapter.titles[adapterPosition])
            bundle.putString("Points", adapter.points[adapterPosition])
            bundle.putString("TrueRatio", adapter.trueRatios[adapterPosition])
            bundle.putString("Description", adapter.descriptions[adapterPosition])
            bundle.putString("DateEarned", adapter.datesEarned[adapterPosition])
            bundle.putString("NumAwarded", adapter.numsAwarded[adapterPosition])
            bundle.putString("NumAwardedHardcore", adapter.numsAwardedHC[adapterPosition])
            bundle.putString("Author", adapter.authors[adapterPosition])
            bundle.putString("DateCreated", adapter.datesCreated[adapterPosition])
            bundle.putString("DateModified", adapter.datesModified[adapterPosition])
            bundle.putDouble("NumDistinctPlayersCasual", adapter.numDistinctCasual)
            detailsFragment.arguments = bundle
            fragment
                    .activity
                    ?.supportFragmentManager
                    ?.beginTransaction()
                    ?.setReorderingAllowed(true)
                    ?.addSharedElement(transitionBadge, transitionBadge.transitionName)
                    ?.replace(R.id.game_details_frame, detailsFragment, AchievementDetailsFragment::class.java.simpleName)
                    ?.addToBackStack(null)
                    ?.commit()
        }

    }

    class AchievementViewHolder internal constructor(val layout: ConstraintLayout, private val viewHolderListener: AchievementViewHolderListener) : RecyclerView.ViewHolder(layout), View.OnClickListener {
        override fun onClick(view: View) {
            viewHolderListener.onItemClicked(view, adapterPosition)
        }

        init {
            layout.setOnClickListener(this)
        }
    }

}