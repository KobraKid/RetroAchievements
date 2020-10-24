package com.kobrakid.retroachievements.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.database.Game
import com.kobrakid.retroachievements.model.GameSummary
import com.kobrakid.retroachievements.view.adapter.GameSummaryAdapter.GameSummaryViewHolder
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller.OnPopupTextUpdate
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import java.util.*

class GameSummaryAdapter(private val listener: View.OnClickListener, private val context: Context?) : RecyclerView.Adapter<GameSummaryViewHolder>(), OnPopupTextUpdate, Filterable {

    private var games: List<GameSummary> = mutableListOf()
    private var gamesFiltered: List<GameSummary> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameSummaryViewHolder {
        val layout = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.view_holder_game_summary,
                        parent,
                        false) as ConstraintLayout
        layout.setOnClickListener(listener)
        return GameSummaryViewHolder(layout)
    }

    override fun onBindViewHolder(holder: GameSummaryViewHolder, position: Int) {
        holder.itemView.id = gamesFiltered[position].id.toInt()
        holder.itemView.findViewById<View>(R.id.game_summary_image_icon).background =
                if (gamesFiltered[position].totalAchievements > 0 && gamesFiltered[position].numAchievementsEarned == gamesFiltered[position].totalAchievements)
                    context?.let { ContextCompat.getDrawable(it, R.drawable.image_view_border) }
                else null
        holder.itemView.findViewById<TextView>(R.id.game_summary_title).text = Jsoup.parse(gamesFiltered[position].title.trim { it <= ' ' }).text().let { title ->
            if (title.contains(", The"))
                "The " + title.indexOf(", The").let {
                    title.substring(0, it) + title.substring(it + 5)
                }
            else title
        }
        holder.itemView.findViewById<TextView>(R.id.game_summary_stats).apply {
            if (gamesFiltered[position].totalAchievements > 0) {
                visibility = View.VISIBLE
                text = context?.getString(R.string.game_stats,
                        gamesFiltered[position].numAchievementsEarned,
                        gamesFiltered[position].totalAchievements,
                        gamesFiltered[position].earnedPoints,
                        gamesFiltered[position].totalPoints)
            } else visibility = View.GONE
        }
        Picasso.get()
                .load(Consts.BASE_URL + gamesFiltered[position].imageIcon)
                .placeholder(R.drawable.game_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.game_summary_image_icon))
    }

    override fun getItemCount(): Int {
        return gamesFiltered.size
    }

    /**
     * Computes the contents of the 'fast-scroll' bubble when games are shown alphabetically.
     *
     * @param position The position of the element whose info is needed.
     * @return The first character of the currently-scrolled-to game.
     */
    override fun onChange(position: Int): CharSequence {
        return gamesFiltered[position].title.substring(0, 1)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val results = FilterResults()
                if (charSequence.isNotEmpty()) {
                    results.values = games.filter {
                        it.title.toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT))
                    }.also { results.count = it.size }
                }
                return results
            }

            override fun publishResults(charSequence: CharSequence, results: FilterResults?) {
                if (results != null) {
                    (gamesFiltered as MutableList).clear()
                    if (results.count > 0 && results.values is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (gamesFiltered as MutableList<GameSummary>).addAll(results.values as List<GameSummary>)
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    /**
     * For populating the list of games from a network request
     *
     * @param games The games to show the user
     */
    fun setGameSummaries(games: List<GameSummary>) {
        this.games = games
        (this.gamesFiltered as MutableList).clear()
        (this.gamesFiltered as MutableList).addAll(games)
        notifyDataSetChanged()
    }

    /**
     * For populating the list of games from the database
     *
     * @param games The games to show the user
     */
    fun setGames(games: List<Game?>) {
        setGameSummaries(games.map { game ->
            GameSummary().apply {
                game?.id?.let { id = it }
                game?.title?.let { title = it }
                game?.imageIcon?.let { imageIcon = it }
                game?.numDistinctPlayersCasual?.let { if (it.isNotEmpty() && it.isDigitsOnly()) numDistinctCasual = it.toInt() }
                game?.numAwardedToUser?.let { numAchievementsEarned = it }
                game?.numAwardedToUserHardcore?.let { numAchievementsEarnedHC = it }
                game?.numAchievements?.let { totalAchievements = it }
            }
        })
    }

    inner class GameSummaryViewHolder(constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)
}