package com.kobrakid.retroachievements.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.AppExecutors;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.ConsoleAdapter;
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter;
import com.kobrakid.retroachievements.database.Console;
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListsFragment extends Fragment implements RAAPICallback {

    private static final String TAG = ListsFragment.class.getSimpleName();

    public boolean isShowingGames = false;

    private RAAPIConnection apiConnection;
    private boolean isActive = false;
    private boolean hideEmptyConsoles, hideEmptyGames;

    private RecyclerView consoleListRecyclerView, gameListRecyclerView;
    private LinearLayoutManager consoleListLayoutManager, gameListLayoutManager;
    public ConsoleAdapter consoleAdapter;
    private GameSummaryAdapter gameAdapter;
    private ArrayList<String> consoleIDs, consoleNames, gameImageIcons, gameTitles, gameStats, gameIDs;
    private int scrollPosition = 0;

    public ListsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).setTitle("Consoles");

        apiConnection = ((MainActivity) getActivity()).apiConnection;
        hideEmptyConsoles = getActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getBoolean(getString(R.string.empty_console_hide_setting), false);
        hideEmptyGames = getActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getBoolean(getString(R.string.empty_game_hide_setting), false);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lists, container, false);

        // Initialize views
        consoleListRecyclerView = view.findViewById(R.id.list_console);
        gameListRecyclerView = view.findViewById(R.id.list_games);

        // Set up console list
        consoleIDs = new ArrayList<>();
        consoleNames = new ArrayList<>();
        consoleAdapter = new ConsoleAdapter(consoleIDs, consoleNames, this);
        consoleListRecyclerView.setAdapter(consoleAdapter);
        consoleListLayoutManager = new LinearLayoutManager(getContext());
        consoleListRecyclerView.setLayoutManager(consoleListLayoutManager);

        // Set up games list with empty values
        gameImageIcons = new ArrayList<>();
        gameTitles = new ArrayList<>();
        gameStats = new ArrayList<>();
        gameIDs = new ArrayList<>();
        gameAdapter = new GameSummaryAdapter(getContext(), gameImageIcons, gameTitles, gameStats, gameIDs);
        gameListRecyclerView.setAdapter(gameAdapter);
        gameListLayoutManager = new LinearLayoutManager(getContext());
        gameListRecyclerView.setLayoutManager(gameListLayoutManager);

        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);
        gameListRecyclerView.animate().setDuration(375).translationX(p.x).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationEnd(animation);
                gameListRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
        apiConnection.GetConsoleIDs(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_CONSOLE_IDS) {
            consoleIDs.clear();
            consoleNames.clear();
            consoleAdapter.notifyDataSetChanged();
            if (hideEmptyConsoles) {
                Objects.requireNonNull(getActivity()).findViewById(R.id.list_hiding_fade).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.list_hiding_progress).setVisibility(View.VISIBLE);
            }
            try {
                JSONArray reader = new JSONArray(response);
                // Loop once to add all consoles to view
                for (int i = 0; i < reader.length(); i++) {
                    // Get console information
                    JSONObject console = reader.getJSONObject(i);
                    final String id = console.getString("ID");

                    // Add console information
                    consoleNames.add(console.getString("Name"));
                    Collections.sort(consoleNames);
                    consoleIDs.add(consoleNames.indexOf(console.getString("Name")), id);
                    consoleAdapter.notifyItemInserted(consoleNames.indexOf(console.getString("Name")));
                }
                // Loop twice if we wish to hide empty consoles
                if (hideEmptyConsoles) {
                    for (int i = 0; i < reader.length(); i++) {
                        // Get console information
                        final JSONObject console = reader.getJSONObject(i);
                        final String id = console.getString("ID");
                        final String name = console.getString("Name");
                        final int pos = i, max = reader.length() - 1;

                        final RetroAchievementsDatabase db = RetroAchievementsDatabase.getInstance(getContext());
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            // Get current console
                            List<Console> current = db.consoleDao().getConsoleWithID(Integer.parseInt(id));
                            // If it exists and has 0 games
                            if (current.size() > 0 && current.get(0).getGameCount() == 0 && consoleNames.contains(name)) {
                                final int namePos = consoleNames.indexOf(name), idPos = consoleIDs.indexOf(id);
                                Log.d(TAG, "Removing " + consoleIDs.remove(idPos) + ": " + consoleNames.remove(namePos) + " @ " + namePos + " from view");
                                AppExecutors.getInstance().mainThread().execute(consoleAdapter::notifyDataSetChanged);
                            }
                            if (pos == max) {
                                AppExecutors.getInstance().mainThread().execute(() -> {
                                    Objects.requireNonNull(getActivity()).findViewById(R.id.list_hiding_fade).setVisibility(View.GONE);
                                    getActivity().findViewById(R.id.list_hiding_progress).setVisibility(View.GONE);
                                });
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_LIST) {
            try {
                JSONArray reader = new JSONArray(response);
                if (reader.length() > 0) {
                    Objects.requireNonNull(getActivity()).findViewById(R.id.list_no_games).setVisibility(View.GONE);
                    for (int i = 0; i < reader.length(); i++) {
                        JSONObject game = reader.getJSONObject(i);
                        gameTitles.add(game.getString("Title"));
                        gameIDs.add(game.getString("ID"));
                        gameImageIcons.add(game.getString("ImageIcon"));
                    }
                } else {
                    Objects.requireNonNull(getActivity()).findViewById(R.id.list_no_games).setVisibility(View.VISIBLE);
                }
                // Show Game List RecyclerView
                gameAdapter.notifyDataSetChanged();
                // TODO API spam causes OutOfMemoryError crash
                //  gameAdapter.removeEmptyGames();

                gameListRecyclerView.animate().setDuration(375).translationX(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationEnd(animation);
                        Objects.requireNonNull(getView()).findViewById(R.id.list_games_fast_scroller).setVisibility(View.VISIBLE);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_USER_PROGRESS) {
            try {
                JSONObject reader = new JSONObject(response);
                Log.d(TAG, reader.getJSONObject(reader.names().getString(0)).getString("NumPossibleAchievements"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed() {
        Objects.requireNonNull(getActivity()).setTitle("Consoles");

        TypedValue typedValue = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.drawable.ic_menu, typedValue, true)) {
            Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(typedValue.resourceId);
        } else {
            Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);
        gameListRecyclerView.animate().setDuration(375).translationX(p.x).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Objects.requireNonNull(getView()).findViewById(R.id.list_games_fast_scroller).setVisibility(View.GONE);
            }
        });

        consoleAdapter.isExpanded = false;
        consoleListRecyclerView.animate().setDuration(375).translationX(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                consoleListRecyclerView.setVisibility(View.VISIBLE);
                consoleListLayoutManager.scrollToPositionWithOffset(scrollPosition, 0);
            }
        });

        isShowingGames = false;
        getActivity().findViewById(R.id.list_no_games).setVisibility(View.GONE);
    }

    public void onConsoleSelected(int position, String console, String consoleName) {
        Objects.requireNonNull(getActivity()).setTitle(consoleName);

        // Hide Console List RecyclerView
        consoleAdapter.isExpanded = !consoleAdapter.isExpanded;
        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);
        consoleListRecyclerView.animate().setDuration(375).translationX(-p.x).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                consoleListRecyclerView.setVisibility(View.GONE);
            }
        });
        scrollPosition = position;

        // Set up Game List RecyclerView
        apiConnection.GetGameList(console, this);
        gameImageIcons.clear();
        gameTitles.clear();
        gameStats.clear();
        gameIDs.clear();
        gameAdapter.notifyDataSetChanged();

        TypedValue typedValue = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.drawable.ic_arrow_back, typedValue, true)) {
            Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(typedValue.resourceId);
        } else {
            Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        isShowingGames = true;
    }

}
