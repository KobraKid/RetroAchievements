package com.kobrakid.retroachievements;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.SparseIntArray;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will display detailed information about a single game.
 */
public class GameDetailsActivity extends AppCompatActivity implements RAAPICallback {

    public static int currentPosition = 0;
    public RAAPIConnection apiConnection;

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
        actionBar.setDisplayHomeAsUpEnabled(true);

        gameID = getIntent().getExtras().getString("GameID");

        // Set up API connection
        apiConnection = new RAAPIConnection(this);

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
                    achievementHeaderGroup.setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_down));
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
                    achievementHeaderGroup.setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_up));
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
        apiConnection.GetAchievementDistribution(gameID, this);
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

                setTitle(Jsoup.parse(reader.getString("Title").trim()).text() + " (" + reader.getString("ConsoleName") + ")");
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

                if (reader.getString("NumAchievements").equals("0")) {
                    findViewById(R.id.game_details_no_achievements).setVisibility(View.VISIBLE);
                } else {
                    JSONObject achievements = reader.getJSONObject("Achievements");
                    JSONObject achievement;
                    int numEarned = 0, numEarnedHC = 0, totalAch = 0, earnedPts = 0, totalPts = 0, earnedRatio = 0, totalRatio = 0;
                    ArrayList<Integer> earnedTotals = new ArrayList<>();
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

//                    setupAchievementDistributionChart(earnedTotals);

                    forumTopicID = reader.getString("ForumTopicID");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_ACHIEVEMENT_DISTRIBUTION) {
            new AchievementDistributionChartAsyncTask(this, (LineChart) findViewById(R.id.game_details_achievement_distribution), findViewById(R.id.game_details_achievement_distro_loading)).execute(response);
        }
    }

    private static class AchievementDistributionChartAsyncTask extends AsyncTask<String, Integer, Integer[][]> {

        private WeakReference<Context> contextReference;
        private WeakReference<LineChart> lineChartReference;
        private WeakReference<View> loadingBarReference;

        AchievementDistributionChartAsyncTask(Context context, LineChart lineChart, View frameLayout) {
            this.contextReference = new WeakReference<>(context);
            this.lineChartReference = new WeakReference<>(lineChart);
            this.loadingBarReference = new WeakReference<>(frameLayout);
        }

        @Override
        protected Integer[][] doInBackground(String... strings) {
            String response = strings[0];
            Document document = Jsoup.parse(response);
            Elements scripts = document.getElementsByTag("script");
            String rows = scripts.get(1).dataNodes().get(0).getWholeData();
            rows = rows.substring(rows.indexOf("dataTotalScore.addRows("));
            rows = rows.substring(0, rows.indexOf(");"));

            Pattern p1 = Pattern.compile("v:(\\d+),");
            Matcher m1 = p1.matcher(rows);
            Pattern p2 = Pattern.compile(",\\s(\\d+)\\s]");
            Matcher m2 = p2.matcher(rows);
            SparseIntArray achievementTotals = new SparseIntArray();
            while (m1.find() && m2.find()) {
                achievementTotals.put(Integer.parseInt(m1.group(1)), Integer.parseInt(m2.group(1)));
            }

            // Initialize arrays
            Integer[] achievementCount = new Integer[achievementTotals.size()];
            Integer[] userCount = new Integer[achievementTotals.size()];
            for (int i = 0; i < achievementTotals.size(); i++) {
                achievementCount[i] = i + 1;
                userCount[i] = achievementTotals.get(i + 1);
            }

            return new Integer[][]{achievementCount, userCount};
        }

        @Override
        protected void onPostExecute(Integer[][] chartData) {
            super.onPostExecute(chartData);

            final Context context = contextReference.get();
            final LineChart chart = lineChartReference.get();
            if (context != null && chart != null) {
                // Set chart data
                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < chartData[0].length; i++) {
                    entries.add(new Entry(chartData[0][i], chartData[1][i]));
                }
                LineDataSet dataSet = new LineDataSet(entries, "");
                dataSet.setDrawFilled(true);
                LineData lineData = new LineData(dataSet);
                lineData.setDrawValues(false);

                // Set chart colors
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    TypedValue accentColor = new TypedValue(), primaryColor = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorAccent, accentColor, true);
                    context.getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true);
                    chart.getAxisLeft().setTextColor(primaryColor.data);
                    chart.getXAxis().setTextColor(primaryColor.data);
                    dataSet.setCircleColor(accentColor.data);
                    dataSet.setColor(accentColor.data);
                    dataSet.setCircleHoleColor(accentColor.data);
                    dataSet.setFillColor(accentColor.data);
                }

                // Set chart axes
                chart.getAxisRight().setEnabled(false);
                chart.getLegend().setEnabled(false);
                chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                chart.getAxisLeft().setAxisMinimum(0f);
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
            final View view = loadingBarReference.get();
            if (view != null)
                view.setVisibility(View.GONE);
        }
    }

}
