package com.kobrakid.retroachievements.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.kobrakid.retroachievements.Consts;
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

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment implements RAAPICallback, View.OnClickListener {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private RAAPIConnection apiConnection;
    // TODO Only call API when the view is first started, or when the user asks for a manual refresh
    private boolean hasPopulatedGames, hasPopulatedMasteries;
    private ArrayList<String> masteryIDs, masteryIcons, summaryIDs, summaryTitles, summaryIcons, summaryScores;
    private ArrayList<Boolean> masteryGold;
    String score, rank;
    private boolean isActive = false;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Objects.requireNonNull(getActivity()).setTitle("Home");

        if (savedInstanceState == null) {
            // Set up API connection
            apiConnection = ((MainActivity) Objects.requireNonNull(getActivity())).apiConnection;
            hasPopulatedGames = false;
            hasPopulatedMasteries = false;
            apiConnection.GetUserWebProfile(MainActivity.ra_user, HomeFragment.this);
            apiConnection.GetUserSummary(MainActivity.ra_user, 3, HomeFragment.this);
        } else {
            populateMasteries(view);
            populateGames(view);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;
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
    public void onClick(View view) {
        Intent intent = new Intent(this.getActivity(), GameDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("GameID",
                "" + view.getId());
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void callback(int responseCode, String response) {
        if (!isActive)
            return;
        JSONObject reader;
        if (!hasPopulatedMasteries && responseCode == RAAPIConnection.RESPONSE_GET_USER_WEB_PROFILE) {
            Document document = Jsoup.parse(response);
            Elements elements = document.select("div[class=trophyimage]");

            masteryIDs = new ArrayList<>();
            masteryIcons = new ArrayList<>();
            masteryGold = new ArrayList<>();

            for (Element element : elements) {
                String gameID = element.selectFirst("a[href]").attr("href");
                if (gameID.length() >= 6) {
                    gameID = gameID.substring(6);
                    Element image = element.selectFirst("img[src]");
                    String imageIcon = image.attr("src");
                    masteryIDs.add(gameID);
                    masteryIcons.add(imageIcon);
                    masteryGold.add(image.className().equals("goldimage"));
                }
            }
            if (getView() != null)
                populateMasteries(getView());
        }
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_SUMMARY) {
            try {
                reader = new JSONObject(response);

                summaryIDs = new ArrayList<>();
                summaryIcons = new ArrayList<>();
                summaryTitles = new ArrayList<>();
                summaryScores = new ArrayList<>();

                score = reader.getString("TotalPoints");
                rank = reader.getString("Rank");

                JSONArray recentlyPlayed = reader.getJSONArray("RecentlyPlayed");
                for (int i = 0; i < recentlyPlayed.length(); i++) {
                    JSONObject gameObj = recentlyPlayed.getJSONObject(i);
                    String gameID = gameObj.getString("GameID");
                    JSONObject awards = reader.getJSONObject("Awarded").getJSONObject(gameID);

                    String possibleAchievements = awards.getString("NumPossibleAchievements");
                    String possibleScore = awards.getString("PossibleScore");
                    int awardedAchieve = Integer.parseInt(awards.getString("NumAchieved"));
                    int awardedAchieveHardcore = Integer.parseInt(awards.getString("NumAchievedHardcore"));
                    String score = awardedAchieve > awardedAchieveHardcore ? awards.getString("ScoreAchieved") : awards.getString("ScoreAchievedHardcore");

                    summaryIDs.add(gameID);
                    summaryIcons.add(gameObj.getString("ImageIcon"));
                    summaryTitles.add(gameObj.getString("Title"));
                    summaryScores.add(getResources().getString(R.string.game_stats,
                            Integer.toString(awardedAchieve > awardedAchieveHardcore ? awardedAchieve : awardedAchieveHardcore),
                            possibleAchievements,
                            score,
                            possibleScore));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            populateGames(getView());
        }
    }

    private void populateMasteries(View view) {
        LinearLayout masteries = view.findViewById(R.id.masteries);
        masteries.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMarginEnd(1);

        for (int i = 0; i < masteryIDs.size(); i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(params);
            imageView.setAdjustViewBounds(true);
            if (masteryGold.get(i))
                imageView.setBackground(Objects.requireNonNull(getActivity()).getDrawable(R.drawable.image_view_border));
            Picasso.get()
                    .load(Consts.BASE_URL + masteryIcons.get(i))
                    .placeholder(R.drawable.favicon)
                    .into(imageView);
            masteries.addView(imageView);
            try {
                imageView.setId(Integer.parseInt(masteryIDs.get(i)));
            } catch (NumberFormatException e) {
                // TODO set up logging system
                // This happens when parsing achievements like connecting one's account to FB,
                // developing achievements, etc.
                Log.e(TAG, "Trophy was not a valid RA game.", e);
            }
            imageView.setOnClickListener(this);
        }

        masteries.setVisibility(View.VISIBLE);
        hasPopulatedMasteries = true;
    }

    private void populateGames(View view) {
        ((TextView) view.findViewById(R.id.home_username)).setText(MainActivity.ra_user);
        ((TextView) view.findViewById(R.id.home_stats)).setText(getString(R.string.nav_rank_score, score, rank));
        if (MainActivity.ra_user != null) {
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + MainActivity.ra_user + ".png")
                    .into((ImageView) view.findViewById(R.id.home_profile_picture));
        }
        view.findViewById(R.id.home_view_more).setVisibility(View.VISIBLE);
        view.findViewById(R.id.home_username).setVisibility(View.VISIBLE);
        view.findViewById(R.id.home_stats).setVisibility(View.VISIBLE);

        // Fill out recently played games list
        LinearLayout recentGames = view.findViewById(R.id.home_recent_games);
        if (recentGames.getChildCount() > 1)
            recentGames.removeViews(0, recentGames.getChildCount() - 1);
        for (int i = 0; i < summaryIDs.size(); i++) {
            ConstraintLayout game = (ConstraintLayout) View.inflate(getContext(), R.layout.view_holder_game_summary, null);
            Picasso.get()
                    .load(Consts.BASE_URL + summaryIcons.get(i))
                    .into((ImageView) game.findViewById(R.id.game_summary_image_icon));
            ((TextView) game.findViewById(R.id.game_summary_title)).setText(summaryTitles.get(i));
            ((TextView) game.findViewById(R.id.game_summary_stats)).setText(summaryScores.get(i));
            ((TextView) game.findViewById(R.id.game_summary_game_id)).setText(summaryIDs.get(i));
            recentGames.addView(game, i);
        }

        hasPopulatedGames = true;
    }

}
