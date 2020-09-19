package com.kobrakid.retroachievements.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.activity.MainActivity
import com.kobrakid.retroachievements.adapter.ParticipantsAdapter.ParticipantViewHolder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class ParticipantsAdapter(private val listener: View.OnClickListener) : RecyclerView.Adapter<ParticipantViewHolder>() {

    private val users = ArrayList<String>()
    private val results = ArrayList<String>()
    private val dates = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        return ParticipantViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.view_holder_participants, parent, false)
                        .apply { setOnClickListener(listener) })
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + users[position] + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.participant_icon))
        holder.itemView.findViewById<TextView>(R.id.participant_rank).text = (position + 1).toString()
        holder.itemView.findViewById<TextView>(R.id.participant_username).apply {
            text = users[position]
            isSelected = true
        }
        holder.itemView.findViewById<TextView>(R.id.participant_result).text = results[position]
        holder.itemView.findViewById<TextView>(R.id.participant_date).text = dates[position]
        if (MainActivity.raUser == users[position]) holder.itemView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.border)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    suspend fun addParticipant(user: String, result: String, date: String) {
        withContext(Main) {
            users.add(user)
            results.add(result)
            dates.add(date)
            notifyItemInserted(users.size - 1)
        }
    }

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}