package com.kobrakid.retroachievements.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.view.adapter.ConsoleAdapter.ConsoleViewHolder
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.*

class ConsoleAdapter(private val listener: View.OnClickListener) : RecyclerView.Adapter<ConsoleViewHolder>() {

    private val consoleIDs = mutableListOf<String>()
    private val consoleNames = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        return ConsoleViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.view_holder_console_list, parent, false)
                        .apply { setOnClickListener(listener) })
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.console_id).text = consoleIDs[position]
        holder.itemView.findViewById<TextView>(R.id.console_initial).text = consoleNames[position].substring(0, 1)
        val random = Random()
        holder.itemView.findViewById<TextView>(R.id.console_initial)
                .setTextColor(Color.parseColor("#"
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)))
        holder.itemView.findViewById<TextView>(R.id.console_name).text = consoleNames[position]
    }

    override fun getItemCount(): Int {
        return consoleIDs.size
    }

    suspend fun addConsole(id: String, name: String) {
        withContext(Main) {
            consoleNames.add(name)
            consoleNames.sort()
            consoleIDs.add(consoleNames.indexOf(name), id)
            notifyItemInserted(consoleNames.indexOf(name))
        }
    }

    suspend fun removeConsole(name: String) {
        withContext(Main) {
            if (!consoleNames.contains(name)) return@withContext
            val rem = consoleNames.indexOf(name)
            consoleNames.removeAt(rem)
            consoleIDs.removeAt(rem)
            notifyItemRemoved(rem)
        }
    }

    class ConsoleViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)
}