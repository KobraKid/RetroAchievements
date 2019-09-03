package com.kobrakid.retroachievements.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.RowSortedTable;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.fragment.LeaderboardsFragment;
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LeaderboardsAdapter extends RecyclerView.Adapter implements Filterable, RecyclerViewFastScroller.OnPopupTextUpdate {

    private final RowSortedTable<Integer, String, String> table, tableFiltered;
    private final LeaderboardsViewHolderListenerImpl listener;

    public LeaderboardsAdapter(Fragment fragment, RowSortedTable<Integer, String, String> table, RowSortedTable<Integer, String, String> tableFiltered) {
        this.table = table;
        this.tableFiltered = tableFiltered;
        this.listener = new LeaderboardsViewHolderListenerImpl(fragment, tableFiltered);
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
                String filter = charSequence.toString();
                String[] strings = new String[]{filter.substring(0, filter.indexOf("\t")), filter.substring(filter.indexOf("\t") + 1)};
                FilterResults results = new FilterResults();
                tableFiltered.clear();
                if (strings[0].equals("") && strings[1].equals("")) {
                    results.values = false;
                } else {
                    for (int i = 0; i < table.rowKeySet().size(); i++) {
                        if (Objects.requireNonNull(table.row(i).get("TITLE")).toLowerCase().contains(strings[1].toLowerCase())
                                && (strings[0].equals("") || Objects.requireNonNull(table.row(i).get("CONSOLE")).equals(strings[0]))) {
                            int row = tableFiltered.rowKeySet().size();
                            tableFiltered.put(row, "ID", Objects.requireNonNull(table.row(i).get("ID")));
                            tableFiltered.put(row, "IMAGE", Objects.requireNonNull(table.row(i).get("IMAGE")));
                            tableFiltered.put(row, "GAME", Objects.requireNonNull(table.row(i).get("GAME")));
                            tableFiltered.put(row, "CONSOLE", Objects.requireNonNull(table.row(i).get("CONSOLE")));
                            tableFiltered.put(row, "TITLE", Objects.requireNonNull(table.row(i).get("TITLE")));
                            tableFiltered.put(row, "DESCRIPTION", Objects.requireNonNull(table.row(i).get("DESCRIPTION")));
                            tableFiltered.put(row, "TYPE", Objects.requireNonNull(table.row(i).get("TYPE")));
                            tableFiltered.put(row, "NUMRESULTS", Objects.requireNonNull(table.row(i).get("NUMRESULTS")));
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

    @NotNull
    @Override
    public CharSequence onChange(int position) {
        return tableFiltered.get(position, "CONSOLE");
    }

    /* Inner Classes and Interfaces */

    private interface LeaderboardsViewHolderListener {
        void onItemClicked(@SuppressWarnings("unused") View view, int adapterPosition);
    }

    private static class LeaderboardsViewHolderListenerImpl implements LeaderboardsViewHolderListener {

        private final Fragment fragment;
        private final RowSortedTable<Integer, String, String> table;

        LeaderboardsViewHolderListenerImpl(Fragment fragment, RowSortedTable<Integer, String, String> table) {
            this.fragment = fragment;
            this.table = table;
        }

        @Override
        public void onItemClicked(View view, int adapterPosition) {
            if (fragment instanceof LeaderboardsFragment) {
                ((LeaderboardsFragment) fragment).onClick(table.row(adapterPosition));
            }
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
