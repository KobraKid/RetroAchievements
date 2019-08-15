package com.kobrakid.retroachievements.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GameSummaryAdapter extends RecyclerView.Adapter<GameSummaryAdapter.GameSummaryViewHolder> {

    private final ArrayList<String> imageIcons;
    private final ArrayList<String> titles;
    private final ArrayList<String> stats;
    private final ArrayList<String> ids;

    public static class GameSummaryViewHolder extends RecyclerView.ViewHolder {

        final LinearLayout linearLayout;

        GameSummaryViewHolder(LinearLayout l) {
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
                .inflate(R.layout.view_holder_game_summary,
                        parent,
                        false);
        return new GameSummaryViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull GameSummaryViewHolder holder, int position) {
        if (position >= imageIcons.size()) {
            Log.e("retroachievements", "Position too big: " + position);
            return;
        }
        Picasso.get()
                .load(Consts.BASE_URL + imageIcons.get(position))
                .into(((ImageView) holder.linearLayout.findViewById(R.id.game_summary_image_icon)));
        ((TextView) holder.linearLayout.findViewById(R.id.game_summary_title))
                .setText(titles.get(position).trim());
        if (stats.size() == 0) {
            holder.linearLayout.findViewById(R.id.game_summary_stats).setVisibility(View.GONE);
        } else {
            ((TextView) holder.linearLayout.findViewById(R.id.game_summary_stats))
                    .setText(stats.get(position));
        }
        ((TextView) holder.linearLayout.findViewById(R.id.game_summary_game_id))
                .setText(ids.get(position));
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

}
