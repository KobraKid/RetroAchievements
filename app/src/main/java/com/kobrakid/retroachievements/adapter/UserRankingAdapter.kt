package com.kobrakid.retroachievements.adapter

import android.os.Build
import android.text.Html
import android.text.Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.MainActivity
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.adapter.UserRankingAdapter.UserRankingViewHolder
import com.squareup.picasso.Picasso

class UserRankingAdapter(private val userRankings: MutableList<String>, private val userNames: MutableList<String>, private val userScores: MutableList<String>, private val userRatios: MutableList<String>) : RecyclerView.Adapter<UserRankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRankingViewHolder {
        return UserRankingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_participants, parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: UserRankingViewHolder, position: Int) {
        holder.itemView.findViewById<View>(R.id.participant_result).visibility = View.INVISIBLE
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + userNames[position] + ".png")
                .into(holder.itemView.findViewById<ImageView>(R.id.participant_icon))
        holder.itemView.findViewById<TextView>(R.id.participant_rank).text = userRankings[position]
        holder.itemView.findViewById<TextView>(R.id.participant_username).text = userNames[position]
        holder.itemView.findViewById<TextView>(R.id.participant_date).text =
                Html.fromHtml(
                        holder.itemView.context.getString(R.string.score_ratio_format, userScores[position], userRatios[position]),
                        TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        if (userNames[position] == MainActivity.raUser)
            holder.itemView.background = holder.itemView.context.getDrawable(R.drawable.border)
        else
            holder.itemView.background = null
    }

    override fun getItemCount(): Int {
        return userRankings.size
    }

    class UserRankingViewHolder(view: View) : RecyclerView.ViewHolder(view)

}