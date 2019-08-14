package com.kobrakid.retroachievements.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.RowSortedTable;
import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

public class LeaderboardsAdapter extends RecyclerView.Adapter {

    private RowSortedTable<Integer, String, String> table;

    public LeaderboardsAdapter(RowSortedTable<Integer, String, String> table) {
        this.table = table;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LeaderboardsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_leaderboard, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TextView) holder.itemView.findViewById(R.id.id)).setText(table.row(position).get("ID"));
        Picasso.get().load(table.row(position).get("IMAGE")).into((ImageView) holder.itemView.findViewById(R.id.game));
        ((TextView) holder.itemView.findViewById(R.id.console)).setText(table.row(position).get("CONSOLE"));
        ((TextView) holder.itemView.findViewById(R.id.title)).setText(table.row(position).get("TITLE"));
        ((TextView) holder.itemView.findViewById(R.id.description)).setText(table.row(position).get("DESCRIPTION"));
        ((TextView) holder.itemView.findViewById(R.id.type)).setText(table.row(position).get("TYPE"));
        ((TextView) holder.itemView.findViewById(R.id.numresults)).setText(table.row(position).get("NUMRESULTS"));
    }

    @Override
    public int getItemCount() {
        return table.size();
    }

    class LeaderboardsViewHolder extends RecyclerView.ViewHolder {

        LeaderboardsViewHolder(View itemView) {
            super(itemView);
        }
    }

}
