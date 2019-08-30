package com.kobrakid.retroachievements.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.AchievementAdapter;
import com.kobrakid.retroachievements.manager.AchievementLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class is responsible for displaying summary information on all the achievements for a
 * particular game.
 */
public class AchievementSummaryFragment extends Fragment implements RAAPICallback {

    private static final String TAG = AchievementSummaryFragment.class.getSimpleName();

    private RecyclerView.Adapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    private final ArrayList<String>
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
    private final StringBuilder numDistinctCasual = new StringBuilder("1");
    private boolean isActive = false;

    public AchievementSummaryFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_achievements_summary, container, false);

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
                numDistinctCasual);

        RecyclerView recyclerView = view.findViewById(R.id.game_details_achievements_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new AchievementLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        new RAAPIConnection(getContext()).GetGameInfoAndUserProgress(MainActivity.ra_user, Objects.requireNonNull(getArguments()).getString("GameID"), this);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS) {
            try {
                JSONObject reader = new JSONObject(response);
                numDistinctCasual.delete(0, numDistinctCasual.length());
                numDistinctCasual.append(reader.getString("NumDistinctPlayersCasual"));
                if (reader.getString("NumAchievements").equals("0")) {
                    Objects.requireNonNull(getView()).findViewById(R.id.game_details_loading_bar).setVisibility(View.GONE);
                    getView().findViewById(R.id.game_details_no_achievements).setVisibility(View.VISIBLE);
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

                    ((TextView) Objects.requireNonNull(getView()).findViewById(R.id.game_details_progress_text))
                            .setText(getString(
                                    R.string.completion,
                                    new DecimalFormat("@@@@")
                                            .format(((float) (numEarned + numEarnedHC) / (float) totalAch) * 100.0)));
                    ((ProgressBar) getView().findViewById(R.id.game_details_progress)).setProgress((int) (((float) numEarned) / ((float) totalAch) * 10000.0));
                    ((TextView) getView().findViewById(R.id.game_details_user_summary))
                            .setText(Html.fromHtml(getString(
                                    R.string.user_summary,
                                    numEarned,
                                    totalAch,
                                    numEarnedHC,
                                    earnedPts,
                                    earnedRatio,
                                    totalPts * 2, // Account for hardcore achievements worth double
                                    totalRatio)));
                    getView().findViewById(R.id.game_details_progress).setVisibility(View.VISIBLE);

                    new AchievementDetailsAsyncTask(
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
                            hardcoreEarnings).execute(response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static class AchievementDetailsAsyncTask extends AsyncTask<String, Integer, String[]> {

        final WeakReference<Fragment> fragmentReference;
        final WeakReference<ArrayList<String>> idsReference;
        final WeakReference<ArrayList<String>> badgesReference;
        final WeakReference<ArrayList<String>> titlesReference;
        final WeakReference<ArrayList<String>> pointsReference;
        final WeakReference<ArrayList<String>> trueRatiosReference;
        final WeakReference<ArrayList<String>> descriptionsReference;
        final WeakReference<ArrayList<String>> datesEarnedReference;
        final WeakReference<ArrayList<String>> numsAwardedReference;
        final WeakReference<ArrayList<String>> numsAwardedHCReference;
        final WeakReference<ArrayList<String>> authorsReference;
        final WeakReference<ArrayList<String>> datesCreatedReference;
        final WeakReference<ArrayList<String>> datesModifiedReference;
        final WeakReference<Map<String, Boolean>> hardcoreEarningsReference;
        private final ArrayList<String>
                asyncIds = new ArrayList<>();
        private final ArrayList<String> asyncBadges = new ArrayList<>();
        private final ArrayList<String> asyncTitles = new ArrayList<>();
        private final ArrayList<String> asyncPoints = new ArrayList<>();
        private final ArrayList<String> asyncTrueRatios = new ArrayList<>();
        private final ArrayList<String> asyncDescriptions = new ArrayList<>();
        private final ArrayList<String> asyncDatesEarned = new ArrayList<>();
        private final ArrayList<String> asyncNumsAwarded = new ArrayList<>();
        private final ArrayList<String> asyncNumsAwardedHC = new ArrayList<>();
        private final ArrayList<String> asyncAuthors = new ArrayList<>();
        private final ArrayList<String> asyncDatesCreated = new ArrayList<>();
        private final ArrayList<String> asyncDatesModified = new ArrayList<>();
        private final Map<String, Boolean> asyncHardcoreEarnings = new HashMap<>();

        AchievementDetailsAsyncTask(Fragment fragment,
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
            this.fragmentReference = new WeakReference<>(fragment);
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
            try {
                JSONObject reader = new JSONObject(strings[0]);

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
                        asyncHardcoreEarnings.put(achievementID, true);
                    } else if (achievement.has("DateEarned")) {
                        dateEarned = achievement.getString("DateEarned");
                        displayOrderEarned.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrderEarned);
                        count = displayOrderEarned.indexOf(Integer.parseInt(achievement.getString("DisplayOrder")));
                        asyncHardcoreEarnings.put(achievementID, false);
                    } else {
                        displayOrder.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrder);
                        count = displayOrder.indexOf(Integer.parseInt(achievement.getString("DisplayOrder"))) + displayOrderEarned.size();
                    }
                    if (count == 0)
                        count = totalAch;

                    // Parse JSON for achievement info
                    asyncIds.add(count, achievementID);
                    asyncBadges.add(count, achievement.getString("BadgeName"));
                    asyncTitles.add(count, achievement.getString("Title"));
                    asyncPoints.add(count, achievement.getString("Points"));
                    asyncTrueRatios.add(count, achievement.getString("TrueRatio"));
                    asyncDescriptions.add(count, achievement.getString("Description"));
                    if (dateEarned.equals("")) {
                        dateEarned = "NoDate:" + count;
                        asyncHardcoreEarnings.put(achievementID, false);
                    }
                    asyncDatesEarned.add(count, dateEarned);
                    asyncNumsAwarded.add(count, achievement.getString("NumAwarded"));
                    asyncNumsAwardedHC.add(count, achievement.getString("NumAwardedHardcore"));
                    asyncAuthors.add(count, achievement.getString("Author"));
                    asyncDatesCreated.add(count, achievement.getString("DateCreated"));
                    asyncDatesModified.add(count, achievement.getString("DateModified"));

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
            final AchievementSummaryFragment fragment = (AchievementSummaryFragment) fragmentReference.get();
            if (fragment != null && fragment.isActive) {
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

                ids.addAll(asyncIds);
                badges.addAll(asyncBadges);
                titles.addAll(asyncTitles);
                points.addAll(asyncPoints);
                trueRatios.addAll(asyncTrueRatios);
                descriptions.addAll(asyncDescriptions);
                datesEarned.addAll(asyncDatesEarned);
                numsAwarded.addAll(asyncNumsAwarded);
                numsAwardedHC.addAll(asyncNumsAwardedHC);
                authors.addAll(asyncAuthors);
                datesCreated.addAll(asyncDatesCreated);
                datesModified.addAll(asyncDatesModified);
                hardcoreEarnings.putAll(asyncHardcoreEarnings);

                Objects.requireNonNull(fragment.getView()).findViewById(R.id.game_details_loading_bar).setVisibility(View.GONE);
                fragment.getView().findViewById(R.id.game_details_achievements_recycler_view).setVisibility(View.VISIBLE);
                fragment.adapter.notifyDataSetChanged();
            }
        }
    }
}
