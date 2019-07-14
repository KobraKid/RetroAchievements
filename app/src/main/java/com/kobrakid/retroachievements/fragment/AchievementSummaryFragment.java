package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.AchievementAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AchievementSummaryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AchievementSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AchievementSummaryFragment extends Fragment implements RAAPICallback {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private OnFragmentInteractionListener mListener;
    private RAAPIConnection apiConnection;
    private String gameID;
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
    private boolean isActive = false;

    public AchievementSummaryFragment() {
        // Required empty public constructor
    }

    public static AchievementSummaryFragment newInstance() {
        return new AchievementSummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        apiConnection = ((GameDetailsActivity) getActivity()).apiConnection;

        gameID = getArguments().getString("GameID");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_achievement_summary, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set up RecyclerView
        recyclerView = getActivity().findViewById(R.id.game_details_achievements_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
                "1");
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;

        apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, gameID, AchievementSummaryFragment.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
