package com.kobrakid.retroachievements;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GameSummaryAdapter extends RecyclerView.Adapter<GameSummaryAdapter.GameSummaryViewHolder> {

    private ArrayList<String> imageIcons, titles, stats, ids;

    public static class GameSummaryViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout linearLayout;

        public GameSummaryViewHolder(LinearLayout l) {
            super(l);
            linearLayout = l;
        }

    }

    public GameSummaryAdapter(ArrayList<String> imageIcons, ArrayList<String> titles, ArrayList<String> stats, ArrayList<String> ids) {
        this.imageIcons = imageIcons;
        this.titles = titles;
        this.stats = stats;
        this.ids = ids;
    }

    @NonNull
    @Override
    public GameSummaryAdapter.GameSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.game_summary,
                        parent,
                        false);
        GameSummaryViewHolder viewHolder = new GameSummaryViewHolder(linearLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GameSummaryViewHolder holder, int position) {
        if (position >= imageIcons.size()) {
            Log.e("retroachievements", "Position too big: " + position);
            return;
        }
        Picasso.get()
                .load("https://retroachievements.org" + imageIcons.get(position))
                .into(((ImageView) holder.linearLayout.findViewById(R.id.game_summary_image_icon)));
        ((TextView) holder.linearLayout.findViewById(R.id.game_summary_title))
                .setText(titles.get(position));
        ((TextView) holder.linearLayout.findViewById(R.id.game_summary_stats))
                .setText(stats.get(position));
        ((TextView) holder.linearLayout.findViewById(R.id.game_summary_game_id))
                .setText(ids.get(position));
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

}
