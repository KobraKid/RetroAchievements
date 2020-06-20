package com.kobrakid.retroachievements.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

import java.util.List;
import java.util.Objects;

public class ListsFragment extends Fragment implements RAAPICallback {

    public boolean isShowingGames = false;

    private RAAPIConnection apiConnection;
    private boolean isActive = false;
    private boolean hideEmptyConsoles, hideEmptyGames;

    private RecyclerView consoleListRecyclerView;
    private LinearLayoutManager consoleListLayoutManager;
    private ConsoleAdapter consoleAdapter;
    private GameSummaryAdapter gameSummaryAdapter;
    private String consoleName = "";
    private int scrollPosition = 0;
    private Point p;

    public ListsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lists, container, false);

        if (savedInstanceState == null) {
            Objects.requireNonNull(getActivity()).setTitle("Consoles");

            apiConnection = ((MainActivity) getActivity()).apiConnection;
            hideEmptyConsoles = getActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getBoolean(getString(R.string.empty_console_hide_setting), false);
            hideEmptyGames = getActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getBoolean(getString(R.string.empty_game_hide_setting), false);

            consoleAdapter = new ConsoleAdapter(this);
            gameSummaryAdapter = new GameSummaryAdapter(getContext());
        }

        // Initialize views
        consoleListRecyclerView = view.findViewById(R.id.list_console);
        consoleListRecyclerView.setAdapter(consoleAdapter);
        consoleListLayoutManager = new LinearLayoutManager(getContext());
        consoleListRecyclerView.setLayoutManager(consoleListLayoutManager);

        RecyclerView gameListRecyclerView = view.findViewById(R.id.list_games);
        gameListRecyclerView.setAdapter(gameSummaryAdapter);
        gameListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        EditText gamesFilter = view.findViewById(R.id.list_games_filter);
        gamesFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                gameSummaryAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        p = new Point();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getSize(p);

        if (savedInstanceState == null) {
            if (hideEmptyConsoles) {
                view.findViewById(R.id.list_hiding_fade).setVisibility(View.VISIBLE);
                view.findViewById(R.id.list_hiding_progress).setVisibility(View.VISIBLE);
            }
            apiConnection.GetConsoleIDs(this);
        } else {
            if (isShowingGames) {
                populateGamesView(view, false);
            } else {
                populateConsolesView(view);
            }
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
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
            consoleAdapter.clear();
            try {
                JSONArray reader = new JSONArray(response);
                // Loop once to add all consoles to view
                for (int i = 0; i < reader.length(); i++) {
                    // Get console information
                    JSONObject console = reader.getJSONObject(i);
                    consoleAdapter.addConsole(console.getString("ID"), console.getString("Name"));
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
                            if (current.size() > 0 && current.get(0).getGameCount() == 0) {
                                AppExecutors.getInstance().mainThread().execute(() -> consoleAdapter.removeConsole(name));
                            }
                            if (pos == max) {
                                AppExecutors.getInstance().mainThread().execute(() -> {
                                    if (getView() != null) {
                                        populateConsolesView(getView());
                                    }
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
                    Objects.requireNonNull(getView()).findViewById(R.id.list_no_games).setVisibility(View.GONE);
                    for (int i = 0; i < reader.length(); i++) {
                        JSONObject game = reader.getJSONObject(i);
                        gameSummaryAdapter.addGame(
                                game.getString("ID"),
                                game.getString("ImageIcon"),
                                game.getString("Title")
                        );
                    }
                    gameSummaryAdapter.updateGameSummaries(0, 0);
                }
                if (getView() != null)
                    populateGamesView(getView(), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateConsolesView(View view) {
        view.findViewById(R.id.list_hiding_fade).setVisibility(View.GONE);
        view.findViewById(R.id.list_hiding_progress).setVisibility(View.GONE);
    }

    private void populateGamesView(View view, Boolean animate) {
        Objects.requireNonNull(getActivity()).setTitle(consoleName);
        Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        // Show Game List RecyclerView
        if (animate) {
            view.findViewById(R.id.list_games_fast_scroller)
                    .animate()
                    .setDuration(375)
                    .translationX(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            view.findViewById(R.id.list_games_fast_scroller).setVisibility(View.VISIBLE);
                        }
                    });
            view.findViewById(R.id.list_games_filter)
                    .animate()
                    .setDuration(375)
                    .translationX(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            view.findViewById(R.id.list_games_filter).setVisibility(View.VISIBLE);
                        }
                    });
        } else {

            consoleListRecyclerView
                    .animate()
                    .setDuration(0)
                    .translationX(-p.x)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            consoleListRecyclerView.setVisibility(View.GONE);
                        }
                    });
            view.findViewById(R.id.list_games_fast_scroller).setVisibility(View.VISIBLE);
            view.findViewById(R.id.list_games_filter).setVisibility(View.VISIBLE);
        }
        if (gameSummaryAdapter.getNumGames() == 0)
            Objects.requireNonNull(getView()).findViewById(R.id.list_no_games).setVisibility(View.VISIBLE);
    }

    public void onBackPressed(View view) {
        isShowingGames = false;
        Objects.requireNonNull(getActivity()).setTitle("Consoles");
        Objects.requireNonNull(((MainActivity) getActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_menu);

        view.findViewById(R.id.list_games_fast_scroller)
                .animate()
                .setDuration(375)
                .translationX(p.x)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.findViewById(R.id.list_games_fast_scroller).setVisibility(View.GONE);
                    }
                });

        view.findViewById(R.id.list_games_filter)
                .animate()
                .setDuration(375)
                .translationX(p.x)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Objects.requireNonNull(getView()).findViewById(R.id.list_games_filter).setVisibility(View.GONE);
                        ((EditText) view.findViewById(R.id.list_games_filter)).setText("");
                    }
                });

        consoleListRecyclerView
                .animate()
                .setDuration(375)
                .translationX(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        consoleListRecyclerView.setVisibility(View.VISIBLE);
                        consoleListLayoutManager.scrollToPositionWithOffset(scrollPosition, 0);
                    }
                });

        view.findViewById(R.id.list_no_games).setVisibility(View.GONE);
    }

    public void onConsoleSelected(int position, String console, String consoleName) {
        this.scrollPosition = position;
        this.consoleName = consoleName;

        // Hide Console List RecyclerView
        consoleListRecyclerView
                .animate()
                .setDuration(375)
                .translationX(-p.x)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        consoleListRecyclerView.setVisibility(View.GONE);
                    }
                });

        // Set up Game List RecyclerView
        isShowingGames = true;
        gameSummaryAdapter.clear();
        gameSummaryAdapter.updateGameSummaries(0, 0);
        apiConnection.GetGameList(console, this);
    }

}
