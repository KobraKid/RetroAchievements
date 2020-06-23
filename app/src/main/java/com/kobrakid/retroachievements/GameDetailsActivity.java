package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.kobrakid.retroachievements.adapter.GameDetailsPagerAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.Objects;

/**
 * This class will display detailed information about a single game.
 */
public class GameDetailsActivity extends AppCompatActivity implements RAAPICallback {

    public static int currentPosition = 0;

    RAAPIConnection apiConnection;
    private String gameID, console, imageIcon, title, developer, publisher, genre, released, forumTopicID;
    private boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up theme and title bar
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_game_details);

        setSupportActionBar(findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

        if (getIntent().getExtras() != null) {
            gameID = getIntent().getExtras().getString("GameID");

            // Set up API connection
            apiConnection = new RAAPIConnection(this);

            ViewPager viewPager = findViewById(R.id.game_details_view_pager);
            viewPager.setAdapter(new GameDetailsPagerAdapter(getSupportFragmentManager(), gameID));
            viewPager.setOffscreenPageLimit(GameDetailsPagerAdapter.getPageActiveCount());

            ImageButton page0 = findViewById(R.id.game_details_button_page_0);
            if (page0 != null)
                page0.setOnClickListener((view) -> viewPager.setCurrentItem(0));
            ImageButton page1 = findViewById(R.id.game_details_button_page_1);
            if (page1 != null)
                page1.setOnClickListener((view) -> viewPager.setCurrentItem(1));
            ImageButton page2 = findViewById(R.id.game_details_button_page_2);
            if (page2 != null)
                page2.setOnClickListener((view) -> viewPager.setCurrentItem(2));

            if (savedInstanceState == null || savedInstanceState.getString("forumTopicID") == null) {
                apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, gameID, this);
                // TODO Linked hashes requires login
                //  apiConnection.GetLinkedHashes(gameID, this);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("gameID", gameID);
        outState.putString("console", console);
        outState.putString("imageIcon", imageIcon);
        outState.putString("title", title);
        outState.putString("developer", developer);
        outState.putString("publisher", publisher);
        outState.putString("genre", genre);
        outState.putString("released", released);
        outState.putString("forumTopicID", forumTopicID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        gameID = savedInstanceState.getString("gameID");
        console = savedInstanceState.getString("console");
        imageIcon = savedInstanceState.getString("imageIcon");
        title = savedInstanceState.getString("title");
        developer = savedInstanceState.getString("developer");
        publisher = savedInstanceState.getString("publisher");
        genre = savedInstanceState.getString("genre");
        released = savedInstanceState.getString("released");
        forumTopicID = savedInstanceState.getString("forumTopicID");
        if (forumTopicID != null)
            populateElements();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_overflow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean handled = false;
        switch (id) {
            case android.R.id.home:
                handled = true;
                finish();
                break;
            case R.id.action_forum:
                handled = true;
                String forumUrl = Consts.BASE_URL + "/" + Consts.FORUM_POSTFIX + forumTopicID;
                Intent forumIntent = new Intent(Intent.ACTION_VIEW);
                forumIntent.setData(Uri.parse(forumUrl));
                startActivity(forumIntent);
                break;
            case R.id.action_webpage:
                handled = true;
                String raUrl = Consts.BASE_URL + "/" + Consts.GAME_POSTFIX + "/" + gameID;
                Intent raIntent = new Intent(Intent.ACTION_VIEW);
                raIntent.setData(Uri.parse(raUrl));
                startActivity(raIntent);
                break;
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
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS) {
            JSONObject reader;
            try {
                reader = new JSONObject(response);

                title = Jsoup.parse(reader.getString("Title").trim()).text();
                if (title.contains(", The"))
                    title = "The " + title.substring(0, title.indexOf(", The")) + title.substring(title.indexOf(", The") + 5);
                console = reader.getString("ConsoleName");
                imageIcon = reader.getString("ImageIcon");
                developer = reader.getString("Developer");
                developer = developer.equals("null") ? "????" : Jsoup.parse(developer).text();
                publisher = reader.getString("Publisher");
                publisher = publisher.equals("null") ? "????" : Jsoup.parse(publisher).text();
                genre = reader.getString("Genre");
                genre = genre.equals("null") ? "????" : Jsoup.parse(genre).text();
                released = reader.getString("Released");
                released = released.equals("null") ? "????" : Jsoup.parse(released).text();
                forumTopicID = reader.getString("ForumTopicID");
                populateElements();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateElements() {
        setTitle(title + " (" + console + ")");
        Picasso.get()
                .load(Consts.BASE_URL + imageIcon)
                .placeholder(R.drawable.game_placeholder)
                .into((ImageView) findViewById(R.id.game_details_image_icon));
        ((TextView) findViewById(R.id.game_details_developer)).setText(getString(R.string.developed, developer));
        ((TextView) findViewById(R.id.game_details_publisher)).setText(getString(R.string.published, publisher));
        ((TextView) findViewById(R.id.game_details_genre)).setText(getString(R.string.genre, genre));
        ((TextView) findViewById(R.id.game_details_release_date)).setText(getString(R.string.released, released));
    }

}
