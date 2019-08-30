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
import java.util.Random;

public class ConsoleAdapter extends RecyclerView.Adapter {

    public boolean isExpanded = false;

    private final ArrayList<String> consoleIDs, consoleNames;
    private final ConsoleViewHolderListenerImpl viewHolderListener;

    public ConsoleAdapter(ArrayList<String> consoleIDs, ArrayList<String> consoleNames, Fragment fragment) {
        this.consoleIDs = consoleIDs;
        this.consoleNames = consoleNames;
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
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

    /* Inner Classes and Interfaces */

    private interface ConsoleViewHolderListener {
        void onItemClicked(View view, int adapterPosition);
    }

    private static class ConsoleViewHolderListenerImpl implements ConsoleViewHolderListener {

        private final Fragment fragment;
        private final ConsoleAdapter adapter;

        ConsoleViewHolderListenerImpl(Fragment fragment, ConsoleAdapter adapter) {
            this.fragment = fragment;
            this.adapter = adapter;
        }

        @Override
        public void onItemClicked(View view, int adapterPosition) {
            ((ListsFragment) fragment).onConsoleSelected(adapterPosition, adapter.consoleIDs.get(adapterPosition), adapter.consoleNames.get(adapterPosition));
        }
    }

    public class ConsoleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ConsoleViewHolderListener viewHolderListener;

        ConsoleViewHolder(View itemView, ConsoleViewHolderListenerImpl viewHolderListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.viewHolderListener = viewHolderListener;
        }

        @Override
        public void onClick(View view) {
            viewHolderListener.onItemClicked(view, getAdapterPosition());
        }
    }

}
