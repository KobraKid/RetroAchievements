package com.kobrakid.retroachievements.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.AchievementAdapter;
import com.kobrakid.retroachievements.manager.AchievementLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for displaying summary information on all the achievements for a
 * particular game.
 */
public class AchievementSummaryFragment extends Fragment implements RAAPICallback {

    private static final String TAG = AchievementSummaryFragment.class.getName();

    private RAAPIConnection apiConnection = null;
    private String gameID = null;
    private String gameInfoAndUserProgressResponse = "";
    private ArrayList<String>
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
            datesModified;
    private Map<String, Boolean> hardcoreEarnings;
    private boolean isActive = false;

    private RecyclerView recyclerView = null;
    private RecyclerView.Adapter adapter = null;
    public RecyclerView.LayoutManager layoutManager = null;

    public AchievementSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up API connection
        if (apiConnection == null)
            apiConnection = ((GameDetailsActivity) getActivity()).apiConnection;

        // Set up arguments
        if (gameID == null && getArguments().containsKey("GameID"))
            gameID = getArguments().getString("GameID");

        View rootView = inflater.inflate(R.layout.fragment_achievement_summary, container, false);

        // Set up Achievements RecyclerView
        recyclerView = rootView.findViewById(R.id.game_details_achievements_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new AchievementLayoutManager(getActivity());
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
        authors = new ArrayList<>();
        datesCreated = new ArrayList<>();
        datesModified = new ArrayList<>();
        hardcoreEarnings = new HashMap<>();

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
        recyclerView.setAdapter(adapter);

        // Set up animations
        prepareTransitions();
        postponeEnterTransition();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startPostponedEnterTransition();
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;

        if (gameInfoAndUserProgressResponse.equals("")) {
            apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, gameID, AchievementSummaryFragment.this);
        } else {
            parseResponse();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO_AND_USER_PROGRESS) {
            gameInfoAndUserProgressResponse = response;
            parseResponse();
            getActivity().findViewById(R.id.game_details_loading_bar).setVisibility(View.GONE);
        }
    }

    private void parseResponse() {
        JSONObject reader;
        try {
            reader = new JSONObject(gameInfoAndUserProgressResponse);

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

            ((AchievementAdapter) adapter).numDistinctCasual = reader.getString("NumDistinctPlayersCasual");

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
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            if (e.toString().contains("Value null at Achievements of type org.json.JSONObject$1 cannot be converted to JSONObject"))
                Log.d(TAG, "This game has no achievements");
            else
                e.printStackTrace();
        }
    }

    private void prepareTransitions() {
//        setExitSharedElementCallback(new SharedElementCallback() {
//            @Override
//            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                sharedElements.put(names.get(0),
//                        recyclerView.findViewHolderForAdapterPosition(GameDetailsActivity.currentPosition)
//                                .itemView
//                                .findViewById(R.id.achievement_summary_badge));
//            }
//        });
    }

}
