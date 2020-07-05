package com.kobrakid.retroachievements.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.LeaderboardsAdapter.LeaderboardsViewHolder
import com.kobrakid.retroachievements.fragment.LeaderboardsFragmentDirections
import com.kobrakid.retroachievements.ra.Leaderboard
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller.OnPopupTextUpdate
import com.squareup.picasso.Picasso

class LeaderboardsAdapter(private val navController: NavController) : RecyclerView.Adapter<LeaderboardsViewHolder>(), Filterable, OnPopupTextUpdate {

    private val leaderboardMap = mutableMapOf<Int, Leaderboard>()
    private val leaderboardMapFiltered = mutableMapOf<Int, Leaderboard>()
    private val uniqueConsoles = mutableSetOf<String>()

    @Suppress("ConvertToStringTemplate")
    var consoleFilter = "console:"
        set(value) {
            field = "console:" + value
        }

    @Suppress("ConvertToStringTemplate")
    var titleFilter = "title:"
        set(value) {
            field = "title:" + value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardsViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_leaderboard, parent, false)
        return LeaderboardsViewHolder(layout)
    }

    override fun onBindViewHolder(holder: LeaderboardsViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            navController.navigate(
                    LeaderboardsFragmentDirections.actionLeaderboardsFragmentToLeaderboardFragment(
                            leaderboardMapFiltered[position]))
        }
        holder.itemView.findViewById<TextView>(R.id.id).text = leaderboardMapFiltered[position]?.id
        Picasso.get()
                .load(leaderboardMapFiltered[position]?.image)
                .placeholder(R.drawable.game_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.imageIcon))
        holder.itemView.findViewById<TextView>(R.id.game).text = leaderboardMapFiltered[position]?.game
        holder.itemView.findViewById<TextView>(R.id.console).text = holder.itemView.context.getString(R.string.console_parens, leaderboardMapFiltered[position]?.console
                ?: "")
        holder.itemView.findViewById<TextView>(R.id.title).text = leaderboardMapFiltered[position]?.title
        holder.itemView.findViewById<TextView>(R.id.description).text = leaderboardMapFiltered[position]?.description
        holder.itemView.findViewById<TextView>(R.id.type).text = leaderboardMapFiltered[position]?.type
        holder.itemView.findViewById<TextView>(R.id.numresults).text = leaderboardMapFiltered[position]?.numResults
    }

    override fun getItemCount(): Int {
        return leaderboardMapFiltered.size
    }

    override fun onChange(position: Int): CharSequence {
        return leaderboardMapFiltered[position]?.console ?: " "
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                if (charSequence != null) {
                    val constraint = charSequence.split(';')
                    val consoleFilter = constraint[CONSOLE].substring(8)
                    val titleFilter = constraint[TITLE].substring(6)
                    val res = mutableListOf<Int>()
                    leaderboardMap.forEach { (i, leaderboard) ->
                        if ((consoleFilter.isEmpty() || leaderboard.console == consoleFilter)
                                && (titleFilter.isEmpty() || leaderboard.title.contains(titleFilter)))
                            res.add(i)
                    }
                    results.values = res
                    results.count = res.size
                }
                return results
            }

            override fun publishResults(charSequerce: CharSequence?, results: FilterResults?) {
                if (results?.count != null) {
                    leaderboardMapFiltered.clear()
                    if (results.count > 0) {
                        var count = 0
                        (results.values as MutableList<*>).forEach {
                            val leaderboard = leaderboardMap[it as Int]
                            if (leaderboard != null) {
                                leaderboardMapFiltered[count] = leaderboard
                                count++
                            }
                        }
                    }
                    notifyDataSetChanged()
                }
            }

        }
    }

    fun addLeaderboard(index: Int, leaderboard: Leaderboard) {
        leaderboardMap[index] = leaderboard
        leaderboardMapFiltered[index] = leaderboard
        uniqueConsoles.add(leaderboard.console)
        notifyItemInserted(index)
    }

    fun getUniqueConsoles(): List<String> {
        return uniqueConsoles.toList()
    }

    fun buildFilter(): String {
        return "$consoleFilter;$titleFilter"
    }

    class LeaderboardsViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val CONSOLE = 0
        const val TITLE = 1
    }
}