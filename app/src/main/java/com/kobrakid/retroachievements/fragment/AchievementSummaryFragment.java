package com.kobrakid.retroachievements.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.AchievementAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This class is responsible for displaying summary information on all the achievements for a
 * particular game.
 */
public class AchievementSummaryFragment extends Fragment implements RAAPICallback {

    private static final String TAG = AchievementSummaryFragment.class.getSimpleName();

    private AchievementAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    private int numEarned, numEarnedHC, totalAch, earnedPts, totalPts, earnedRatio, totalRatio;

    public AchievementSummaryFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        View view = inflater.inflate(R.layout.view_pager_achievements_summary, container, false);

        if (savedInstanceState == null) {
            adapter = new AchievementAdapter(this);
        }
        RecyclerView recyclerView = view.findViewById(R.id.game_details_achievements_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        if (savedInstanceState == null && getArguments() != null) {
            new RAAPIConnection(Objects.requireNonNull(getContext())).GetGameInfoAndUserProgress(MainActivity.ra_user, getArguments().getString("GameID"), this);
        } else {
            populateViews(view);
        }
        return view;
    }

    @Override
    public void callback(int responseCode, String response) {
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS) {
            try {
                JSONObject reader = new JSONObject(response);
                adapter.setNumDistinctCasual(Double.parseDouble(reader.getString("NumDistinctPlayersCasual")));
                if (reader.getString("NumAchievements").equals("0")) {
                    if (getView() != null) {
                        getView().findViewById(R.id.game_details_loading_bar).setVisibility(View.GONE);
                        getView().findViewById(R.id.game_details_no_achievements).setVisibility(View.VISIBLE);
                    }
                } else {
                    JSONObject achievements = reader.getJSONObject("Achievements");
                    JSONObject achievement;
                    numEarned = 0;
                    numEarnedHC = 0;
                    totalAch = 0;
                    earnedPts = 0;
                    totalPts = 0;
                    earnedRatio = 0;
                    totalRatio = 0;
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
                    adapter.clear();
                    new AchievementDetailsAsyncTask(this).execute(response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateViews(View view) {
        ((TextView) view.findViewById(R.id.game_details_progress_text))
                .setText(getString(
                        R.string.completion,
                        new DecimalFormat("@@@@")
                                .format(((float) (numEarned + numEarnedHC) / (float) totalAch) * 100.0)));
        ((TextView) view.findViewById(R.id.game_details_user_summary))
                .setText(Html.fromHtml(getString(
                        R.string.user_summary,
                        numEarned,
                        totalAch,
                        numEarnedHC,
                        earnedPts,
                        earnedRatio,
                        totalPts * 2, // Account for hardcore achievements worth double
                        totalRatio)));

        ((ProgressBar) view.findViewById(R.id.game_details_progress)).
                setProgress((int) (((float) numEarned) / ((float) totalAch) * 10000.0));

        view.findViewById(R.id.game_details_progress).setVisibility(View.VISIBLE);
        view.findViewById(R.id.game_details_loading_bar).setVisibility(View.GONE);
        view.findViewById(R.id.game_details_achievements_recycler_view).setVisibility(View.VISIBLE);
    }

    private static class AchievementDetailsAsyncTask extends AsyncTask<String, Object, String[]> {

        final WeakReference<Fragment> fragmentReference;

        AchievementDetailsAsyncTask(Fragment fragment) {
            this.fragmentReference = new WeakReference<>(fragment);
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
                    boolean earnedHC = false;
                    if (achievement.has("DateEarnedHardcore")) {
                        dateEarned = achievement.getString("DateEarnedHardcore");
                        displayOrderEarned.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrderEarned);
                        count = displayOrderEarned.indexOf(Integer.parseInt(achievement.getString("DisplayOrder")));
                        earnedHC = true;
                    } else if (achievement.has("DateEarned")) {
                        dateEarned = achievement.getString("DateEarned");
                        displayOrderEarned.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrderEarned);
                        count = displayOrderEarned.indexOf(Integer.parseInt(achievement.getString("DisplayOrder")));
                    } else {
                        displayOrder.add(Integer.parseInt(achievement.getString("DisplayOrder")));
                        Collections.sort(displayOrder);
                        count = displayOrder.indexOf(Integer.parseInt(achievement.getString("DisplayOrder"))) + displayOrderEarned.size();
                    }
                    if (dateEarned.equals("")) {
                        dateEarned = "NoDate:" + count;
                        earnedHC = false;
                    }
                    if (count == 0)
                        count = totalAch;

                    publishProgress(
                            count,
                            achievementID,
                            achievement.getString("BadgeName"),
                            achievement.getString("Title"),
                            achievement.getString("Points"),
                            achievement.getString("TrueRatio"),
                            achievement.getString("Description"),
                            dateEarned,
                            earnedHC,
                            achievement.getString("NumAwarded"),
                            achievement.getString("NumAwardedHardcore"),
                            achievement.getString("Author"),
                            achievement.getString("DateCreated"),
                            achievement.getString("DateModified"));

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
        protected void onProgressUpdate(Object... values) {
            final AchievementSummaryFragment fragment = (AchievementSummaryFragment) fragmentReference.get();
            if (fragment != null) {
                fragment.adapter.addAchievement(
                        (int) values[0],
                        (String) values[1],
                        (String) values[2],
                        (String) values[3],
                        (String) values[4],
                        (String) values[5],
                        (String) values[6],
                        (String) values[7],
                        (boolean) values[8],
                        (String) values[9],
                        (String) values[10],
                        (String) values[11],
                        (String) values[12],
                        (String) values[13]);
            }
        }

        @Override
        protected void onPostExecute(String[] strings) {
            final AchievementSummaryFragment fragment = (AchievementSummaryFragment) fragmentReference.get();
            if (fragment != null && fragment.getView() != null)
                fragment.populateViews(fragment.getView());
        }
    }
}
