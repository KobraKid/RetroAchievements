package com.kobrakid.retroachievements.adapter

import android.content.res.Resources
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.AchievementAdapter.AchievementViewHolder
import com.kobrakid.retroachievements.fragment.AchievementDetailsFragment
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment
import com.kobrakid.retroachievements.ra.Achievement
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class AchievementAdapter(fragment: Fragment, private val resources: Resources) : RecyclerView.Adapter<AchievementViewHolder>() {

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
        return AchievementViewHolder(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_holder_achievement_summary,
                        parent,
                        false) as ConstraintLayout, viewHolderListener)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {

        // Hidden Text Views
        holder.layout.findViewById<TextView>(R.id.recycler_view_position).text = position.toString()
        holder.layout.findViewById<TextView>(R.id.achievement_summary_author).text = authors[position]
        holder.layout.findViewById<TextView>(R.id.achievement_summary_created).text = datesCreated[position]
        holder.layout.findViewById<TextView>(R.id.achievement_summary_modified).text = datesModified[position]

        // Badge
        with(holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge)) {
            background = if (hardcoreEarnings[position]) context.getDrawable(R.drawable.image_view_border) else null
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + badges[position] + ".png")
                    .placeholder(R.drawable.favicon)
                    .into(this)
        }
        holder.layout.findViewById<TextView>(R.id.achievement_summary_badge_id).text = badges[position]

        // Text descriptions
        holder.layout.findViewById<TextView>(R.id.achievement_summary_id).text = ids[position]
        holder.layout.findViewById<TextView>(R.id.achievement_summary_title).text = resources.getString(R.string.achievement_summary_title, titles[position], points[position], trueRatios[position])
        holder.layout.findViewById<TextView>(R.id.achievement_summary_desc).text = descriptions[position]
        if (datesEarned[position].startsWith("NoDate")) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge).colorFilter = ColorMatrixColorFilter(matrix)
            holder.layout.findViewById<TextView>(R.id.achievement_summary_date).text = ""
        } else {
            holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge).clearColorFilter()
            holder.layout.findViewById<TextView>(R.id.achievement_summary_date).text = resources.getString(R.string.date_earned_lower, datesEarned[position])
        }
        holder.layout.findViewById<TextView>(R.id.achievement_summary_stats).text = resources.getString(R.string.won_by,
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

    fun getAchievementWithId(id: String): Achievement {
        val i = ids.indexOf(id)
        if (i >= 0)
            return Achievement(
                    ids[i],
                    badges[i],
                    titles[i],
                    points[i],
                    trueRatios[i],
                    descriptions[i],
                    datesEarned[i],
                    hardcoreEarnings[i],
                    numsAwarded[i],
                    numsAwardedHC[i],
                    authors[i],
                    datesCreated[i],
                    datesModified[i],
                    numDistinctCasual)
        else
            return Achievement()
    }

    fun setNumDistinctCasual(n: Double) {
        numDistinctCasual = n
    }

    interface AchievementViewHolderListener {
        fun onItemClicked(view: View, adapterPosition: Int)
    }

    inner class AchievementViewHolderListenerImpl internal constructor(private val fragment: Fragment, private val adapter: AchievementAdapter) : AchievementViewHolderListener {
        override fun onItemClicked(view: View, adapterPosition: Int) {
            val transitionBadge = view.findViewById<ImageView>(R.id.achievement_summary_badge)
                    .apply { transitionName = "achievement_$adapterPosition" }
            val sharedTransition = TransitionInflater.from(fragment.context).inflateTransition(R.transition.change_image_transform)
            val transition = TransitionInflater.from(fragment.context).inflateTransition(android.R.transition.slide_bottom)
            fragment.apply {
                sharedElementReturnTransition = sharedTransition
                // FIXME: Has no effect
                exitTransition = transition
            }
            fragment.childFragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .addSharedElement(transitionBadge, transitionBadge.transitionName)
                    .replace(R.id.game_details_frame,
                            AchievementDetailsFragment().apply {
                                arguments = bundleOf(
                                        "achievement" to adapter.getAchievementWithId(adapter.ids[adapterPosition]),
                                        "transitionName" to transitionBadge.transitionName
                                )
                                sharedElementEnterTransition = sharedTransition
                                enterTransition = transition
                            },
                            AchievementDetailsFragment::class.java.simpleName)
                    .addToBackStack(AchievementSummaryFragment::class.java.simpleName)
                    .commit()
        }

    }

    inner class AchievementViewHolder internal constructor(val layout: ConstraintLayout, private val viewHolderListener: AchievementViewHolderListener) : RecyclerView.ViewHolder(layout), View.OnClickListener {
        override fun onClick(view: View) {
            viewHolderListener.onItemClicked(view, adapterPosition)
        }

        init {
            layout.setOnClickListener(this)
        }
    }

}