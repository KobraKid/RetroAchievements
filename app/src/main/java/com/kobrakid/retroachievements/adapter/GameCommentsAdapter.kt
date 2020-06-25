package com.kobrakid.retroachievements.adapter

import android.content.Context
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

class GameCommentsAdapter(private val context: Context, private val comments: Map<String, List<String>>) : RecyclerView.Adapter<GameCommentsAdapter.GameCommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCommentViewHolder {
        return GameCommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_game_comment, parent, false))
    }

    override fun onBindViewHolder(holder: GameCommentViewHolder, position: Int) {
        Picasso.get()
                .load(Consts.BASE_URL
                        + "/" + Consts.USER_PIC_POSTFIX
                        + "/" + comments["user"]?.get(position) + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.comments_user_icon))
        holder.itemView.findViewById<TextView>(R.id.comments_user_comment).text = comments["text"]?.get(position)
        holder.itemView.findViewById<TextView>(R.id.comments_user_name).text = comments["user"]?.get(position)
        holder.itemView.findViewById<TextView>(R.id.comments_date).text = comments["date"]?.get(position)
        holder.itemView.findViewById<TextView>(R.id.comments_user_rank_score).text = context.getString(R.string.score_rank, comments["score"]?.get(position), comments["rank"]?.get(position))
        if (comments["tag"]?.get(position) != "RA_NO_TAG_$position") {
            holder.itemView.findViewById<TextView>(R.id.comments_user_tag).text = context.getString(R.string.quote, comments["tag"]?.get(position))
            holder.itemView.findViewById<View>(R.id.comments_user_tag).visibility = View.VISIBLE
        } else {
            holder.itemView.findViewById<View>(R.id.comments_user_tag).visibility = View.GONE
        }
        val username = holder.itemView.findViewById<TextView>(R.id.comments_user_name)
        when (comments["acct"]?.get(position)) {
            "Banned" -> {
                username.setTextColor(Color.RED)
                username.paintFlags = username.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            "Developer" -> {
                username.setTextColor(Color.GREEN)
                username.paintFlags = username.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            "Registered" -> {
                val primaryColor = TypedValue()
                context.theme.resolveAttribute(R.attr.colorPrimary, primaryColor, true)
                username.setTextColor(primaryColor.data)
                username.paintFlags = username.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            else -> {
                username.setTextColor(Color.YELLOW)
                username.paintFlags = username.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun getItemCount(): Int {
        return comments["text"]?.size ?: 0
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