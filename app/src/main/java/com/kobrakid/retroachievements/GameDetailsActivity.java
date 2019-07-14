package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment;

/**
 * This class will display detailed information about a single game.
 */
public class GameDetailsActivity extends AppCompatActivity implements RAAPICallback, AchievementSummaryFragment.OnFragmentInteractionListener {

    private String gameID;
    public RAAPIConnection apiConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Set up theme and title bar
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        setTheme(ThemeToggler.getTheme(this, sharedPref));

        setContentView(R.layout.activity_game_details);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        gameID = getIntent().getExtras().getString("GameID");

        // Set up API connection
        apiConnection = new RAAPIConnection(MainActivity.ra_api_user, MainActivity.ra_api_key, this);

        // Create fragment
        Bundle bundle = new Bundle();
        bundle.putString("GameID", gameID);
        AchievementSummaryFragment summaryFragment = new AchievementSummaryFragment();
        summaryFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.game_details_frame, summaryFragment).commit();
    }

    @Override
    public void callback(int responseCode, String response) {

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
                onBackPressed();
                break;
            case R.id.action_forum:
                handled = true;
                String forumUrl = "https://retroachievements.org/viewtopic.php?t="; // TODO + forumTopicID;
                Intent forumIntent = new Intent(Intent.ACTION_VIEW);
                forumIntent.setData(Uri.parse(forumUrl));
                startActivity(forumIntent);
                break;
            case R.id.action_webpage:
                handled = true;
                String raUrl = "https://retroachievements.org/game/" + gameID;
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
    public void onFragmentInteraction(Uri uri) {

    }
}
