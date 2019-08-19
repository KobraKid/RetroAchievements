package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.kobrakid.retroachievements.adapter.ParticipantsAdapter;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity implements RAAPICallback {

    public RAAPIConnection apiConnection;

    private String id, game, image, console, title, description, type, count;
    private RecyclerView rankedUsers;
    private RecyclerView.Adapter adapter;
    private ArrayList<String> users, results, dates;
    private boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up theme and title bar
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_leaderboard);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        id = getIntent().getExtras().getString("ID");
        game = getIntent().getExtras().getString("GAME");
        image = getIntent().getExtras().getString("IMAGE");
        console = getIntent().getExtras().getString("CONSOLE");
        title = getIntent().getExtras().getString("TITLE");
        description = getIntent().getExtras().getString("DESCRIPTION");
        type = getIntent().getExtras().getString("TYPE");
        count = getIntent().getExtras().getString("NUMRESULTS");
        apiConnection = new RAAPIConnection(this);

        setTitle(game + ": " + title);
        Picasso.get().load(image).into((ImageView) findViewById(R.id.leaderboard_game_icon));
        ((TextView) findViewById(R.id.leaderboard_title)).setText(title + " (" + console + ")");
        ((TextView) findViewById(R.id.leaderboard_description)).setText(description);
        ((TextView) findViewById(R.id.leaderboard_type)).setText(type);

        rankedUsers = findViewById(R.id.leaderboard_participants);
        users = new ArrayList<>();
        results = new ArrayList<>();
        dates = new ArrayList<>();
        adapter = new ParticipantsAdapter(this, users, results, dates);
        rankedUsers.setAdapter(adapter);
        rankedUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiConnection.GetLeaderboard(id, count, this);
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
            new ParseHTMLAsyncTask(adapter, users, results, dates).execute(response);
        }
    }

    private static class ParseHTMLAsyncTask extends AsyncTask<String, Integer, Void> {

        private WeakReference<RecyclerView.Adapter> adapterReference;
        private WeakReference<ArrayList<String>> usersReference, resultsReference, datesReference;

        ParseHTMLAsyncTask(RecyclerView.Adapter adapter, ArrayList<String> users, ArrayList<String> results, ArrayList<String> dates) {
            this.adapterReference = new WeakReference<>(adapter);
            this.usersReference = new WeakReference<>(users);
            this.resultsReference = new WeakReference<>(results);
            this.datesReference = new WeakReference<>(dates);
        }

        @Override
        protected Void doInBackground(String... strings) {
            ArrayList<String> users = new ArrayList<>(), results = new ArrayList<>(), dates = new ArrayList<>();

            Document document = Jsoup.parse(strings[0]);
            Elements data = document.select("td.lb_user");
            for (Element e : data)
                users.add(e.text());
            data = document.select("td.lb_result");
            for (Element e : data)
                results.add(e.text());
            data = document.select("td.lb_date");
            for (Element e : data)
                dates.add(e.text());
            final ArrayList<String> u = usersReference.get(), r = resultsReference.get(), d = datesReference.get();
            u.addAll(users);
            r.addAll(results);
            d.addAll(dates);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }
}
