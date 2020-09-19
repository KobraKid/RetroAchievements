package com.kobrakid.retroachievements.view.adapter

import android.os.Build
import android.text.Html
import android.text.Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.view.adapter.UserRankingAdapter.UserRankingViewHolder
import com.kobrakid.retroachievements.view.ui.MainActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class UserRankingAdapter(private val listener: View.OnClickListener) : RecyclerView.Adapter<UserRankingViewHolder>() {

    private val userRankings = mutableListOf<String>()
    private val userNames = mutableListOf<String>()
    private val userScores = mutableListOf<String>()
    private val userRatios = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRankingViewHolder {
        return UserRankingViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.view_holder_participants, parent, false)
                        .apply { setOnClickListener(listener) })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: UserRankingViewHolder, position: Int) {
        holder.itemView.findViewById<View>(R.id.participant_result).visibility = View.INVISIBLE
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + userNames[position] + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.participant_icon))
        holder.itemView.findViewById<TextView>(R.id.participant_rank).text = userRankings[position]
        holder.itemView.findViewById<TextView>(R.id.participant_username).text = userNames[position]
        holder.itemView.findViewById<TextView>(R.id.participant_date).text =
                Html.fromHtml(
                        holder.itemView.context.getString(R.string.score_ratio_format, userScores[position], userRatios[position]),
                        TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        if (userNames[position] == MainActivity.raUser)
            holder.itemView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.border)
        else
            holder.itemView.background = null
    }

    override fun getItemCount(): Int {
        return userRankings.size
    }

    suspend fun addUser(rank: String, name: String, score: String, ratio: String) {
        if (userRankings.contains(rank)) return
        userRankings.add(rank)
        userNames.add(name)
        userScores.add(score)
        userRatios.add(ratio)
        withContext(Main) {
            notifyDataSetChanged()
        }
    }

    class UserRankingViewHolder(view: View) : RecyclerView.ViewHolder(view)

}