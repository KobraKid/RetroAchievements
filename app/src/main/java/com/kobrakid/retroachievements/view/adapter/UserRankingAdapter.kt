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
import com.kobrakid.retroachievements.model.User
import com.kobrakid.retroachievements.view.adapter.UserRankingAdapter.UserRankingViewHolder
import com.squareup.picasso.Picasso

class UserRankingAdapter(private val listener: View.OnClickListener, private val user: String?) : RecyclerView.Adapter<UserRankingViewHolder>() {

    private val users = mutableListOf<User>()

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
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + users[position].username + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.participant_icon))
        holder.itemView.findViewById<TextView>(R.id.participant_rank).text = users[position].rank
        holder.itemView.findViewById<TextView>(R.id.participant_username).text = users[position].username
        holder.itemView.findViewById<TextView>(R.id.participant_date).text =
                Html.fromHtml(
                        holder.itemView.context.getString(R.string.score_ratio_format, users[position].totalPoints, users[position].retroRatio.toString()),
                        TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        if (user == users[position].username)
            holder.itemView.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.border)
        else
            holder.itemView.background = null
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun populateUsers(users: List<User>) {
        users.forEach { if (!this.users.contains(it)) this.users.add(it) }
        this.users.sortBy { it.rank.toInt() }
        notifyDataSetChanged()
    }

    class UserRankingViewHolder(view: View) : RecyclerView.ViewHolder(view)

}