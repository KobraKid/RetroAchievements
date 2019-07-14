package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private String gameID;
    private String forumTopicID;
    private ArrayList<String>
            ids,
            badges,
            titles,
            points,
            trueRatios,
            descriptions,
            datesEarned,
            numsAwarded,
            numsAwardedHC;
    private String numDistinctCasual;
    RAAPIConnection apiConnection;

    private boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.login_key), Context.MODE_PRIVATE);
        String theme = sharedPref.getString(getString(R.string.theme_setting), "Blank");
        setTheme(ThemeToggler.getTheme(this, sharedPref));

        setContentView(R.layout.activity_game_details);

        apiConnection = new RAAPIConnection(MainActivity.ra_api_user, MainActivity.ra_api_key, this);

        // Set up title bar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        gameID = getIntent().getExtras().getString("GameID");

        // Set up RecyclerView
        recyclerView = findViewById(R.id.game_details_achievements_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ids = new ArrayList<>();
        badges = new ArrayList<>();
        titles = new ArrayList<>();
        points = new ArrayList<>();
        trueRatios = new ArrayList<>();
        descriptions = new ArrayList<>();
        datesEarned = new ArrayList<>();
        numsAwarded = new ArrayList<>();
        numsAwardedHC = new ArrayList<>();

        adapter = new AchievementAdapter(
                ids,
                badges,
                titles,
                points,
                trueRatios,
                descriptions,
                datesEarned,
                numsAwarded,
                numsAwardedHC);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, gameID, GameDetailsActivity.this);
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

                ids.clear();
                badges.clear();
                titles.clear();
                points.clear();
                trueRatios.clear();
                descriptions.clear();
                datesEarned.clear();
                numsAwarded.clear();
                numsAwardedHC.clear();

                setTitle(reader.getString("Title") + " (" + reader.getString("ConsoleName") + ")");
                Picasso.get()
                        .load("https://retroachievements.org" + reader.getString("ImageIcon"))
                        .into((ImageView) findViewById(R.id.game_details_image_icon));
                ((TextView) findViewById(R.id.game_details_developer)).setText(getString(R.string.developed, reader.getString("Developer")));
                ((TextView) findViewById(R.id.game_details_publisher)).setText(getString(R.string.published, reader.getString("Publisher")));
                ((TextView) findViewById(R.id.game_details_genre)).setText(getString(R.string.genre, reader.getString("Genre")));
                ((TextView) findViewById(R.id.game_details_release_date)).setText(getString(R.string.released, reader.getString("Released")));

                forumTopicID = reader.getString("ForumTopicID");
                numDistinctCasual = reader.getString("NumDistinctPlayersCasual");

                JSONObject achievements = reader.getJSONObject("Achievements");
                JSONObject achievement;
                int count;
                List<Integer> displayOrder = new ArrayList<>();
                List<Integer> displayOrderEarned = new ArrayList<>();
                int numEarned = 0, numEarnedHC = 0, totalAch = 0, earnedPts = 0, totalPts = 0, earnedRatio = 0, totalRatio = 0;
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
                        numEarned++;
                        numEarnedHC++;
                        earnedPts += 2 * Integer.parseInt(achievement.getString("Points"));
                        earnedRatio += Integer.parseInt(achievement.getString("TrueRatio"));
                    } else if (achievement.has("DateEarned")) {
                        dateEarned = achievement.getString("DateEarned");
                        displayOrderEarned.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrderEarned);
                        count = displayOrderEarned.indexOf(Integer.parseInt(achievement.getString("DisplayOrder")));
                        numEarned++;
                        earnedPts += Integer.parseInt(achievement.getString("Points"));
                        earnedRatio += Integer.parseInt(achievement.getString("TrueRatio"));
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
                    }
                    datesEarned.add(count, dateEarned);
                    numsAwarded.add(count, achievement.getString("NumAwarded"));
                    numsAwardedHC.add(count, achievement.getString("NumAwardedHardcore"));

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

                adapter.notifyDataSetChanged();
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

    public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

        private ArrayList<String>
                ids,
                badges,
                titles,
                points,
                trueRatios,
                descriptions,
                datesEarned,
                numsAwarded,
                numsAwardedHC;

        class AchievementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            LinearLayout linearLayout;

            AchievementViewHolder(LinearLayout l) {
                super(l);
                linearLayout = l;
                linearLayout.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                // TODO Turn popup-window into much prettier "shared element" transition to new fragment
                LayoutInflater layoutInflater = getLayoutInflater();
                View achievementDetailView = layoutInflater.inflate(R.layout.achievement_details, null);
                final PopupWindow popupWindow = new PopupWindow(achievementDetailView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setElevation(5.0f);
                achievementDetailView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });
                int position = ids.indexOf(((TextView) view.findViewById(R.id.achievement_summary_id)).getText().toString());
                Picasso.get()
                        .load("https://retroachievements.org/Badge/" + badges.get(position) + ".png")
                        .into(((ImageView) achievementDetailView.findViewById(R.id.achievement_details_badge)));
                ((TextView) achievementDetailView.findViewById(R.id.achievement_details_title))
                        .setText(titles.get(position));
                ((TextView) achievementDetailView.findViewById(R.id.achievement_details_points))
                        .setText(points.get(position) + "\n(" + trueRatios.get(position) + ")");
                ((TextView) achievementDetailView.findViewById(R.id.achievement_details_description))
                        .setText(descriptions.get(position));
                popupWindow.showAtLocation(findViewById(R.id.game_details_achievements_recycler_view), Gravity.CENTER, 0, 0);
            }
        }

        AchievementAdapter(ArrayList<String> ids,
                           ArrayList<String> badges,
                           ArrayList<String> titles,
                           ArrayList<String> points,
                           ArrayList<String> trueRatios,
                           ArrayList<String> descriptions,
                           ArrayList<String> datesEarned,
                           ArrayList<String> numsAwarded,
                           ArrayList<String> numsAwardedHC) {
            this.ids = ids;
            this.badges = badges;
            this.titles = titles;
            this.points = points;
            this.trueRatios = trueRatios;
            this.descriptions = descriptions;
            this.datesEarned = datesEarned;
            this.numsAwarded = numsAwarded;
            this.numsAwardedHC = numsAwardedHC;

        }

        @NonNull
        @Override
        public AchievementAdapter.AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.achievement_summary,
                            parent,
                            false);
            return new AchievementViewHolder(linearLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
            // Badge
            Picasso.get()
                    .load("https://retroachievements.org/Badge/" + badges.get(position) + ".png")
                    .into((ImageView) holder.linearLayout.findViewById(R.id.achievement_summary_badge));

            // Text descriptions
            ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_id))
                    .setText(ids.get(position));
            ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_title))
                    .setText(titles.get(position) + " (" + points.get(position) + ") (" + trueRatios.get(position) + ")");
            ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_desc))
                    .setText(descriptions.get(position));
            if (datesEarned.get(position).startsWith("NoDate")) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ((ImageView) holder.linearLayout.findViewById(R.id.achievement_summary_badge)).setColorFilter(new ColorMatrixColorFilter(matrix));
                ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_date))
                        .setText("");
            } else {
                ((ImageView) holder.linearLayout.findViewById(R.id.achievement_summary_badge)).clearColorFilter();
                ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_date))
                        .setText(getString(R.string.date_earned, datesEarned.get(position)));
            }
            ((TextView) holder.linearLayout.findViewById(R.id.achievement_summary_stats))
                    .setText(getString(R.string.won_by,
                            numsAwarded.get(position),
                            numsAwardedHC.get(position),
                            numDistinctCasual,
                            new DecimalFormat("@@@@")
                                    .format(Double.parseDouble(numsAwarded.get(position)) / Double.parseDouble(numDistinctCasual) * 100.0)));

            // Double-layered Progress Bar
            ProgressBar progressBar = holder.linearLayout.findViewById(R.id.achievement_summary_progress);
            progressBar.setProgress((int) (Double.parseDouble(numsAwardedHC.get(position)) / Double.parseDouble(numDistinctCasual) * 10000.0));
            progressBar.setSecondaryProgress((int) (Double.parseDouble(numsAwarded.get(position)) / Double.parseDouble(numDistinctCasual) * 10000.0));
        }

        @Override
        public int getItemCount() {
            return ids.size();
        }

    }

}
