package com.kobrakid.retroachievements.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.fragment.ListsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ConsoleAdapter extends RecyclerView.Adapter<ConsoleAdapter.ConsoleViewHolder> {

    private final List<String> consoleIDs = new ArrayList<>();
    private final List<String> consoleNames = new ArrayList<>();
    private final ConsoleViewHolderListenerImpl viewHolderListener;

    public ConsoleAdapter(Fragment fragment) {
        this.viewHolderListener = new ConsoleViewHolderListenerImpl(fragment, this);
    }

    @NonNull
    @Override
    public ConsoleAdapter.ConsoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConsoleViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_console_list, parent, false),
                viewHolderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsoleViewHolder holder, int position) {
        ((TextView) holder.itemView.findViewById(R.id.console_id)).setText(consoleIDs.get(position));
        ((TextView) holder.itemView.findViewById(R.id.console_initial)).setText(consoleNames.get(position).substring(0, 1));
        Random random = new Random();
        ((TextView) holder.itemView.findViewById(R.id.console_initial))
                .setTextColor(Color.parseColor("#"
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)
                        + String.format("#%02X", random.nextInt(255)).substring(1)));
        ((TextView) holder.itemView.findViewById(R.id.console_name)).setText(consoleNames.get(position));
    }

    @Override
    public int getItemCount() {
        return consoleIDs.size();
    }

    public void addConsole(String id, String name) {
        consoleNames.add(name);
        Collections.sort(consoleNames);
        consoleIDs.add(consoleNames.indexOf(name), id);
        notifyItemInserted(consoleNames.indexOf(name));
    }

    public void removeConsole(String name) {
        if (!consoleNames.contains(name)) return;
        int rem = consoleNames.indexOf(name);
        consoleNames.remove(rem);
        consoleIDs.remove(rem);
        notifyItemRemoved(rem);
    }

    public void clear() {
        consoleIDs.clear();
        consoleNames.clear();
        notifyDataSetChanged();
    }

    /* Inner Classes and Interfaces */

    private interface ConsoleViewHolderListener {
        void onItemClicked(int adapterPosition);
    }

    private static class ConsoleViewHolderListenerImpl implements ConsoleViewHolderListener {

        private final Fragment fragment;
        private final ConsoleAdapter adapter;

        ConsoleViewHolderListenerImpl(Fragment fragment, ConsoleAdapter adapter) {
            this.fragment = fragment;
            this.adapter = adapter;
        }

        @Override
        public void onItemClicked(int adapterPosition) {
            ((ListsFragment) fragment).onConsoleSelected(adapterPosition, adapter.consoleIDs.get(adapterPosition), adapter.consoleNames.get(adapterPosition));
        }
    }

    static class ConsoleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ConsoleViewHolderListener viewHolderListener;

        ConsoleViewHolder(View itemView, ConsoleViewHolderListenerImpl viewHolderListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.viewHolderListener = viewHolderListener;
        }

        @Override
        public void onClick(View view) {
            viewHolderListener.onItemClicked(getAdapterPosition());
        }
    }

}
