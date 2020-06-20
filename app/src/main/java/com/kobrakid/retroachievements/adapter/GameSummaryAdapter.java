package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.R;
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class GameSummaryAdapter extends RecyclerView.Adapter<GameSummaryAdapter.GameSummaryViewHolder> implements RecyclerViewFastScroller.OnPopupTextUpdate, Filterable {

    private static final String TAG = GameSummaryAdapter.class.getSimpleName();

    private final Context context;

    private final ArrayList<String> ids = new ArrayList<>();
    private final ArrayList<String> imageIcons = new ArrayList<>();
    private final ArrayList<String> titles = new ArrayList<>();
    private final ArrayList<String> stats = new ArrayList<>();
    private final ArrayList<Boolean> masteries = new ArrayList<>();
    private final ArrayList<Boolean> loading = new ArrayList<>();

    private final List<Integer> mappings = new ArrayList<>();

    public GameSummaryAdapter(Context context) {
        this.context = context;
        refreshMappings();
    }

    @NonNull
    @Override
    public GameSummaryAdapter.GameSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.view_holder_game_summary,
                        parent,
                        false);
        return new GameSummaryViewHolder(constraintLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull GameSummaryViewHolder holder, int position) {
        int mappedPosition = mappings.get(position);
        if (mappedPosition >= imageIcons.size()) {
            Log.e(TAG, "Position too large: " + mappedPosition);
            return;
        }
        if (loading.get(mappedPosition)) {
            holder.constraintLayout.findViewById(R.id.game_summary_container)
                    .setVisibility(View.INVISIBLE);
            holder.constraintLayout.findViewById(R.id.separator)
                    .setVisibility(View.INVISIBLE);
            holder.constraintLayout.findViewById(R.id.game_summary_loading)
                    .setVisibility(View.VISIBLE);
        }
        Picasso.get()
                .load(Consts.BASE_URL + imageIcons.get(mappedPosition))
                .into(holder.constraintLayout.findViewById(R.id.game_summary_image_icon), new Callback() {
                    @Override
                    public void onSuccess() {
                        loading.set(mappedPosition, false);
                        holder.constraintLayout.findViewById(R.id.game_summary_loading)
                                .setVisibility(View.INVISIBLE);
                        holder.constraintLayout.findViewById(R.id.separator)
                                .setVisibility(View.VISIBLE);
                        holder.constraintLayout.findViewById(R.id.game_summary_container)
                                .setVisibility(View.VISIBLE);
                        if (masteries.size() != 0 && masteries.get(mappedPosition))
                            holder.constraintLayout.findViewById(R.id.game_summary_image_icon)
                                    .setBackground(context.getDrawable(R.drawable.image_view_border));
                        else
                            holder.constraintLayout.findViewById(R.id.game_summary_image_icon)
                                    .setBackground(null);
                        String title = Jsoup.parse(titles.get(mappedPosition).trim()).text();
                        // Fix titles with an appended ", The"
                        if (title.contains(", The"))
                            title = "The " + title.substring(0, title.indexOf(", The")) + title.substring(title.indexOf(", The") + 5);
                        ((TextView) holder.constraintLayout.findViewById(R.id.game_summary_title)).setText(title);
                        if (stats.size() == 0) {
                            holder.constraintLayout.findViewById(R.id.game_summary_stats).setVisibility(View.GONE);
                        } else {
                            ((TextView) holder.constraintLayout.findViewById(R.id.game_summary_stats))
                                    .setText(stats.get(mappedPosition));
                        }
                        ((TextView) holder.constraintLayout.findViewById(R.id.game_summary_game_id))
                                .setText(ids.get(mappedPosition));
                    }

                    @Override
                    public void onError(Exception e) {}
                });

    }

    @Override
    public int getItemCount() {
        return mappings.size();
    }

    /**
     * Computes the contents of the 'fast-scroll' bubble when games are shown alphabetically.
     *
     * @param position The position of the element whose info is needed.
     * @return The first character of the currently-scrolled-to game.
     */
    @NotNull
    @Override
    public CharSequence onChange(int position) {
        return titles.get(mappings.get(position)).substring(0, 1);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                if (charSequence.length() > 0) {
                    List<Integer> filterMappings = new ArrayList<>();
                    for (int i = 0; i < titles.size(); i++)
                        if (titles.get(i).toLowerCase().contains(charSequence.toString().toLowerCase()))
                            filterMappings.add(i);
                    results.count = filterMappings.size();
                    results.values = filterMappings;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.values instanceof List) {
                    mappings.clear();
                    mappings.addAll((List<Integer>) filterResults.values);
                } else {
                    refreshMappings();
                }
                notifyDataSetChanged();
            }
        };
    }

    public void refreshMappings() {
        mappings.clear();
        for (int i = 0; i < ids.size(); i++)
            mappings.add(i);
    }

    public void addGame(int index, String id, String imageIcon, String title, String stat, boolean mastered) {
        ids.add(index, id);
        imageIcons.add(index, imageIcon);
        titles.add(index, title);
        stats.add(index, stat);
        masteries.add(index, mastered);
        loading.add(index, true);
    }

    public void addGame(String id, String imageIcon, String title) {
        ids.add(id);
        imageIcons.add(imageIcon);
        titles.add(title);
        loading.add(false);
    }

    public int getNumGames() {
        return ids.size();
    }

    public void updateGameSummaries(int start, int length) {
        refreshMappings();
        if (start == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(start, length);
        }
    }

    public void clear() {
        ids.clear();
        imageIcons.clear();
        titles.clear();
        stats.clear();
        masteries.clear();
        loading.clear();
    }

    /* Inner Classes and Interfaces */

    static class GameSummaryViewHolder extends RecyclerView.ViewHolder {

        final ConstraintLayout constraintLayout;

        GameSummaryViewHolder(ConstraintLayout l) {
            super(l);
            constraintLayout = l;
        }

    }

}
