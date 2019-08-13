package com.kobrakid.retroachievements.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.ConsoleAdapter;
import com.kobrakid.retroachievements.adapter.GameSummaryAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class ListsFragment extends Fragment implements RAAPICallback {

    private boolean isActive = false;

    private RAAPIConnection apiConnection;

    public RecyclerView consoleListRecyclerView, gameListRecyclerView;
    private LinearLayoutManager consoleListLayoutManager, gameListLayoutManager;
    public ConsoleAdapter consoleAdapter;
    public GameSummaryAdapter gameAdapter;
    private ArrayList<String> consoleIDs, consoleNames, gameImageIcons, gameTitles, gameStats, gameIDs;
    public int scrollPosition = 0;
    public boolean isShowingGames = false;

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

    public void onConsoleSelected(int position, String console, String consoleName) {
        getActivity().setTitle(consoleName);

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
            consoleIDs.clear();
            consoleNames.clear();
            try {
                reader = new JSONArray(response);
                for (int i = 0; i < reader.length(); i++) {
                    JSONObject console = reader.getJSONObject(i);
                    consoleNames.add(console.getString("Name"));
                    Collections.sort(consoleNames);
                    consoleIDs.add(consoleNames.indexOf(console.getString("Name")), console.getString("ID"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            consoleAdapter.notifyDataSetChanged();
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            gameAdapter.notifyDataSetChanged();

            // Show Game List RecyclerView
            gameListRecyclerView.animate().setDuration(375).translationX(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationEnd(animation);
                    gameListRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

}
