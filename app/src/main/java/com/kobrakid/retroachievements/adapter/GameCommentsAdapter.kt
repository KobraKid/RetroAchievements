package com.kobrakid.retroachievements.adapter

import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class GameCommentsAdapter : RecyclerView.Adapter<GameCommentsAdapter.GameCommentViewHolder>() {

    private val comments = mutableListOf<String>()

    // Users' profile pictures
    private val users = mutableListOf<String>()

    // Users' account statuses
    private val accounts = mutableListOf<String>()
    private val scores = mutableListOf<String>()
    private val ranks = mutableListOf<String>()

    // Users' taglines
    private val tags = mutableListOf<String>()
    private val dates = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCommentViewHolder {
        return GameCommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_game_comment, parent, false))
    }

    override fun onBindViewHolder(holder: GameCommentViewHolder, position: Int) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + users[position] + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.comments_user_icon))
        holder.itemView.findViewById<TextView>(R.id.comments_user_comment).text = comments[position]
        holder.itemView.findViewById<TextView>(R.id.comments_date).text = dates[position]
        holder.itemView.findViewById<TextView>(R.id.comments_user_rank_score).text = holder.itemView.context.getString(R.string.score_rank, scores[position], ranks[position])
        with(holder.itemView.findViewById<TextView>(R.id.comments_user_tag)) {
            when (tags[position]) {
                "_RA_NO_TAG" -> visibility = View.GONE
                else -> {
                    text = holder.itemView.context.getString(R.string.quote, tags[position])
                    visibility = View.VISIBLE
                }
            }
        }
        with(holder.itemView.findViewById<TextView>(R.id.comments_user_name)) {
            text = users[position]
            when (accounts[position]) {
                "Banned" -> {
                    setTextColor(Color.RED)
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                "Developer" -> {
                    setTextColor(Color.GREEN)
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                "Registered" -> {
                    val primaryColor = TypedValue()
                    holder.itemView.context.theme.resolveAttribute(R.attr.colorPrimary, primaryColor, true)
                    setTextColor(primaryColor.data)
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                else -> {
                    setTextColor(Color.YELLOW)
                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    suspend fun addComment(text: String, user: String, account: String, score: String, rank: String, tag: String, date: String) {
        withContext(Main) {
            comments.add(text)
            users.add(user)
            accounts.add(account)
            scores.add(score)
            ranks.add(rank)
            tags.add(tag)
            dates.add(date)
            notifyItemInserted(comments.size - 1)
        }
    }

    class GameCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(view: View) {
            val extra = view.findViewById<View>(R.id.comment_hide_content)
            if (extra != null) {
                extra.visibility = if (extra.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                view.findViewById<TextView>(R.id.comments_user_comment).maxLines = if (extra.visibility == View.VISIBLE) 10 else 3
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

}