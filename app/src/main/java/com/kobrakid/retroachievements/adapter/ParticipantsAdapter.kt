package com.kobrakid.retroachievements.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.MainActivity
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.ParticipantsAdapter.ParticipantViewHolder
import com.squareup.picasso.Picasso

class ParticipantsAdapter(private val context: Context) : RecyclerView.Adapter<ParticipantViewHolder>() {

    // TODO: Make this more Kotlin-oriented (referenced @ LeaderboardActivity.kt)
    @JvmField
    val users = mutableListOf<String>()
    @JvmField
    val results = mutableListOf<String>()
    @JvmField
    val dates = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        return ParticipantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_participants, parent, false))
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + users[position] + ".png")
                .into(holder.itemView.findViewById<View>(R.id.participant_icon) as ImageView)
        (holder.itemView.findViewById<View>(R.id.participant_rank) as TextView).text = context.getString(R.string.participant, position + 1)
        (holder.itemView.findViewById<View>(R.id.participant_username) as TextView).text = users[position]
        (holder.itemView.findViewById<View>(R.id.participant_result) as TextView).text = results[position]
        (holder.itemView.findViewById<View>(R.id.participant_date) as TextView).text = dates[position]
        if (MainActivity.raUser == users[position]) holder.itemView.background = context.getDrawable(R.drawable.border)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun addParticipant(user: String, result: String, date: String) {
        users.add(user)
        results.add(result)
        dates.add(date)
        notifyItemInserted(users.size - 1)
    }

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}