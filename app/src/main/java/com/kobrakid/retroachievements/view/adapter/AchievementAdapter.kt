package com.kobrakid.retroachievements.view.adapter

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.model.Achievement
import com.kobrakid.retroachievements.model.IAchievement
import com.kobrakid.retroachievements.view.adapter.AchievementAdapter.AchievementViewHolder
import com.kobrakid.retroachievements.view.ui.AchievementDetailsFragment
import com.kobrakid.retroachievements.view.ui.AchievementSummaryFragment
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class AchievementAdapter(private val fragment: Fragment) : RecyclerView.Adapter<AchievementViewHolder>() {

    private var numDistinctCasual = 1.0
    private var achievements: List<IAchievement> = mutableListOf()
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
        holder.layout.findViewById<TextView>(R.id.achievement_summary_author).text = achievements[position].author
        holder.layout.findViewById<TextView>(R.id.achievement_summary_created).text = achievements[position].dateCreated
        holder.layout.findViewById<TextView>(R.id.achievement_summary_modified).text = achievements[position].dateModified

        // Badge
        with(holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge)) {
            background = if (achievements[position].dateEarnedHardcore.isNotEmpty()) ContextCompat.getDrawable(context, R.drawable.image_view_border) else null
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + Consts.GAME_BADGE_POSTFIX + "/" + achievements[position].badgeName + ".png")
                    .placeholder(R.drawable.favicon)
                    .into(this)
        }
        holder.layout.findViewById<TextView>(R.id.achievement_summary_badge_id).text = achievements[position].badgeName

        // Text descriptions
        holder.layout.findViewById<TextView>(R.id.achievement_summary_id).text = achievements[position].achievementID
        holder.layout.findViewById<TextView>(R.id.achievement_summary_title).text = fragment.getString(R.string.achievement_summary_title, achievements[position].title, achievements[position].points, achievements[position].truePoints)
        holder.layout.findViewById<TextView>(R.id.achievement_summary_desc).text = achievements[position].description
        if (achievements[position].dateEarned.isEmpty()) {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge).colorFilter = ColorMatrixColorFilter(matrix)
            holder.layout.findViewById<TextView>(R.id.achievement_summary_date).text = ""
        } else {
            holder.layout.findViewById<ImageView>(R.id.achievement_summary_badge).clearColorFilter()
            holder.layout.findViewById<TextView>(R.id.achievement_summary_date).text = fragment.getString(R.string.date_earned_lower, achievements[position].dateEarned)
        }
        holder.layout.findViewById<TextView>(R.id.achievement_summary_stats).text = fragment.getString(R.string.won_by,
                achievements[position].numAwarded,
                achievements[position].numAwardedHardcore,
                numDistinctCasual.toInt(),
                DecimalFormat("@@@@")
                        .format(achievements[position].numAwarded.toDouble() / numDistinctCasual * 100.0))

        // Double-layered Progress Bar
        holder.layout.findViewById<ProgressBar>(R.id.achievement_summary_progress).apply {
            progress = (achievements[position].numAwardedHardcore.toDouble() / numDistinctCasual * 10000.0).toInt()
            secondaryProgress = (achievements[position].numAwarded.toDouble() / numDistinctCasual * 10000.0).toInt()
        }
    }

    override fun getItemCount(): Int {
        return achievements.size
    }

    fun getAchievementWithId(id: String): IAchievement {
        // IDs are unique, so the list should only have one element
        return achievements.filter { it.achievementID == id }.let { if (it.isEmpty()) Achievement() else it[0] }
    }

    /**
     * Set the number of distinct casual players that have played this game.
     *
     * @param n The number of players.
     */
    fun setNumDistinctCasual(n: Int) {
        numDistinctCasual = n.toDouble()
    }

    fun setAchievements(achievements: List<IAchievement>) {
        this.achievements = achievements
        notifyDataSetChanged()
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
                                        "achievement" to adapter.getAchievementWithId(adapter.achievements[adapterPosition].achievementID),
                                        "numDistinctCasual" to adapter.numDistinctCasual,
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