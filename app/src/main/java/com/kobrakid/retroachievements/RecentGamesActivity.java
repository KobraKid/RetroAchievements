package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class will display a more comprehensive list of recent games, rather than the
 * quick 5-game summary present on the home screen.
 */
public class RecentGamesActivity extends AppCompatActivity implements RAAPICallback {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<String> imageIcons, titles, stats, ids;

    private boolean isActive = false;

    RAAPIConnection apiConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        String theme = sharedPref.getString(getString(R.string.theme_setting), "Blank");
        setTheme(ThemeToggler.getTheme(this, sharedPref));

        setContentView(R.layout.activity_recent_games);
        setTitle(R.string.recent_games_title);
        overridePendingTransition(R.anim.slide_in, android.R.anim.fade_out);

        apiConnection = new RAAPIConnection(
                MainActivity.ra_api_user,
                MainActivity.ra_api_key,
                RecentGamesActivity.this);

        // Set up title bar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recent_games_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        imageIcons = new ArrayList<>();
        titles = new ArrayList<>();
        stats = new ArrayList<>();
        ids = new ArrayList<>();

        adapter = new GameSummaryAdapter(imageIcons, titles, stats, ids);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiConnection.GetUserRecentlyPlayedGames(MainActivity.ra_user, 15, 0, this);
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
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_RECENTLY_PLAYED_GAMES) {
            JSONArray reader;
            try {
                reader = new JSONArray(response);

                ids.clear();
                imageIcons.clear();
                titles.clear();
                stats.clear();

                for (int i = 0; i < reader.length(); i++) {
                    JSONObject game = reader.getJSONObject(i);
                    imageIcons.add(i, game.getString("ImageIcon"));
                    titles.add(i, game.getString("Title"));

                    stats.add(i, getString(R.string.game_stats,
                            game.getString("NumAchieved"),
                            game.getString("NumPossibleAchievements"),
                            game.getString("ScoreAchieved"),
                            game.getString("PossibleScore")));

                    ids.add(i, game.getString("GameID"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();
        }
    }

    public void showGameDetails(View view) {
        Intent intent = new Intent(this, GameDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("GameID",
                ((TextView) view.findViewById(R.id.game_summary_game_id)).getText().toString());
        intent.putExtras(extras);
        startActivity(intent);
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
}
