package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.kobrakid.retroachievements.adapter.AchievementAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
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
    private RecyclerView.LayoutManager layoutManager;

    private OnFragmentInteractionListener mListener;
    private RAAPIConnection apiConnection;
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
        return inflater.inflate(R.layout.fragment_game_details_uesr_summary, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set up RecyclerView
        recyclerView = getView().findViewById(R.id.game_details_achievements_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
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
        numDistinctCasual = "1";

        adapter = new AchievementAdapter(
                getContext(),
                ids,
                badges,
                titles,
                points,
                trueRatios,
                descriptions,
                datesEarned,
                numsAwarded,
                numsAwardedHC,
                numDistinctCasual);
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

                getActivity().setTitle(reader.getString("Title") + " (" + reader.getString("ConsoleName") + ")");
                Picasso.get()
                        .load("https://retroachievements.org" + reader.getString("ImageIcon"))
                        .into((ImageView) getView().findViewById(R.id.game_details_image_icon));
                ((TextView) getView().findViewById(R.id.game_details_developer)).setText(getString(R.string.developed, reader.getString("Developer")));
                ((TextView) getView().findViewById(R.id.game_details_publisher)).setText(getString(R.string.published, reader.getString("Publisher")));
                ((TextView) getView().findViewById(R.id.game_details_genre)).setText(getString(R.string.genre, reader.getString("Genre")));
                ((TextView) getView().findViewById(R.id.game_details_release_date)).setText(getString(R.string.released, reader.getString("Released")));

                forumTopicID = reader.getString("ForumTopicID");
                ((AchievementAdapter) adapter).numDistinctCasual = reader.getString("NumDistinctPlayersCasual");

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

                ((TextView) getView().findViewById(R.id.game_details_progress_text))
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
                                totalPts,
                                totalRatio)));

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
