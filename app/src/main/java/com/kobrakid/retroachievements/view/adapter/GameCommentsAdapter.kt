package com.kobrakid.retroachievements.view.adapter

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
import com.kobrakid.retroachievements.model.Comment
import com.squareup.picasso.Picasso

class GameCommentsAdapter : RecyclerView.Adapter<GameCommentsAdapter.GameCommentViewHolder>() {

    private var comments: List<Comment> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameCommentViewHolder {
        return GameCommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_game_comment, parent, false))
    }

    override fun onBindViewHolder(holder: GameCommentViewHolder, position: Int) {
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + comments[position].username + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(holder.itemView.findViewById<ImageView>(R.id.comments_user_icon))
        holder.itemView.findViewById<TextView>(R.id.comments_user_comment).text = comments[position].text
        holder.itemView.findViewById<TextView>(R.id.comments_date).text = comments[position].date
        holder.itemView.findViewById<TextView>(R.id.comments_user_rank_score).text = holder.itemView.context.getString(R.string.score_rank, comments[position].score, comments[position].rank)
        with(holder.itemView.findViewById<TextView>(R.id.comments_user_tag)) {
            comments[position].tagline.let { tagline ->
                text = if (tagline.isEmpty()) "" else holder.itemView.context.getString(R.string.quote, tagline)
                visibility = if (tagline.isEmpty()) View.GONE else View.VISIBLE
            }
        }
        with(holder.itemView.findViewById<TextView>(R.id.comments_user_name)) {
            text = comments[position].username
            when (comments[position].accountStatus) {
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

    fun setComments(comments: List<Comment>) {
        this.comments = comments
        notifyDataSetChanged()
    }

    class GameCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(view: View) {
            view.findViewById<View>(R.id.comment_hide_content)?.apply {
                // Toggle visibility
                visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
                // Increase comment max size when visible
                view.findViewById<TextView>(R.id.comments_user_comment).maxLines = if (visibility == View.VISIBLE) 10 else 3
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

}