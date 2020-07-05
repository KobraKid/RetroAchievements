package com.kobrakid.retroachievements.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter.GameSummaryViewHolder
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller.OnPopupTextUpdate
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.*

class GameSummaryAdapter(private val listener: View.OnClickListener, private val masteredBorder: Drawable?) : RecyclerView.Adapter<GameSummaryViewHolder>(), OnPopupTextUpdate, Filterable {

    private val ids = mutableListOf<String>()
    private val imageIcons = mutableListOf<String>()
    private val titles = mutableListOf<String>()
    private val stats = mutableListOf<String>()
    private val masteries = mutableListOf<Boolean>()
    private val loading = mutableListOf<Boolean>()
    val numGames: Int
        get() = ids.size

    // For filtering
    private val mappings = mutableListOf<Int>()

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
        val mappedPosition = mappings[position]
        if (mappedPosition >= imageIcons.size) {
            Log.e(TAG, "Position too large: $mappedPosition")
            return
        }
        if (loading[mappedPosition]) {
            holder.constraintLayout.findViewById<View>(R.id.game_summary_container).visibility = View.INVISIBLE
            holder.constraintLayout.findViewById<View>(R.id.separator).visibility = View.INVISIBLE
            holder.constraintLayout.findViewById<View>(R.id.game_summary_loading).visibility = View.VISIBLE
        }
        Picasso.get()
                .load(Consts.BASE_URL + imageIcons[mappedPosition])
                .placeholder(R.drawable.game_placeholder)
                .into(holder.constraintLayout.findViewById(R.id.game_summary_image_icon), object : Callback {
                    override fun onSuccess() {
                        loading[mappedPosition] = false
                        holder.constraintLayout.findViewById<View>(R.id.game_summary_loading).visibility = View.INVISIBLE
                        holder.constraintLayout.findViewById<View>(R.id.separator).visibility = View.VISIBLE
                        holder.constraintLayout.findViewById<View>(R.id.game_summary_container).visibility = View.VISIBLE
                        if (masteries.size != 0 && masteries[mappedPosition])
                            holder.constraintLayout.findViewById<View>(R.id.game_summary_image_icon).background = masteredBorder
                        else
                            holder.constraintLayout.findViewById<View>(R.id.game_summary_image_icon).background = null
                        var title = Jsoup.parse(titles[mappedPosition].trim { it <= ' ' }).text()
                        // Fix titles with an appended ", The"
                        if (title.contains(", The"))
                            title = "The " + title.substring(0, title.indexOf(", The")) + title.substring(title.indexOf(", The") + 5)
                        holder.constraintLayout.findViewById<TextView>(R.id.game_summary_title).text = title
                        if (stats.size == 0) {
                            holder.constraintLayout.findViewById<View>(R.id.game_summary_stats).visibility = View.GONE
                        } else {
                            holder.constraintLayout.findViewById<TextView>(R.id.game_summary_stats).text = stats[mappedPosition]
                        }
                        holder.constraintLayout.findViewById<TextView>(R.id.game_summary_game_id).text = ids[mappedPosition]
                    }

                    override fun onError(e: Exception) {}
                })
    }

    override fun getItemCount(): Int {
        return mappings.size
    }

    /**
     * Computes the contents of the 'fast-scroll' bubble when games are shown alphabetically.
     *
     * @param position The position of the element whose info is needed.
     * @return The first character of the currently-scrolled-to game.
     */
    override fun onChange(position: Int): CharSequence {
        return titles[mappings[position]].substring(0, 1)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val results = FilterResults()
                if (charSequence.isNotEmpty()) {
                    val filterMappings = mutableListOf<Int>()
                    for (i in titles.indices)
                        if (titles[i].toLowerCase(Locale.ROOT).contains(charSequence.toString().toLowerCase(Locale.ROOT)))
                            filterMappings.add(i)
                    results.count = filterMappings.size
                    results.values = filterMappings
                }
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if (filterResults.values is List<*> && (filterResults.values as List<*>).isNotEmpty()) {
                    mappings.clear()
                    mappings.addAll((filterResults.values as List<*>).filterIsInstance<Int>())
                } else {
                    refreshMappings()
                }
                notifyDataSetChanged()
            }
        }
    }

    suspend fun addGame(index: Int, id: String, imageIcon: String, title: String, stat: String, mastered: Boolean) {
        withContext(Main) {
            ids.add(index, id)
            imageIcons.add(index, imageIcon)
            titles.add(index, title)
            stats.add(index, stat)
            masteries.add(index, mastered)
            loading.add(index, true)
            refreshMappings()
            notifyItemInserted(index)
        }
    }

    suspend fun addGame(id: String, imageIcon: String, title: String) {
        withContext(Main) {
            ids.add(id)
            imageIcons.add(imageIcon)
            titles.add(title)
            loading.add(false)
            refreshMappings()
            notifyItemInserted(mappings.size - 1)
        }
    }

    fun refreshMappings() {
        mappings.clear()
        for (i in ids.indices) mappings.add(i)
    }

    suspend fun clear() {
        withContext(Main) {
            ids.clear()
            imageIcons.clear()
            titles.clear()
            stats.clear()
            masteries.clear()
            loading.clear()
            mappings.clear()
            notifyDataSetChanged()
        }
    }

    /* Inner Classes and Interfaces */
    class GameSummaryViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)

    companion object {
        private val TAG = GameSummaryAdapter::class.java.simpleName
    }
}