package com.kobrakid.retroachievements.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.RowSortedTable;
import com.kobrakid.retroachievements.R;
import com.squareup.picasso.Picasso;

public class LeaderboardsAdapter extends RecyclerView.Adapter implements Filterable {

    private final RowSortedTable<Integer, String, String> table, tableFiltered;
    private final LeaderboardsViewHolderListenerImpl listener;

    public LeaderboardsAdapter(RowSortedTable<Integer, String, String> table, RowSortedTable<Integer, String, String> tableFiltered) {
        this.table = table;
        this.tableFiltered = tableFiltered;
        this.listener = new LeaderboardsViewHolderListenerImpl();
    }

    @NonNull
    @Override
    public LeaderboardsAdapter.LeaderboardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LeaderboardsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_leaderboard, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TextView) holder.itemView.findViewById(R.id.id)).setText(tableFiltered.row(position).get("ID"));
        Picasso.get().load(tableFiltered.row(position).get("IMAGE")).into((ImageView) holder.itemView.findViewById(R.id.game));
        ((TextView) holder.itemView.findViewById(R.id.console)).setText(tableFiltered.row(position).get("CONSOLE"));
        ((TextView) holder.itemView.findViewById(R.id.title)).setText(tableFiltered.row(position).get("TITLE"));
        ((TextView) holder.itemView.findViewById(R.id.description)).setText(tableFiltered.row(position).get("DESCRIPTION"));
        ((TextView) holder.itemView.findViewById(R.id.type)).setText(tableFiltered.row(position).get("TYPE"));
        ((TextView) holder.itemView.findViewById(R.id.numresults)).setText(tableFiltered.row(position).get("NUMRESULTS"));
    }

    @Override
    public int getItemCount() {
        return tableFiltered.rowKeySet().size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String[] strings = charSequence.toString().split("\t");
                FilterResults results = new FilterResults();
                if (strings.length == 0 || strings.length == 1 && strings[0].equals("")) {
                    results.values = false;
                } else {
                    tableFiltered.clear();
                    for (int i = 0; i < table.rowKeySet().size(); i++) {
                        if (table.row(i).get("TITLE").contains(strings[1]) && (strings[0].equals("") || table.row(i).get("CONSOLE").equals(strings[0]))) {
                            int row = tableFiltered.rowKeySet().size();
                            tableFiltered.put(row, "ID", table.row(i).get("ID"));
                            tableFiltered.put(row, "IMAGE", table.row(i).get("IMAGE"));
                            tableFiltered.put(row, "GAME", table.row(i).get("GAME"));
                            tableFiltered.put(row, "CONSOLE", table.row(i).get("CONSOLE"));
                            tableFiltered.put(row, "TITLE", table.row(i).get("TITLE"));
                            tableFiltered.put(row, "DESCRIPTION", table.row(i).get("DESCRIPTION"));
                            tableFiltered.put(row, "TYPE", table.row(i).get("TYPE"));
                            tableFiltered.put(row, "NUMRESULTS", table.row(i).get("NUMRESULTS"));
                        }
                    }
                    results.values = true;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (!(filterResults.values instanceof Boolean && (Boolean) filterResults.values)) {
                    tableFiltered.putAll(table);
                }
                notifyDataSetChanged();
            }
        };
    }

    /* Inner Classes and Interfaces */

    private interface LeaderboardsViewHolderListener {
        void onItemClicked(View view, int adapterPosition);
    }

    private static class LeaderboardsViewHolderListenerImpl implements LeaderboardsViewHolderListener {
        @Override
        public void onItemClicked(View view, int adapterPosition) {
            // TODO What should happen on click?
            Log.i("TAG", "" + adapterPosition);
        }
    }

    class LeaderboardsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final LeaderboardsViewHolderListenerImpl listener;

        LeaderboardsViewHolder(View itemView, LeaderboardsViewHolderListenerImpl listener) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.listener = listener;
        }

        @Override
        public void onClick(View view) {
            listener.onItemClicked(view, getAdapterPosition());
        }
    }

}
