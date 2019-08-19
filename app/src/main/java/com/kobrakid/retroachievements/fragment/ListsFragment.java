package com.kobrakid.retroachievements.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class ListsFragment extends Fragment implements RAAPICallback {

    private static final String TAG = ListsFragment.class.getName();

    public boolean isShowingGames = false;

    private RAAPIConnection apiConnection;
    private boolean isActive = false;
    private boolean hideEmptyConsoles;
    private boolean isPopulatingConsoles = false;

    private RecyclerView consoleListRecyclerView, gameListRecyclerView;
    private LinearLayoutManager consoleListLayoutManager, gameListLayoutManager;
    public ConsoleAdapter consoleAdapter;
    private GameSummaryAdapter gameAdapter;
    private ArrayList<String> consoleIDs, consoleNames, gameImageIcons, gameTitles, gameStats, gameIDs;
    private int scrollPosition = 0;

    public ListsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        apiConnection = ((MainActivity) getActivity()).apiConnection;
        hideEmptyConsoles = getActivity().getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE).getBoolean(getString(R.string.empty_console_hide_setting), false);

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
        gameListRecyclerView = view.findViewById(R.id.list_games);
        gameImageIcons = new ArrayList<>();
        gameTitles = new ArrayList<>();
        gameStats = new ArrayList<>();
        gameIDs = new ArrayList<>();
        gameAdapter = new GameSummaryAdapter(gameImageIcons, gameTitles, gameStats, gameIDs);
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

        getActivity().setTitle("Consoles");

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
        JSONArray reader;
        if (responseCode == RAAPIConnection.RESPONSE_GET_CONSOLE_IDS) {
            isPopulatingConsoles = true;
            consoleIDs.clear();
            consoleNames.clear();
            consoleAdapter.notifyDataSetChanged();
            try {
                reader = new JSONArray(response);
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
                        JSONObject console = reader.getJSONObject(i);
                        final String id = console.getString("ID");
                        final String name = console.getString("Name");

                        final RetroAchievementsDatabase db = RetroAchievementsDatabase.getInstance(getContext());
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                // Get current console
                                List<Console> current = db.consoleDao().getConsoleWithID(Integer.parseInt(id));
                                // If it exists and has 0 games
                                if (current.size() > 0 && current.get(0).getGameCount() == 0 && consoleNames.contains(name)) {
                                    final int pos = consoleNames.indexOf(name);
                                    Log.d(TAG, "Removing " + consoleIDs.remove(pos) + ": " + consoleNames.remove(pos) + " @ " + pos);
                                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            consoleAdapter.notifyItemRemoved(pos);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_LIST) {
            try {
                reader = new JSONArray(response);
                if (reader.length() > 0) {
                    getActivity().findViewById(R.id.list_no_games).setVisibility(View.GONE);
                    for (int i = 0; i < reader.length(); i++) {
                        JSONObject game = reader.getJSONObject(i);
                        gameTitles.add(game.getString("Title"));
                        gameIDs.add(game.getString("ID"));
                        gameImageIcons.add(game.getString("ImageIcon"));
                    }
                } else {
                    getActivity().findViewById(R.id.list_no_games).setVisibility(View.VISIBLE);
                }
                // Show Game List RecyclerView
                gameAdapter.notifyDataSetChanged();
                gameListRecyclerView.animate().setDuration(375).translationX(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationEnd(animation);
                        gameListRecyclerView.setVisibility(View.VISIBLE);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed() {
        getActivity().setTitle("Consoles");

        TypedValue typedValue = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.drawable.ic_menu, typedValue, true)) {
            ((MainActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(typedValue.resourceId);
        } else {
            ((MainActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);
        gameListRecyclerView.animate().setDuration(375).translationX(p.x).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                gameListRecyclerView.setVisibility(View.GONE);
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
    }

    public void onConsoleSelected(int position, String console, String consoleName) {
        getActivity().setTitle(consoleName);

        isPopulatingConsoles = false;

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
            ((MainActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(typedValue.resourceId);
        } else {
            ((MainActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        isShowingGames = true;
    }

}
