package com.kobrakid.retroachievements.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserRankingAdapter extends RecyclerView.Adapter {

    private final ArrayList<String> userRankings;
    private final ArrayList<String> userNames;
    private final ArrayList<String> userScores;
    private final ArrayList<String> userRatios;

    public UserRankingAdapter(ArrayList<String> userRankings, ArrayList<String> userNames, ArrayList<String> userScores, ArrayList<String> userRatios) {
        this.userRankings = userRankings;
        this.userNames = userNames;
        this.userScores = userScores;
        this.userRatios = userRatios;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserRankingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_participants, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.itemView.findViewById(R.id.participant_result).setVisibility(View.INVISIBLE);
        Picasso.get()
                .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + userNames.get(position) + ".png")
                .into((ImageView) holder.itemView.findViewById(R.id.participant_icon));
        ((TextView) holder.itemView.findViewById(R.id.participant_rank))
                .setText(String.valueOf(userRankings.get(position)));
        ((TextView) holder.itemView.findViewById(R.id.participant_username))
                .setText(userNames.get(position));
        ((TextView) holder.itemView.findViewById(R.id.participant_date))
                .setText(Html.fromHtml(holder.itemView.getContext().getString(R.string.score_ratio_format, userScores.get(position), userRatios.get(position))));
        if (userNames.get(position).equals(MainActivity.ra_user))
            holder.itemView.setBackground(holder.itemView.getContext().getDrawable(R.drawable.border));
        else
            holder.itemView.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return userRankings.size();
    }

    private class UserRankingViewHolder extends RecyclerView.ViewHolder {
        UserRankingViewHolder(View view) {
            super(view);
        }
    }
}
