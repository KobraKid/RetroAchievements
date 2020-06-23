package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameCommentsAdapter extends RecyclerView.Adapter {

    private Context context;
    private Map<String, List<String>> comments;

    public GameCommentsAdapter(Context context, Map<String, List<String>> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GameCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_game_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Picasso.get()
                .load(Consts.BASE_URL
                        + "/" + Consts.USER_PIC_POSTFIX
                        + "/" + Objects.requireNonNull(comments.get("user")).get(position) + ".png")
                .placeholder(R.drawable.user_placeholder)
                .into(((ImageView) holder.itemView.findViewById(R.id.comments_user_icon)));
        ((TextView) holder.itemView.findViewById(R.id.comments_user_comment))
                .setText(Objects.requireNonNull(comments.get("text")).get(position));
        ((TextView) holder.itemView.findViewById(R.id.comments_user_name))
                .setText(Objects.requireNonNull(comments.get("user")).get(position));
        ((TextView) holder.itemView.findViewById(R.id.comments_date))
                .setText(Objects.requireNonNull(comments.get("date")).get(position));
        ((TextView) holder.itemView.findViewById(R.id.comments_user_rank_score))
                .setText(context.getString(R.string.score_rank, Objects.requireNonNull(comments.get("score")).get(position), Objects.requireNonNull(comments.get("rank")).get(position)));
        if (!Objects.requireNonNull(comments.get("tag")).get(position).equals("RA_NO_TAG_" + position)) {
            ((TextView) holder.itemView.findViewById(R.id.comments_user_tag))
                    .setText(context.getString(R.string.quote, Objects.requireNonNull(comments.get("tag")).get(position)));
            holder.itemView.findViewById(R.id.comments_user_tag).setVisibility(View.VISIBLE);
        } else {
            holder.itemView.findViewById(R.id.comments_user_tag).setVisibility(View.GONE);
        }
        TextView username = holder.itemView.findViewById(R.id.comments_user_name);
        switch (comments.get("acct").get(position)) {
            case "Banned":
                username.setTextColor(Color.RED);
                username.setPaintFlags(username.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                break;
            case "Developer":
                username.setTextColor(Color.GREEN);
                username.setPaintFlags(username.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                break;
            case "Registered":
                final TypedValue primaryColor = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true);
                username.setTextColor(primaryColor.data);
                username.setPaintFlags(username.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                break;
            default:
                username.setTextColor(Color.YELLOW);
                username.setPaintFlags(username.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return comments.get("text") != null ? Objects.requireNonNull(comments.get("text")).size() : 0;
    }

    static class GameCommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        GameCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            View extra = view.findViewById(R.id.comment_hide_content);
            if (extra != null) {
                extra.setVisibility(extra.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                ((TextView) view.findViewById(R.id.comments_user_comment)).setMaxLines(extra.getVisibility() == View.VISIBLE ? 10 : 3);
            }
        }
    }

}
