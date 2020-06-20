package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.adapter.ParticipantsAdapter;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single leaderboard instance. Displays information about the leaderboard
 * and lists the participating users.
 */
public class LeaderboardActivity extends AppCompatActivity implements RAAPICallback {

    private ParticipantsAdapter adapter;
    private boolean isActive = false;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up theme and title bar
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_leaderboard);

        setSupportActionBar(findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String id = bundle.getString("ID");
            String game = bundle.getString("GAME");
            String image = bundle.getString("IMAGE");
            String console = bundle.getString("CONSOLE");
            String title = bundle.getString("TITLE");
            String description = bundle.getString("DESCRIPTION");
            String type = bundle.getString("TYPE");
            String count = bundle.getString("NUMRESULTS");

            setTitle(game + ": " + title);
            Picasso.get().load(image).into((ImageView) findViewById(R.id.leaderboard_game_icon));
            if (console != null && console.equals(""))
                ((TextView) findViewById(R.id.leaderboard_title)).setText(title);
            else
                ((TextView) findViewById(R.id.leaderboard_title)).setText(getString(R.string.leaderboard_title, title, console));
            ((TextView) findViewById(R.id.leaderboard_description)).setText(description);
            if (type != null && type.contains("Score"))
                ((TextView) findViewById(R.id.leaderboard_type)).setText(getString(R.string.type_score, type));
            else if (type != null && type.contains("Time"))
                ((TextView) findViewById(R.id.leaderboard_type)).setText(getString(R.string.type_time, type));
            else
                ((TextView) findViewById(R.id.leaderboard_type)).setText(type);

            RecyclerView rankedUsers = findViewById(R.id.leaderboard_participants);
            adapter = new ParticipantsAdapter(this);
            rankedUsers.setAdapter(adapter);
            rankedUsers.setLayoutManager(new LinearLayoutManager(this));

            if (savedInstanceState == null) {
                new RAAPIConnection(this).GetLeaderboard(id, count, this);
            } else {
                ArrayList<String> savedUsers = (ArrayList<String>) savedInstanceState.getSerializable("users");
                ArrayList<String> savedResults = (ArrayList<String>) savedInstanceState.getSerializable("results");
                ArrayList<String> savedDates = (ArrayList<String>) savedInstanceState.getSerializable("dates");
                if (savedUsers != null && savedUsers.size() > 0
                        && savedResults != null && savedResults.size() > 0
                        && savedDates != null && savedDates.size() > 0) {
                    adapter.users.addAll(savedUsers);
                    adapter.results.addAll(savedResults);
                    adapter.dates.addAll(savedDates);
                    adapter.notifyDataSetChanged();
                } else {
                    new RAAPIConnection(this).GetLeaderboard(id, count, this);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("users", (Serializable) adapter.users);
        outState.putSerializable("results", (Serializable) adapter.results);
        outState.putSerializable("dates", (Serializable) adapter.dates);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean handled = false;
        if (id == android.R.id.home) {
            handled = true;
            finish();
        }
        if (handled)
            return true;
        else
            return super.onOptionsItemSelected(item);

    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_LEADERBOARD) {
            new ParseHTMLAsyncTask(adapter).execute(response);
        }
    }

    private static class ParseHTMLAsyncTask extends AsyncTask<String, String, Void> {

        private final WeakReference<ParticipantsAdapter> adapterReference;

        ParseHTMLAsyncTask(ParticipantsAdapter adapter) {
            this.adapterReference = new WeakReference<>(adapter);
        }

        @Override
        protected Void doInBackground(String... strings) {
            Document document = Jsoup.parse(strings[0]);
            Elements userData = document.select("td.lb_user");
            Elements resultData = document.select("td.lb_result");
            Elements dateData = document.select("td.lb_date");
            for (int i = 0; i < userData.size(); i++) {
                publishProgress(userData.get(i).text(), resultData.get(i).text(), dateData.get(i).text());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            final ParticipantsAdapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.addParticipant(values[0], values[1], values[2]);
            }
        }
    }
}
