package com.kobrakid.retroachievements.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.activity.MainActivity
import com.kobrakid.retroachievements.adapter.ParticipantsAdapter.ParticipantViewHolder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class ParticipantsAdapter : RecyclerView.Adapter<ParticipantViewHolder>() {

    private val users = ArrayList<String>()
    private val results = ArrayList<String>()
    private val dates = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        return ParticipantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_participants, parent, false))
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + users[position] + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.participant_icon))
        holder.itemView.findViewById<TextView>(R.id.participant_rank).text = (position + 1).toString()
        holder.itemView.findViewById<TextView>(R.id.participant_username).text = users[position]
        holder.itemView.findViewById<TextView>(R.id.participant_result).text = results[position]
        holder.itemView.findViewById<TextView>(R.id.participant_date).text = dates[position]
        if (MainActivity.raUser == users[position]) holder.itemView.background = holder.itemView.context.getDrawable(R.drawable.border)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun saveUsersInstanceState(): ArrayList<String> {
        return users
    }

    fun saveResultsInstanceState(): ArrayList<String> {
        return results
    }

    fun saveDatesInstanceState(): ArrayList<String> {
        return dates
    }

    suspend fun addParticipant(user: String, result: String, date: String) {
        users.add(user)
        results.add(result)
        dates.add(date)
        withContext(Main) {
            notifyItemInserted(users.size - 1)
        }
    }

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}