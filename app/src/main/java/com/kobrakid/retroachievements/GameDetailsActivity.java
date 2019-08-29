package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.kobrakid.retroachievements.adapter.AchievementAdapter;
import com.kobrakid.retroachievements.adapter.GameDetailsPagerAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will display detailed information about a single game.
 */
public class GameDetailsActivity extends AppCompatActivity implements RAAPICallback {

    public static final String TAG = GameDetailsActivity.class.getSimpleName();

    public static int currentPosition = 0;
    public RAAPIConnection apiConnection;

    // Views
    public ViewPager viewPager;
    public RecyclerView recyclerView = null;
    public RecyclerView.Adapter adapter = null;
    public RecyclerView.LayoutManager layoutManager = null;

    // Activity-wide
    public String gameID;
    private String forumTopicID;

    // Fragment-Specific
    private ArrayList<String>
            ids = new ArrayList<>(),
            badges = new ArrayList<>(),
            titles = new ArrayList<>(),
            points = new ArrayList<>(),
            trueRatios = new ArrayList<>(),
            descriptions = new ArrayList<>(),
            datesEarned = new ArrayList<>(),
            numsAwarded = new ArrayList<>(),
            numsAwardedHC = new ArrayList<>(),
            authors = new ArrayList<>(),
            datesCreated = new ArrayList<>(),
            datesModified = new ArrayList<>();
    private final Map<String, Boolean> hardcoreEarnings = new HashMap<>();

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

        adapter = new AchievementAdapter(
                this,
                ids,
                badges,
                titles,
                points,
                trueRatios,
                descriptions,
                datesEarned,
                numsAwarded,
                numsAwardedHC,
                authors,
                datesCreated,
                datesModified,
                hardcoreEarnings,
                "1");

        viewPager = findViewById(R.id.game_details_view_pager);
        viewPager.setAdapter(new GameDetailsPagerAdapter(getSupportFragmentManager(), this, gameID));

        // Set up Achievements toggle
//        final ImageButton achievementHeaderGroup = findViewById(R.id.game_details_toggle_achievements);
//        achievementHeaderGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final View gameDetailsFrame = findViewById(R.id.game_details_frame);
//                if (gameDetailsFrame.getVisibility() == View.GONE) {
//                    achievementHeaderGroup.setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_down));
//                    gameDetailsFrame
//                            .animate()
//                            .alpha(1.0f)
//                            .translationYBy(gameDetailsFrame.getHeight())
//                            .setDuration(300)
//                            .setListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationStart(Animator animation) {
//                                    super.onAnimationEnd(animation);
//                                    gameDetailsFrame.setVisibility(View.VISIBLE);
//                                }
//                            });
//                } else {
//                    achievementHeaderGroup.setImageDrawable(getDrawable(R.drawable.ic_arrow_drop_up));
//                    gameDetailsFrame
//                            .animate()
//                            .alpha(0.0f)
//                            .translationYBy(-gameDetailsFrame.getHeight())
//                            .setDuration(300)
//                            .setListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    super.onAnimationEnd(animation);
//                                    gameDetailsFrame.setVisibility(View.GONE);
//                                }
//                            });
//                }
//            }
//        });

        // apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, gameID, this);
        // apiConnection.GetAchievementDistribution(gameID, this);
        // apiConnection.GetLinkedHashes(gameID, this);
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

