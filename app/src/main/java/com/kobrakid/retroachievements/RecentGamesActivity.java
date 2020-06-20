package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kobrakid.retroachievements.adapter.GameSummaryAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

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
    private boolean hasParsed = false; // Prevent spam API calls while scrolling repeatedly

    private GameSummaryAdapter gameSummaryAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        setSupportActionBar(findViewById(R.id.recent_games_toolbar));
        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recent_games_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        gameSummaryAdapter = new GameSummaryAdapter(this);
        recyclerView.setAdapter(gameSummaryAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // Try to catch user reaching end of list early and append past the screen.
                // If the user has already scrolled to the end, the scrolling will halt while more
                // entries are added.
                if (layoutManager.findLastVisibleItemPosition() >= (offset + gamesPerAPICall - 2) && hasParsed) {
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
        if (!isActive) {
            offset = Math.max(0, offset - gamesPerAPICall);
            return;
        }
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_RECENTLY_PLAYED_GAMES) {
            JSONArray reader;
            try {
                reader = new JSONArray(response);

                if (offset == 0)
                    gameSummaryAdapter.clear();

                for (int i = 0; i < reader.length(); i++) {
                    JSONObject game = reader.getJSONObject(i);
                    gameSummaryAdapter.addGame(
                            i + offset,
                            game.getString("GameID"),
                            game.getString("ImageIcon"),
                            game.getString("Title"),
                            getString(R.string.game_stats,
                                    game.getString("NumAchieved"),
                                    game.getString("NumPossibleAchievements"),
                                    game.getString("ScoreAchieved"),
                                    game.getString("PossibleScore")),
                            (!game.getString("NumAchieved").equals("0"))
                                    && game.getString("NumAchieved").equals(game.getString("NumPossibleAchievements"))
                    );
                }
                swipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            gameSummaryAdapter.updateGameSummaries(offset, gamesPerAPICall);
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
