package com.kobrakid.retroachievements.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.AchievementAdapter.AchievementViewHolder
import com.kobrakid.retroachievements.fragment.AchievementDetailsFragment
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment
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
        val linearLayout = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_holder_achievement_summary,
                        parent,
                        false) as LinearLayout
        return AchievementViewHolder(linearLayout, viewHolderListener)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {

        // Hidden Text Views
        holder.linearLayout.findViewById<TextView>(R.id.recycler_view_position).text = position.toString()
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_author).text = authors[position]
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_created).text = datesCreated[position]
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_modified).text = datesModified[position]

        // Badge
        if (hardcoreEarnings[position]) {
            holder.linearLayout.findViewById<View>(R.id.achievement_summary_badge).background = fragment.context?.getDrawable(R.drawable.image_view_border)
        } else {
            holder.linearLayout.findViewById<View>(R.id.achievement_summary_badge).background = null
        }
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + badges[position] + ".png")
                .into(holder.linearLayout.findViewById<ImageView>(R.id.achievement_summary_badge))
        holder.linearLayout.findViewById<View>(R.id.achievement_summary_badge).transitionName = "achievement_$position"
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_badge_id).text = badges[position]

        // Text descriptions
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_id).text = ids[position]
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_title).text = fragment.getString(R.string.achievement_summary_title, titles[position], points[position], trueRatios[position])
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_desc).text = descriptions[position]
        if (datesEarned[position].startsWith("NoDate")) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            holder.linearLayout.findViewById<ImageView>(R.id.achievement_summary_badge).colorFilter = ColorMatrixColorFilter(matrix)
            holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_date).text = ""
        } else {
            holder.linearLayout.findViewById<ImageView>(R.id.achievement_summary_badge).clearColorFilter()
            holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_date).text = fragment.getString(R.string.date_earned, datesEarned[position])
        }
        holder.linearLayout.findViewById<TextView>(R.id.achievement_summary_stats).text = fragment.getString(R.string.won_by,
                numsAwarded[position],
                numsAwardedHC[position],
                numDistinctCasual.toInt(),
                DecimalFormat("@@@@")
                        .format(numsAwarded[position].toDouble() / numDistinctCasual * 100.0))

        // Double-layered Progress Bar
        val progressBar = holder.linearLayout.findViewById<ProgressBar>(R.id.achievement_summary_progress)
        progressBar.progress = (numsAwardedHC[position].toDouble() / numDistinctCasual * 10000.0).toInt()
        progressBar.secondaryProgress = (numsAwarded[position].toDouble() / numDistinctCasual * 10000.0).toInt()
    }

    override fun getItemCount(): Int {
        return ids.size
    }

    fun addAchievement(
            index: Int,
            id: String,
            badge: String,
            title: String,
            point: String,
            trueRatio: String,
            description: String,
            dateEarned: String,
            earnedHardcore: Boolean,
            numAwarded: String,
            numAwardedHC: String,
            author: String,
            dateCreated: String,
            dateModified: String) {
        ids.add(index, id)
        badges.add(index, badge)
        titles.add(index, title)
        points.add(index, point)
        trueRatios.add(index, trueRatio)
        descriptions.add(index, description)
        datesEarned.add(index, dateEarned)
        hardcoreEarnings.add(index, earnedHardcore)
        numsAwarded.add(index, numAwarded)
        numsAwardedHC.add(index, numAwardedHC)
        authors.add(index, author)
        datesCreated.add(index, dateCreated)
        datesModified.add(index, dateModified)
        notifyItemInserted(index)
    }

    fun setNumDistinctCasual(n: Double) {
        numDistinctCasual = n
    }

    fun clear() {
        ids.clear()
        badges.clear()
        titles.clear()
        points.clear()
        trueRatios.clear()
        descriptions.clear()
        hardcoreEarnings.clear()
        datesEarned.clear()
        numsAwarded.clear()
        numsAwardedHC.clear()
        authors.clear()
        datesCreated.clear()
        datesModified.clear()
    }

    /* Inner Classes and Interfaces */
    interface AchievementViewHolderListener {
        fun onItemClicked(view: View, adapterPosition: Int)
    }

    class AchievementViewHolderListenerImpl internal constructor(private val fragment: Fragment, private val adapter: AchievementAdapter) : AchievementViewHolderListener {
        override fun onItemClicked(view: View, adapterPosition: Int) { // Create a new transition
            val transitionSet = TransitionSet()
            transitionSet.ordering = TransitionSet.ORDERING_TOGETHER
            // Get the adapter position of the first child
            val firstChildIndex =
                    (fragment as AchievementSummaryFragment).layoutManager
                            .getChildAt(0)
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
                slide.addTarget(fragment.layoutManager.getChildAt(i))
                if (i + firstChildIndex < adapterPosition) {
                    slide.slideEdge = Gravity.TOP
                    transitionSet.addTransition(slide)
                } else if (i + firstChildIndex > adapterPosition) {
                    slide.slideEdge = Gravity.BOTTOM
                    transitionSet.addTransition(slide)
                }
            }
            fragment.setExitTransition(transitionSet)
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
                    .getActivity()
                    ?.supportFragmentManager
                    ?.beginTransaction()
                    ?.setReorderingAllowed(true)
                    ?.addSharedElement(transitionBadge, transitionBadge.transitionName)
                    ?.replace(R.id.game_details_frame, detailsFragment, AchievementDetailsFragment::class.java.simpleName)
                    ?.addToBackStack(null)
                    ?.commit()
        }

    }

    class AchievementViewHolder internal constructor(val linearLayout: LinearLayout, private val viewHolderListener: AchievementViewHolderListener) : RecyclerView.ViewHolder(linearLayout), View.OnClickListener {
        override fun onClick(view: View) {
            viewHolderListener.onItemClicked(view, adapterPosition)
        }

        init {
            linearLayout.setOnClickListener(this)
        }
    }

}