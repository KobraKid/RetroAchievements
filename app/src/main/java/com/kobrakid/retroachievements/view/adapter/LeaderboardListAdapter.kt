package com.kobrakid.retroachievements.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.model.ILeaderboard
import com.kobrakid.retroachievements.model.Leaderboard
import com.kobrakid.retroachievements.view.adapter.LeaderboardListAdapter.LeaderboardListViewHolder
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller.OnPopupTextUpdate
import com.squareup.picasso.Picasso

class LeaderboardListAdapter(private val listener: View.OnClickListener) : RecyclerView.Adapter<LeaderboardListViewHolder>(), Filterable, OnPopupTextUpdate {

    private var leaderboards: List<ILeaderboard> = mutableListOf()
    private var leaderboardsFiltered: List<ILeaderboard> = mutableListOf()
    var consoleFilter = "console:"
        set(value) {
            field = if (value.isNotEmpty()) "console:$value" else "console:"
        }
    var titleFilter = "title:"
        set(value) {
            field = if (value.isNotEmpty()) "title:$value" else "title:"
        }
    val filterTemplate
        get() = "$consoleFilter;$titleFilter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardListViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_leaderboard, parent, false)
        layout.setOnClickListener(listener)
        return LeaderboardListViewHolder(layout)
    }

    override fun onBindViewHolder(holder: LeaderboardListViewHolder, position: Int) {
        holder.itemView.id = leaderboardsFiltered[position].id.toInt()
        Picasso.get()
                .load(Consts.BASE_URL + "/Images/" + leaderboardsFiltered[position].icon) // TODO use consts?
                .placeholder(R.drawable.game_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.imageIcon))
        holder.itemView.findViewById<TextView>(R.id.game).text = leaderboardsFiltered[position].gameId
        holder.itemView.findViewById<TextView>(R.id.console).text = holder.itemView.context.getString(R.string.console_parens, leaderboardsFiltered[position].console)
        holder.itemView.findViewById<TextView>(R.id.title).text = leaderboardsFiltered[position].title
        holder.itemView.findViewById<TextView>(R.id.numresults).text = leaderboardsFiltered[position].numResults
    }

    override fun getItemCount(): Int {
        return leaderboardsFiltered.size
    }

    override fun onChange(position: Int): CharSequence {
        return if (consoleFilter == "console:") leaderboardsFiltered[position].console
        else leaderboardsFiltered[position].gameId
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                if (charSequence != null) {
                    val constraint = charSequence.split(';')
                    val consoleFilter = constraint[CONSOLE].substring(8)
                    val titleFilter = constraint[TITLE].substring(6)
                    results.values = leaderboards.filter {
                        ((consoleFilter.isEmpty() || it.console == consoleFilter)
                                && (titleFilter.isEmpty() || it.title.contains(titleFilter)))
                    }.also { results.count = it.size }
                }
                return results
            }

            override fun publishResults(charSequerce: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    (leaderboardsFiltered as MutableList).clear()
                    if (results.count > 0 && results.values is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (leaderboardsFiltered as MutableList<Leaderboard>).addAll(results.values as List<Leaderboard>)
                    }
                    notifyDataSetChanged()
                }
            }

        }
    }

    fun setLeaderboards(leaderboards: List<ILeaderboard>) {
        this.leaderboards = leaderboards
        (this.leaderboardsFiltered as MutableList).clear()
        (this.leaderboardsFiltered as MutableList).addAll(leaderboards)
        notifyDataSetChanged()
    }

    fun getUniqueConsoles(): Set<String> {
        return leaderboardsFiltered.map { it.console }.toMutableList().apply { add(0, "") }.toMutableSet()
    }

    class LeaderboardListViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val CONSOLE = 0
        const val TITLE = 1
    }
}