package com.kobrakid.retroachievements.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.ConsoleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class ListsFragment extends Fragment implements RAAPICallback {

    private boolean isActive = false;

    private RAAPIConnection apiConnection;

    public RecyclerView consoleListRecyclerView;
    private RecyclerView gameListRecyclerView;
    private LinearLayoutManager layoutManager;
    public ConsoleAdapter adapter;
    private ArrayList<String> consoleIDs, consoleNames;
    public int scrollPosition = 0;

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
        adapter = new ConsoleAdapter(consoleIDs, consoleNames, gameListRecyclerView, this);
        consoleListRecyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getContext());
        consoleListRecyclerView.setLayoutManager(layoutManager);

        // Wait to set up game list until console is chosen
        gameListRecyclerView = view.findViewById(R.id.list_games);

        // TODO figure out what titles should be/change to
        getActivity().setTitle("Consoles");

        return view;
    }

    public void onBackPressed() {
        adapter.isExpanded = false;
        consoleListRecyclerView.animate().setDuration(375).translationX(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                consoleListRecyclerView.setVisibility(View.VISIBLE);
                layoutManager.scrollToPositionWithOffset(scrollPosition, 0);
            }
        });
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
            try {
                JSONArray reader = new JSONArray(response);
                for (int i = 0; i < reader.length(); i++) {
                    JSONObject console = reader.getJSONObject(i);
                    consoleNames.add(console.getString("Name"));
                    Collections.sort(consoleNames);
                    consoleIDs.add(consoleNames.indexOf(console.getString("Name")), console.getString("ID"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    }
}
