package com.kobrakid.retroachievements.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.ConsoleAdapter.ConsoleViewHolder
import com.kobrakid.retroachievements.fragment.ListsFragment
import java.util.*

class ConsoleAdapter(fragment: Fragment) : RecyclerView.Adapter<ConsoleViewHolder>() {
    private val consoleIDs: MutableList<String> = ArrayList()
    private val consoleNames: MutableList<String> = ArrayList()
    private val viewHolderListener: ConsoleViewHolderListenerImpl
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsoleViewHolder {
        return ConsoleViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.view_holder_console_list, parent, false),
                viewHolderListener)
    }

    override fun onBindViewHolder(holder: ConsoleViewHolder, position: Int) {
        (holder.itemView.findViewById<View>(R.id.console_id) as TextView).text = consoleIDs[position]
        (holder.itemView.findViewById<View>(R.id.console_initial) as TextView).text = consoleNames[position].substring(0, 1)
        val random = Random()
        (holder.itemView.findViewById<View>(R.id.console_initial) as TextView)
                .setTextColor(Color.parseColor("#"
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)))
        (holder.itemView.findViewById<View>(R.id.console_name) as TextView).text = consoleNames[position]
    }

    override fun getItemCount(): Int {
        return consoleIDs.size
    }

    fun addConsole(id: String, name: String) {
        consoleNames.add(name)
        consoleNames.sort()
        consoleIDs.add(consoleNames.indexOf(name), id)
        notifyItemInserted(consoleNames.indexOf(name))
    }

    fun removeConsole(name: String) {
        if (!consoleNames.contains(name)) return
        val rem = consoleNames.indexOf(name)
        consoleNames.removeAt(rem)
        consoleIDs.removeAt(rem)
        notifyItemRemoved(rem)
    }

    fun clear() {
        consoleIDs.clear()
        consoleNames.clear()
        notifyDataSetChanged()
    }

    /* Inner Classes and Interfaces */
    private interface ConsoleViewHolderListener {
        fun onItemClicked(adapterPosition: Int)
    }

    class ConsoleViewHolderListenerImpl internal constructor(private val fragment: Fragment, private val adapter: ConsoleAdapter) : ConsoleViewHolderListener {
        override fun onItemClicked(adapterPosition: Int) {
            (fragment as ListsFragment).onConsoleSelected(adapterPosition, adapter.consoleIDs[adapterPosition], adapter.consoleNames[adapterPosition])
        }

    }

    class ConsoleViewHolder internal constructor(itemView: View, viewHolderListener: ConsoleViewHolderListenerImpl) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val viewHolderListener: ConsoleViewHolderListener
        override fun onClick(view: View) {
            viewHolderListener.onItemClicked(adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
            this.viewHolderListener = viewHolderListener
        }
    }

    init {
        viewHolderListener = ConsoleViewHolderListenerImpl(fragment, this)
    }
}