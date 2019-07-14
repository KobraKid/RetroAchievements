package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements RAAPICallback {

    private OnFragmentInteractionListener mListener;
    private RAAPIConnection apiConnection;
    // Only call API when the view is first started, or when the user asks for a manual refresh
    private boolean hasPopulatedGames = false;
    private boolean isActive = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up API connection
        apiConnection = ((MainActivity) getActivity()).apiConnection;
        hasPopulatedGames = false;

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initialize user's home screen if they are logged in
        if (!hasPopulatedGames && MainActivity.ra_user != null) {
            Picasso.get()
                    .load("https://retroachievements.org/UserPic/" + MainActivity.ra_user + ".png")
                    .into((ImageView) getView().findViewById(R.id.home_profile_picture));
            apiConnection.GetUserSummary(MainActivity.ra_user, 5, HomeFragment.this);
            // TODO allow manual repopulation
            hasPopulatedGames = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
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
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_SUMMARY) {
            JSONObject reader;
            try {
                reader = new JSONObject(response);

                // Fill out user summary
                ((TextView) getView().findViewById(R.id.home_stats)).setText(getString(R.string.nav_rank_score,
                        reader.getString("TotalPoints"),
                        reader.getString("Rank")));
                ((TextView) getView().findViewById(R.id.home_username)).setText(MainActivity.ra_user);
                getView().findViewById(R.id.home_stats).setVisibility(View.VISIBLE);

                // Fill out recently played games list
                LinearLayout recentGames = getView().findViewById(R.id.home_recent_games);
                JSONArray recentlyPlayed = reader.getJSONArray("RecentlyPlayed");
                for (int i = 0; i < recentlyPlayed.length(); i++) {
                    LinearLayout game = (LinearLayout) View.inflate(getContext(), R.layout.view_holder_game_summary, null);
                    JSONObject gameObj = recentlyPlayed.getJSONObject(i);

                    // Image
                    String imageIcon = gameObj.getString("ImageIcon");
                    Picasso.get()
                            .load("https://retroachievements.org" + imageIcon)
                            .into((ImageView) game.findViewById(R.id.game_summary_image_icon));

                    // Title
                    String gameTitle = gameObj.getString("Title");
                    ((TextView) game.findViewById(R.id.game_summary_title)).setText(gameTitle);

                    // Awards/Score
                    String gameID = gameObj.getString("GameID");
                    JSONObject awards = reader.getJSONObject("Awarded").getJSONObject(gameID);
                    String possibleAchievements = awards.getString("NumPossibleAchievements");
                    String possibleScore = awards.getString("PossibleScore");
                    int awardedAchieve = Integer.parseInt(awards.getString("NumAchieved"));
                    int awardedAchieveHardcore = Integer.parseInt(awards.getString("NumAchievedHardcore"));
                    String score = awardedAchieve > awardedAchieveHardcore ? awards.getString("ScoreAchieved") : awards.getString("ScoreAchievedHardcore");
                    ((TextView) game.findViewById(R.id.game_summary_stats))
                            .setText(getResources().getString(R.string.game_stats,
                                    Integer.toString(awardedAchieve > awardedAchieveHardcore ? awardedAchieve : awardedAchieveHardcore),
                                    possibleAchievements,
                                    score,
                                    possibleScore));

                    // Game ID
                    ((TextView) game.findViewById(R.id.game_summary_game_id)).setText(gameID);

                    recentGames.addView(game, i);
                }

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
