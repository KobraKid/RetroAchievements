package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kobrakid.retroachievements.fragment.AchievementDetailsFragment;
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * This class will display detailed information about a single game.
 */
public class GameDetailsActivity extends AppCompatActivity implements RAAPICallback, AchievementSummaryFragment.OnFragmentInteractionListener, AchievementDetailsFragment.OnFragmentInteractionListener {

    private String gameID;
    private String forumTopicID;
    private boolean isActive = false;
    public static int currentPosition = 0;

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
        getSupportFragmentManager().beginTransaction().add(R.id.game_details_frame, summaryFragment).commit();

        apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, gameID, this);
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
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS) {
            JSONObject reader;
            try {
                reader = new JSONObject(response);

                setTitle(reader.getString("Title") + " (" + reader.getString("ConsoleName") + ")");

                JSONObject achievements = reader.getJSONObject("Achievements");
                JSONObject achievement;
                int numEarned = 0, numEarnedHC = 0, totalAch = 0, earnedPts = 0, totalPts = 0, earnedRatio = 0, totalRatio = 0;
                for (Iterator<String> keys = achievements.keys(); keys.hasNext(); ) {
                    String achievementID = keys.next();
                    achievement = achievements.getJSONObject(achievementID);

                    if (achievement.has("DateEarnedHardcore")) {
                        numEarned++;
                        numEarnedHC++;
                        earnedPts += 2 * Integer.parseInt(achievement.getString("Points"));
                        earnedRatio += Integer.parseInt(achievement.getString("TrueRatio"));
                    } else if (achievement.has("DateEarned")) {
                        numEarned++;
                        earnedPts += Integer.parseInt(achievement.getString("Points"));
                        earnedRatio += Integer.parseInt(achievement.getString("TrueRatio"));
                    }
                    totalAch++;
                    totalPts += Integer.parseInt(achievement.getString("Points"));
                    totalRatio += Integer.parseInt(achievement.getString("TrueRatio"));
                }

                Picasso.get()
                        .load("https://retroachievements.org" + reader.getString("ImageIcon"))
                        .into((ImageView) findViewById(R.id.game_details_image_icon));
                ((TextView) findViewById(R.id.game_details_developer)).setText(getString(R.string.developed, reader.getString("Developer")));
                ((TextView) findViewById(R.id.game_details_publisher)).setText(getString(R.string.published, reader.getString("Publisher")));
                ((TextView) findViewById(R.id.game_details_genre)).setText(getString(R.string.genre, reader.getString("Genre")));
                ((TextView) findViewById(R.id.game_details_release_date)).setText(getString(R.string.released, reader.getString("Released")));

                ((TextView) findViewById(R.id.game_details_progress_text))
                        .setText(getString(
                                R.string.completion,
                                new DecimalFormat("@@@@")
                                        .format(((float) (numEarned + numEarnedHC) / (float) totalAch) * 100.0)));
                ((ProgressBar) findViewById(R.id.game_details_progress)).setProgress((int) (((float) numEarned) / ((float) totalAch) * 10000.0));
                ((TextView) findViewById(R.id.game_details_user_summary))
                        .setText(Html.fromHtml(getString(
                                R.string.user_summary,
                                numEarned,
                                totalAch,
                                numEarnedHC,
                                earnedPts,
                                earnedRatio,
                                totalPts,
                                totalRatio)));

                forumTopicID = reader.getString("ForumTopicID");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                String forumUrl = "https://retroachievements.org/viewtopic.php?t=" + forumTopicID;
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
