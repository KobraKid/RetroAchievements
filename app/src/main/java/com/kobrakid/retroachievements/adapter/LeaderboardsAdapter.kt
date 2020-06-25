package com.kobrakid.retroachievements.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.RowSortedTable
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.LeaderboardsAdapter.LeaderboardsViewHolder
import com.kobrakid.retroachievements.fragment.LeaderboardsFragment
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller.OnPopupTextUpdate
import com.squareup.picasso.Picasso
import java.util.*

class LeaderboardsAdapter(fragment: Fragment, private val table: RowSortedTable<Int, String, String>, private val tableFiltered: RowSortedTable<Int, String, String>) : RecyclerView.Adapter<LeaderboardsViewHolder>(), Filterable, OnPopupTextUpdate {

    private val listener = LeaderboardsViewHolderListenerImpl(fragment, tableFiltered)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardsViewHolder {
        return LeaderboardsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_leaderboard, parent, false), listener)
    }

    override fun onBindViewHolder(holder: LeaderboardsViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.id).text = tableFiltered.row(position)["ID"]
        Picasso.get()
                .load(tableFiltered.row(position)["IMAGE"])
                .into(holder.itemView.findViewById<ImageView>(R.id.imageIcon))
        holder.itemView.findViewById<TextView>(R.id.game).text = tableFiltered.row(position)["GAME"]
        if (tableFiltered.row(position)["CONSOLE"] == "")
            holder.itemView.findViewById<TextView>(R.id.console).text = null
        else
            holder.itemView.findViewById<TextView>(R.id.console).text = holder.itemView.context.getString(R.string.console_parens, tableFiltered.row(position)["CONSOLE"])
        holder.itemView.findViewById<TextView>(R.id.title).text = tableFiltered.row(position)["TITLE"]
        holder.itemView.findViewById<TextView>(R.id.description).text = tableFiltered.row(position)["DESCRIPTION"]
        holder.itemView.findViewById<TextView>(R.id.type).text = tableFiltered.row(position)["TYPE"]
        holder.itemView.findViewById<TextView>(R.id.numresults).text = tableFiltered.row(position)["NUMRESULTS"]
    }

    override fun getItemCount(): Int {
        return tableFiltered.rowKeySet().size
    }

    // TODO: Find more elegant solution than to split string by tabs
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val filter = charSequence.toString()
                val strings = arrayOf(filter.substring(0, filter.indexOf("\t")), filter.substring(filter.indexOf("\t") + 1))
                val results = FilterResults()
                tableFiltered.clear()
                if (strings[0].isEmpty() && strings[1].isEmpty()) {
                    results.values = false
                } else {
                    for (i in table.rowKeySet().indices) {
                        if (table.row(i)["TITLE"]!!.toLowerCase(Locale.ROOT).contains(strings[1].toLowerCase(Locale.ROOT))
                                && (strings[0].isEmpty() || table.row(i)["CONSOLE"] == strings[0])) {
                            val row = tableFiltered.rowKeySet().size
                            tableFiltered.put(row, "ID", table.row(i)["ID"]!!)
                            tableFiltered.put(row, "IMAGE", table.row(i)["IMAGE"]!!)
                            tableFiltered.put(row, "GAME", (table.row(i)["GAME"])!!)
                            tableFiltered.put(row, "CONSOLE", table.row(i)["CONSOLE"]!!)
                            tableFiltered.put(row, "TITLE", table.row(i)["TITLE"]!!)
                            tableFiltered.put(row, "DESCRIPTION", table.row(i)["DESCRIPTION"]!!)
                            tableFiltered.put(row, "TYPE", table.row(i)["TYPE"]!!)
                            tableFiltered.put(row, "NUMRESULTS", table.row(i)["NUMRESULTS"]!!)
                        }
                    }
                    results.values = true
                }
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if (!(filterResults.values is Boolean && filterResults.values as Boolean)) {
                    // FIXME Can be concurrently modified, clashing with {@Link LeaderboardsFragment.java} line 272
                    tableFiltered.putAll(table)
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun onChange(position: Int): CharSequence {
        return tableFiltered[position, "CONSOLE"]
    }

    /* Inner Classes and Interfaces */
    private interface LeaderboardsViewHolderListener {
        fun onItemClicked(view: View?, adapterPosition: Int)
    }

    class LeaderboardsViewHolderListenerImpl internal constructor(private val fragment: Fragment, private val table: RowSortedTable<Int, String, String>) : LeaderboardsViewHolderListener {
        override fun onItemClicked(view: View?, adapterPosition: Int) {
            if (fragment is LeaderboardsFragment) {
                fragment.onClick(table.row(adapterPosition))
            }
        }

    }

    class LeaderboardsViewHolder internal constructor(itemView: View, listener: LeaderboardsViewHolderListenerImpl) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val listener: LeaderboardsViewHolderListenerImpl
        override fun onClick(view: View) {
            listener.onItemClicked(view, adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
            this.listener = listener
        }
    }
}