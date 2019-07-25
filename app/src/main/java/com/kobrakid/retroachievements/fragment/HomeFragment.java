package com.kobrakid.retroachievements.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HomeFragment extends Fragment implements RAAPICallback, View.OnClickListener {

    private RAAPIConnection apiConnection;
    // Only call API when the view is first started, or when the user asks for a manual refresh
    private boolean hasPopulatedGames = false, hasPopulatedMasteries = false;
    private boolean isActive = false;

    public HomeFragment() {
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
        apiConnection = ((MainActivity) getActivity()).apiConnection;
        hasPopulatedGames = false;

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        apiConnection.GetUserWebProfile(MainActivity.ra_user, HomeFragment.this);

        // Initialize user's home screen if they are logged in
        if (!hasPopulatedGames && MainActivity.ra_user != null) {
            Picasso.get()
                    .load("https://retroachievements.org/UserPic/" + MainActivity.ra_user + ".png")
                    .into((ImageView) getView().findViewById(R.id.home_profile_picture));
            apiConnection.GetUserSummary(MainActivity.ra_user, 3, HomeFragment.this);
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
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;

        JSONObject reader;

        if (!hasPopulatedMasteries && responseCode == RAAPIConnection.RESPONSE_GET_USER_WEB_PROFILE) {
            LinearLayout masteries = getActivity().findViewById(R.id.masteries);

            Document document = Jsoup.parse(response);
            Elements elements = document.select("div[class=trophyimage]");

            for (Element element : elements) {
                String gameID = element.selectFirst("a[href]").attr("href").substring(6);
                String imageIcon = element.selectFirst("img[src]").attr("src");
                ImageView imageView = new ImageView(getContext());
                Picasso.get()
                        .load("https://retroachievements.org" + imageIcon)
                        .into(imageView);
                masteries.addView(imageView);
                try {
                    imageView.setId(Integer.parseInt(gameID));
                } catch (NumberFormatException e) {
                    // TODO set up logging system
                    // Crash happens when parsing achievements for connecting account to FB
                    e.printStackTrace();
                }
                imageView.setOnClickListener(this);
            }
            masteries.setVisibility(View.VISIBLE);
            hasPopulatedMasteries = true;
        }
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_SUMMARY) {
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

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this.getActivity(), GameDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("GameID",
                "" + view.getId());
        intent.putExtras(extras);
        startActivity(intent);
    }
}