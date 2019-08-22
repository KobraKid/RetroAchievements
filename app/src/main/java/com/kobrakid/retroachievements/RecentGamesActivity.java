package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kobrakid.retroachievements.adapter.GameSummaryAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class will display a more comprehensive list of recent games, rather than the
 * quick 5-game summary present on the home screen.
 */
public class RecentGamesActivity extends AppCompatActivity implements RAAPICallback, SwipeRefreshLayout.OnRefreshListener {

    // API
    private RAAPIConnection apiConnection;
    private boolean isActive = false;
    private int offset;
    private final int gamesPerAPICall = 15;
    private boolean hasParsed = false; // Easy way to prevent spam API calls while scrolling quickly

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<String> imageIcons, titles, stats, ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_recent_games);
        setTitle(R.string.recent_games_title);
        overridePendingTransition(R.anim.slide_in, android.R.anim.fade_out);

        apiConnection = new RAAPIConnection(
                RecentGamesActivity.this);

        // Set up title bar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recent_games_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        imageIcons = new ArrayList<>();
        titles = new ArrayList<>();
        stats = new ArrayList<>();
        ids = new ArrayList<>();

        adapter = new GameSummaryAdapter(this, imageIcons, titles, stats, ids);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && hasParsed) {
                    hasParsed = false;
                    offset += gamesPerAPICall;
                    apiConnection.GetUserRecentlyPlayedGames(MainActivity.ra_user, gamesPerAPICall, offset, RecentGamesActivity.this);
                }
            }
        });

        // Set up refresh action
        swipeRefreshLayout = findViewById(R.id.recent_games_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        offset = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        hasParsed = false;
        apiConnection.GetUserRecentlyPlayedGames(MainActivity.ra_user, gamesPerAPICall, offset, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onRefresh() {
        hasParsed = false;
        offset = 0;
        apiConnection.GetUserRecentlyPlayedGames(MainActivity.ra_user, gamesPerAPICall, offset, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out);
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_RECENTLY_PLAYED_GAMES) {
            JSONArray reader;
            try {
                reader = new JSONArray(response);

                if (offset == 0) {
                    ids.clear();
                    imageIcons.clear();
                    titles.clear();
                    stats.clear();
                }

                for (int i = 0; i < reader.length(); i++) {
                    JSONObject game = reader.getJSONObject(i);
                    imageIcons.add(i + offset, game.getString("ImageIcon"));
                    titles.add(i + offset, game.getString("Title"));

                    stats.add(i + offset, getString(R.string.game_stats,
                            game.getString("NumAchieved"),
                            game.getString("NumPossibleAchievements"),
                            game.getString("ScoreAchieved"),
                            game.getString("PossibleScore")));

                    ids.add(i + offset, game.getString("GameID"));
                }
                swipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (offset == 0)
                adapter.notifyDataSetChanged();
            else
                adapter.notifyItemRangeInserted(offset, gamesPerAPICall);
            hasParsed = true;
        }
    }

    /**
     * Sets up a new activity to show more details on a particular game.
     *
     * @param view The game the user tapped on.
     */
    public void showGameDetails(View view) {
        Intent intent = new Intent(this, GameDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("GameID",
                ((TextView) view.findViewById(R.id.game_summary_game_id)).getText().toString());
        intent.putExtras(extras);
        startActivity(intent);
    }
}
