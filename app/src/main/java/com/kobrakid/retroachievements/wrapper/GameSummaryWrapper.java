package com.kobrakid.retroachievements.wrapper;

import android.content.Context;

import com.kobrakid.retroachievements.adapter.GameSummaryAdapter;
import com.kobrakid.retroachievements.database.Game;

import java.util.ArrayList;

public class GameSummaryWrapper {

    /* The wrapped class */
    private GameSummaryAdapter adapter;
    /* The backend arrays that describe each game summary */
    private ArrayList<String> imageIcons;
    private ArrayList<String> titles;
    private ArrayList<String> stats;
    @SuppressWarnings("SpellCheckingInspection")
    private ArrayList<Boolean> masteries;
    private ArrayList<String> ids;
    private boolean[] isReady = {false};

    public GameSummaryWrapper(Context context) {
        imageIcons = new ArrayList<>();
        titles = new ArrayList<>();
        stats = new ArrayList<>();
        masteries = new ArrayList<>();
        ids = new ArrayList<>();
        adapter = new GameSummaryAdapter(context, imageIcons, titles, stats, masteries, ids);
    }

    public void addGame(int index, String id, String imageIcon, String title, String stat, boolean mastered) {
        ids.add(index, id);
        imageIcons.add(index, imageIcon);
        titles.add(index, title);
        stats.add(index, stat);
        masteries.add(index, mastered);
    }

    public void addGame(String id, String imageIcon, String title) {
        ids.add(id);
        imageIcons.add(imageIcon);
        titles.add(title);
    }

    public int getNumGames() {
        return ids.size();
    }

    public void updateGameSummaries(int start, int length) {
        isReady[0] = true;
        adapter.refreshMappings();
        if (start == 0) {
            adapter.notifyDataSetChanged();
        } else {
            adapter.notifyItemRangeInserted(start, length);
        }
    }

    public void clear() {
        imageIcons.clear();
        titles.clear();
        stats.clear();
        masteries.clear();
        ids.clear();
        isReady[0] = false;
    }

    public void filter(String constraint) {
        adapter.getFilter().filter(constraint);
    }

    public GameSummaryAdapter getAdapter() {
        return adapter;
    }

}
