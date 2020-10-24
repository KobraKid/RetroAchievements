package com.kobrakid.retroachievements.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.model.LeaderboardParticipant
import com.kobrakid.retroachievements.view.adapter.ParticipantsAdapter.ParticipantViewHolder
import com.squareup.picasso.Picasso

class ParticipantsAdapter(private val listener: View.OnClickListener, private val user: String?) : RecyclerView.Adapter<ParticipantViewHolder>() {

    private var participants: List<LeaderboardParticipant> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        return ParticipantViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.view_holder_participants, parent, false)
                        .apply { setOnClickListener(listener) })
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + participants[position].username + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.participant_icon))
        holder.itemView.findViewById<TextView>(R.id.participant_rank).text = (position + 1).toString()
        holder.itemView.findViewById<TextView>(R.id.participant_username).apply {
            text = participants[position].username
            isSelected = true
        }
        holder.itemView.findViewById<TextView>(R.id.participant_result).text = participants[position].result
        holder.itemView.findViewById<TextView>(R.id.participant_date).text = participants[position].date
        if (user == participants[position].username) holder.itemView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.border)
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    fun setParticipants(participants: List<LeaderboardParticipant>) {
        this.participants = participants
        notifyDataSetChanged()
    }

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}