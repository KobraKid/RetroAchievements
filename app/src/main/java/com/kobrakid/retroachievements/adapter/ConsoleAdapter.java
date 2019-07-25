package com.kobrakid.retroachievements.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.fragment.ListsFragment;

import java.util.ArrayList;
import java.util.Random;

public class ConsoleAdapter extends RecyclerView.Adapter {

    private ArrayList<String> consoleIDs, consoleNames;
    private final ConsoleViewHolderListenerImpl viewHolderListener;
    private final RecyclerView gameList;
    public boolean isExpanded = false;

    public ConsoleAdapter(ArrayList<String> consoleIDs, ArrayList<String> consoleNames, RecyclerView gameList, Fragment fragment) {
        this.consoleIDs = consoleIDs;
        this.consoleNames = consoleNames;
        this.viewHolderListener = new ConsoleViewHolderListenerImpl(fragment, this);
        this.gameList = gameList;
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

    /****************************
     Inner Classes and Interfaces
     ****************************/

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
            // Hide this RecyclerView
            adapter.isExpanded = !adapter.isExpanded;
            Point p = new Point();
            fragment.getActivity().getWindowManager().getDefaultDisplay().getSize(p);
            ((ListsFragment) fragment).consoleListRecyclerView.animate().setDuration(375).translationX(-p.x).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((ListsFragment) fragment).consoleListRecyclerView.setVisibility(View.GONE);
                }
            });
            ((ListsFragment) fragment).scrollPosition = adapterPosition;

            // TODO Set up next RecyclerView
            adapter.gameList.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
            // adapter.gameList.setAdapter();
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
