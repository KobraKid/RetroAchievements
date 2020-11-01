package com.kobrakid.retroachievements.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.model.IConsole
import com.kobrakid.retroachievements.view.adapter.ConsoleAdapter.ConsoleViewHolder
import java.util.*

class ConsoleAdapter(private val listener: View.OnClickListener) : RecyclerView.Adapter<ConsoleViewHolder>() {

    private val consoles = mutableListOf<IConsole>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        return ConsoleViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.view_holder_console_list, parent, false)
                        .apply { setOnClickListener(listener) })
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.console_id).text = consoles[position].id
        holder.itemView.findViewById<TextView>(R.id.console_initial).text = consoles[position].consoleName.substring(0, 1)
        val random = Random()
        holder.itemView.findViewById<TextView>(R.id.console_initial)
                .setTextColor(Color.parseColor("#"
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)))
        holder.itemView.findViewById<TextView>(R.id.console_name).text = consoles[position].consoleName
    }

    override fun getItemCount(): Int {
        return consoles.size
    }

    fun setData(data: List<IConsole>) {
        consoles.clear()
        data.forEach { console ->
            consoles.add(console)
        }
        consoles.sortBy { it.consoleName }
        notifyDataSetChanged()
    }

    class ConsoleViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)
}