                if (reader.getString("NumAchievements").equals("0")) {
                    findViewById(R.id.game_details_no_achievements).setVisibility(View.VISIBLE);
                } else {
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
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new AchievementDetailsAsyncTask(
                    adapter,
                    ids,
                    badges,
                    titles,
                    points,
                    trueRatios,
                    descriptions,
                    datesEarned,
                    numsAwarded,
                    numsAwardedHC,
                    authors,
                    datesCreated,
                    datesModified,
                    hardcoreEarnings).execute(response);
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_ACHIEVEMENT_DISTRIBUTION) {
            new AchievementDistributionChartAsyncTask(this,
                    (LineChart) findViewById(R.id.game_details_achievement_distribution),
                    findViewById(R.id.game_details_achievement_distro_loading))
                    .execute(response);
        } else if (responseCode == RAAPIConnection.RESPONSE_GET_LINKED_HASHES) {
            // TODO Linked hashes page requires login
        }
    }

    private static class AchievementDetailsAsyncTask extends AsyncTask<String, Integer, String[]> {

        WeakReference<RecyclerView.Adapter> adapterReference;
        WeakReference<ArrayList<String>> idsReference,
                badgesReference,
                titlesReference,
                pointsReference,
                trueRatiosReference,
                descriptionsReference,
                datesEarnedReference,
                numsAwardedReference,
                numsAwardedHCReference,
                authorsReference,
                datesCreatedReference,
                datesModifiedReference;
        WeakReference<Map<String, Boolean>> hardcoreEarningsReference;
        private String numDistinctCasual = "";

        AchievementDetailsAsyncTask(RecyclerView.Adapter adapter,
                                    ArrayList<String> ids,
                                    ArrayList<String> badges,
                                    ArrayList<String> titles,
                                    ArrayList<String> points,
                                    ArrayList<String> trueRatios,
                                    ArrayList<String> descriptions,
                                    ArrayList<String> datesEarned,
                                    ArrayList<String> numsAwarded,
                                    ArrayList<String> numsAwardedHC,
                                    ArrayList<String> authors,
                                    ArrayList<String> datesCreated,
                                    ArrayList<String> datesModified,
                                    Map<String, Boolean> hardcoreEarnings) {
            this.adapterReference = new WeakReference<>(adapter);
            this.idsReference = new WeakReference<>(ids);
            this.badgesReference = new WeakReference<>(badges);
            this.titlesReference = new WeakReference<>(titles);
            this.pointsReference = new WeakReference<>(points);
            this.trueRatiosReference = new WeakReference<>(trueRatios);
            this.descriptionsReference = new WeakReference<>(descriptions);
            this.datesEarnedReference = new WeakReference<>(datesEarned);
            this.numsAwardedReference = new WeakReference<>(numsAwarded);
            this.numsAwardedHCReference = new WeakReference<>(numsAwardedHC);
            this.authorsReference = new WeakReference<>(authors);
            this.datesCreatedReference = new WeakReference<>(datesCreated);
            this.datesModifiedReference = new WeakReference<>(datesModified);
            this.hardcoreEarningsReference = new WeakReference<>(hardcoreEarnings);
        }

        @Override
        protected String[] doInBackground(String... strings) {
            ArrayList<String> ids = idsReference.get();
            ArrayList<String> badges = badgesReference.get();
            ArrayList<String> titles = titlesReference.get();
            ArrayList<String> points = pointsReference.get();
            ArrayList<String> trueRatios = trueRatiosReference.get();
            ArrayList<String> descriptions = descriptionsReference.get();
            ArrayList<String> datesEarned = datesEarnedReference.get();
            ArrayList<String> numsAwarded = numsAwardedReference.get();
            ArrayList<String> numsAwardedHC = numsAwardedHCReference.get();
            ArrayList<String> authors = authorsReference.get();
            ArrayList<String> datesCreated = datesCreatedReference.get();
            ArrayList<String> datesModified = datesModifiedReference.get();
            Map<String, Boolean> hardcoreEarnings = hardcoreEarningsReference.get();
            try {
                JSONObject reader = new JSONObject(strings[0]);

                ids.clear();
                badges.clear();
                titles.clear();
                points.clear();
                trueRatios.clear();
                descriptions.clear();
                datesEarned.clear();
                numsAwarded.clear();
                numsAwardedHC.clear();
                authors.clear();
                datesCreated.clear();
                datesModified.clear();
                hardcoreEarnings.clear();

                this.numDistinctCasual = reader.getString("NumDistinctPlayersCasual");

                JSONObject achievements = reader.getJSONObject("Achievements");
                JSONObject achievement;
                int count;
                List<Integer> displayOrder = new ArrayList<>();
                List<Integer> displayOrderEarned = new ArrayList<>();
                int totalAch = 0;
                for (Iterator<String> keys = achievements.keys(); keys.hasNext(); ) {
                    String achievementID = keys.next();
                    achievement = achievements.getJSONObject(achievementID);

                    // Set up ordering of achievements
                    String dateEarned = "";
                    if (achievement.has("DateEarnedHardcore")) {
                        dateEarned = achievement.getString("DateEarnedHardcore");
                        displayOrderEarned.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrderEarned);
                        count = displayOrderEarned.indexOf(Integer.parseInt(achievement.getString("DisplayOrder")));
                        hardcoreEarnings.put(achievementID, true);
                    } else if (achievement.has("DateEarned")) {
                        dateEarned = achievement.getString("DateEarned");
                        displayOrderEarned.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrderEarned);
                        count = displayOrderEarned.indexOf(Integer.parseInt(achievement.getString("DisplayOrder")));
                        hardcoreEarnings.put(achievementID, false);
                    } else {
                        displayOrder.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrder);
                        count = displayOrder.indexOf(Integer.parseInt(achievement.getString("DisplayOrder"))) + displayOrderEarned.size();
                    }
                    if (count == 0)
                        count = totalAch;

                    // Parse JSON for achievement info
                    ids.add(count, achievementID);
                    badges.add(count, achievement.getString("BadgeName"));
                    titles.add(count, achievement.getString("Title"));
                    points.add(count, achievement.getString("Points"));
                    trueRatios.add(count, achievement.getString("TrueRatio"));
                    descriptions.add(count, achievement.getString("Description"));
                    if (dateEarned.equals("")) {
                        dateEarned = "NoDate:" + count;
                        hardcoreEarnings.put(achievementID, false);
                    }
                    datesEarned.add(count, dateEarned);
                    numsAwarded.add(count, achievement.getString("NumAwarded"));
                    numsAwardedHC.add(count, achievement.getString("NumAwardedHardcore"));
                    authors.add(count, achievement.getString("Author"));
                    datesCreated.add(count, achievement.getString("DateCreated"));
                    datesModified.add(count, achievement.getString("DateModified"));

                    totalAch++;
                }
            } catch (JSONException e) {
                if (e.toString().contains("Value null at Achievements of type org.json.JSONObject$1 cannot be converted to JSONObject"))
                    Log.d(TAG, "This game has no achievements");
                else
                    e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            final AchievementAdapter adapter = (AchievementAdapter) adapterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
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
