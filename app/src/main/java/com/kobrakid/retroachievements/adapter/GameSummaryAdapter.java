package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.AppExecutors;
import com.kobrakid.retroachievements.Consts;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.database.Game;
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase;
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class GameSummaryAdapter extends RecyclerView.Adapter<GameSummaryAdapter.GameSummaryViewHolder> implements RecyclerViewFastScroller.OnPopupTextUpdate, Filterable {

    private static final String TAG = GameSummaryAdapter.class.getSimpleName();

    private final ArrayList<String> imageIcons;
    private final ArrayList<String> titles;
    private final ArrayList<String> stats;
    private final ArrayList<String> ids;
    private final Context context;
    private List<Integer> mappings = new ArrayList<>();

    public GameSummaryAdapter(Context context, ArrayList<String> imageIcons, ArrayList<String> titles, ArrayList<String> stats, ArrayList<String> ids) {
        this.context = context;
        this.imageIcons = imageIcons;
        this.titles = titles;
        this.stats = stats;
        this.ids = ids;
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
            Log.e(TAG, "Position too big: " + mappedPosition);
            return;
        } else if (imageIcons.get(mappedPosition).equals("__loading")) {
            holder.constraintLayout.findViewById(R.id.game_summary_container).setVisibility(View.INVISIBLE);
            holder.constraintLayout.findViewById(R.id.separator).setVisibility(View.INVISIBLE);
            holder.constraintLayout.findViewById(R.id.game_summary_loading).setVisibility(View.VISIBLE);
            return;
        } else {
            holder.constraintLayout.findViewById(R.id.game_summary_loading).setVisibility(View.INVISIBLE);
            holder.constraintLayout.findViewById(R.id.separator).setVisibility(View.VISIBLE);
            holder.constraintLayout.findViewById(R.id.game_summary_container).setVisibility(View.VISIBLE);
        }
        Picasso.get()
                .load(Consts.BASE_URL + imageIcons.get(mappedPosition))
                .placeholder(R.drawable.game_placeholder)
                .into(((ImageView) holder.constraintLayout.findViewById(R.id.game_summary_image_icon)));
        String title = Jsoup.parse(titles.get(mappedPosition).trim()).text();
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
    public int getItemCount() {
        return mappings.size();
    }

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
        for (int i = 0; i < titles.size(); i++)
            mappings.add(i);
    }

    public void removeEmptyGames() {
        final String lastID = ids.get(ids.size() - 1);
        for (int i = 0; i < ids.size(); i++) {
            final String imageIcon = imageIcons.get(i), title = titles.get(i), id = ids.get(i);
            final String stat = (stats != null && stats.size() > 0) ? stats.get(i) : null;
            final RetroAchievementsDatabase db = RetroAchievementsDatabase.getInstance(context);
            final GameSummaryAdapter adapter = this;
            AppExecutors.getInstance().diskIO().execute(() -> {
                List<Game> game = db.gameDao().getGameWithID(Integer.parseInt(id));
                Log.d(TAG, game.toString());
                if (game.size() == 1) {
                    if (game.get(0).getAchievementCount() == 0) {
                        imageIcons.remove(imageIcon);
                        titles.remove(title);
                        ids.remove(id);
                        if (stat != null)
                            stats.remove(stat);
                    }
                } else {
                    AppExecutors.getInstance().mainThread().execute(() -> new RAAPIConnection(context).GetUserProgress(
                            MainActivity.ra_user,
                            id,
                            (responseCode, response) -> {
                                if (responseCode == RAAPIConnection.RESPONSE_GET_USER_PROGRESS) {
                                    try {
                                        JSONObject reader = new JSONObject(response);
                                        final int numAchievements = Integer.parseInt(reader.getJSONObject(id).getString("NumPossibleAchievements"));
                                        AppExecutors.getInstance().diskIO().execute(() -> {
                                            Game game1 = new Game(Integer.parseInt(id), title, numAchievements);
                                            Log.d(TAG, "inserting " + game1);
                                            db.gameDao().insertGame(game1);
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    ));
                }
                if (id.equals(lastID)) {
                    AppExecutors.getInstance().mainThread().execute(adapter::notifyDataSetChanged);
                }
            });
        }
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
