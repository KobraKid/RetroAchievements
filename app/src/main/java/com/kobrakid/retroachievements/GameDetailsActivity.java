package com.kobrakid.retroachievements;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class will display detailed information about a single game.
 */
public class GameDetailsActivity extends AppCompatActivity implements RAAPICallback {

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


        // Set up Achievements toggle
        final ImageButton achievementHeaderGroup = findViewById(R.id.game_details_toggle_achievements);
        achievementHeaderGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View gameDetailsFrame = findViewById(R.id.game_details_frame);
                if (gameDetailsFrame.getVisibility() == View.GONE) {
                    achievementHeaderGroup.setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_up));
                    gameDetailsFrame
                            .animate()
                            .alpha(1.0f)
                            .translationYBy(gameDetailsFrame.getHeight())
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    gameDetailsFrame.setVisibility(View.VISIBLE);
                                }
                            });
                } else {
                    achievementHeaderGroup.setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_down));
                    gameDetailsFrame
                            .animate()
                            .alpha(0.0f)
                            .translationYBy(-gameDetailsFrame.getHeight())
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    gameDetailsFrame.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });

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

                setTitle(reader.getString("Title").trim() + " (" + reader.getString("ConsoleName") + ")");
                Picasso.get()
                        .load("https://retroachievements.org" + reader.getString("ImageIcon"))
                        .into((ImageView) findViewById(R.id.game_details_image_icon));
                String developer = reader.getString("Developer");
                developer = developer.equals("null") ? "????" : developer;
                String publisher = reader.getString("Publisher");
                publisher = publisher.equals("null") ? "????" : publisher;
                String genre = reader.getString("Genre");
                genre = genre.equals("null") ? "????" : genre;
                String released = reader.getString("Released");
                released = released.equals("null") ? "????" : released;
                ((TextView) findViewById(R.id.game_details_developer)).setText(getString(R.string.developed, developer));
                ((TextView) findViewById(R.id.game_details_publisher)).setText(getString(R.string.published, publisher));
                ((TextView) findViewById(R.id.game_details_genre)).setText(getString(R.string.genre, genre));
                ((TextView) findViewById(R.id.game_details_release_date)).setText(getString(R.string.released, released));

                if (reader.getString("NumAchievements").equals("0")) {
                    findViewById(R.id.game_details_no_achievements).setVisibility(View.VISIBLE);
                } else {
                    JSONObject achievements = reader.getJSONObject("Achievements");
                    JSONObject achievement;
                    int numEarned = 0, numEarnedHC = 0, totalAch = 0, earnedPts = 0, totalPts = 0, earnedRatio = 0, totalRatio = 0;
                    ArrayList earnedTotals = new ArrayList<Integer>();
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
                        earnedTotals.add(Integer.parseInt(achievement.getString("NumAwarded")));
                    }

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

                    setupAchievementDistributionChart(earnedTotals);

                    forumTopicID = reader.getString("ForumTopicID");
                }
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
                finish();
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

    private void setupAchievementDistributionChart(ArrayList<Integer> achievementTotals) {
        // TODO Gives approximate distribution representation, but is not exact
        int[] achievementCount = new int[achievementTotals.size()];
        int[] userCount = new int[achievementTotals.size()];

        // Initialize arrays
        for (int i = 0; i < achievementTotals.size(); i++) {
            achievementCount[i] = i + 1;
            userCount[i] = 0;
        }

        // Compose data
        int count = -1;
        while (achievementTotals.size() >= 2) {
            int max = Collections.max(achievementTotals);
            int size = achievementTotals.size() - 1;
            for (int i = size; i >= 0; i--) {
                if (achievementTotals.get(i) == max) {
                    achievementTotals.remove(i);
                    count++;
                }
            }
            int nextMax = 0;
            if (achievementTotals.size() > 0)
                nextMax = Collections.max(achievementTotals);
            int diff = max - nextMax;
            userCount[count] = diff;
        }
        if (achievementTotals.size() > 0)
            userCount[count + 1] = achievementTotals.get(0);

        // Set chart
        LineChart chart = findViewById(R.id.game_details_achievement_distribution);

        // Set chart data
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < achievementCount.length; i++) {
            entries.add(new Entry(achievementCount[i], userCount[i]));
        }
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setDrawFilled(true);
        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(false);

        // Set chart colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TypedValue accentColor = new TypedValue(), primaryColor = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorAccent, accentColor, true);
            getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true);
            dataSet.setCircleColor(accentColor.data);
            dataSet.setColor(accentColor.data);
            dataSet.setCircleHoleColor(accentColor.data);
            chart.getAxisLeft().setTextColor(primaryColor.data);
            chart.getXAxis().setTextColor(primaryColor.data);
            dataSet.setFillColor(accentColor.data);
        }

        // Set chart axes
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.setClickable(false);

        // Set chart description
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        // Set chart finalized data
        chart.setData(lineData);

        // Redraw chart
        chart.invalidate();
    }

}
