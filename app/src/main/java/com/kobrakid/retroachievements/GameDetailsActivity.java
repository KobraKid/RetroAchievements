package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    private String gameID;
    private String forumTopicID;
    private boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up theme and title bar
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        setTheme(ThemeManager.getTheme(this, sharedPref));

        setContentView(R.layout.activity_game_details);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        gameID = Objects.requireNonNull(getIntent().getExtras()).getString("GameID");

        // Set up API connection
        RAAPIConnection apiConnection = new RAAPIConnection(this);

        ViewPager viewPager = findViewById(R.id.game_details_view_pager);
        viewPager.setAdapter(new GameDetailsPagerAdapter(getSupportFragmentManager(), gameID));

        apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, gameID, this);
        // TODO Linked hashes requires login
        //  apiConnection.GetLinkedHashes(gameID, this);
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

                String title = Jsoup.parse(reader.getString("Title").trim()).text();
                if (title.contains(", The"))
                    title = "The " + title.substring(0, title.indexOf(", The")) + title.substring(title.indexOf(", The") + 5);
                setTitle(title + " (" + reader.getString("ConsoleName") + ")");

                Picasso.get()
                        .load(Consts.BASE_URL + reader.getString("ImageIcon"))
                        .into((ImageView) findViewById(R.id.game_details_image_icon));
                String developer = reader.getString("Developer");
                developer = developer.equals("null") ? "????" : Jsoup.parse(developer).text();
                String publisher = reader.getString("Publisher");
                publisher = publisher.equals("null") ? "????" : Jsoup.parse(publisher).text();
                String genre = reader.getString("Genre");
                genre = genre.equals("null") ? "????" : Jsoup.parse(genre).text();
                String released = reader.getString("Released");
                released = released.equals("null") ? "????" : Jsoup.parse(released).text();
                ((TextView) findViewById(R.id.game_details_developer)).setText(getString(R.string.developed, developer));
                ((TextView) findViewById(R.id.game_details_publisher)).setText(getString(R.string.published, publisher));
                ((TextView) findViewById(R.id.game_details_genre)).setText(getString(R.string.genre, genre));
                ((TextView) findViewById(R.id.game_details_release_date)).setText(getString(R.string.released, released));

                forumTopicID = reader.getString("ForumTopicID");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
