package com.kobrakid.retroachievements.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

public class SettingsFragment extends Fragment implements RAAPICallback {

    private RAAPIConnection apiConnection;

    private boolean isActive = false;

    public SettingsFragment() {
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

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        RadioGroup radioGroup = getView().findViewById(R.id.radioGroup1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = radioGroup.findViewById(i);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) {
                    // TODO (Testing)
                    TextView textView = getView().findViewById(R.id.settings_text_view);
                    RAAPICallback callback = SettingsFragment.this;
                    String radioButtonName = checkedRadioButton.getText().toString();
                    switch (radioButtonName) {
                        case "GetTopTenUsers":
                            apiConnection.GetTopTenUsers(callback);
                            break;
                        case "GetGameInfo":
                            apiConnection.GetGameInfo("3", callback);
                            break;
                        case "GetGameInfoExtended":
                            apiConnection.GetGameInfoExtended("3", callback);
                            break;
                        case "GetConsoleIDs":
                            apiConnection.GetConsoleIDs(callback);
                            break;
                        case "GetGameList":
                            apiConnection.GetGameList("2", callback);
                            break;
                        case "GetFeedFor":
                            apiConnection.GetFeedFor("KobraKid1337", 5, 0, callback);
                            break;
                        case "GetUserRankAndScore":
                            apiConnection.GetUserRankAndScore("KobraKid1337", callback);
                            break;
                        case "GetUserProgress":
                            apiConnection.GetUserProgress("KobraKid1337", "3", callback);
                            break;
                        case "GetUserRecentlyPlayedGames":
                            apiConnection.GetUserRecentlyPlayedGames("KobraKid1337", 5, 0, callback);
                            break;
                        case "GetUserSummary":
                            apiConnection.GetUserSummary("KobraKid1337", 5, callback);
                            break;
                        case "GetGameInfoAndUserProgress":
                            apiConnection.GetGameInfoAndUserProgress("KobraKid1337", "3", callback);
                            break;
                        case "GetAchievementsEarnedOnDay":
                            apiConnection.GetAchievementsEarnedOnDay("KobraKid1337", "2018-04-16", callback);
                            break;
                        case "GetAchievementsEarnedBetween":
                            apiConnection.GetAchievementsEarnedBetween("KobraKid1337", new Date(118, 4, 15), new Date(118, 4, 20), callback);
                            break;
                        case "GetLeaderboards":
                            apiConnection.GetLeaderboards(true, callback);
                            break;
                        case "GetUserWebProfile":
                            apiConnection.GetUserWebProfile("KobraKid1337", callback);
                            break;
                        default:
                            textView.setText("Uh oh:\n" + radioButtonName);
                            break;
                    }
                }
            }
        });
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
        if (responseCode == RAAPIConnection.RESPONSE_GET_USER_WEB_PROFILE) {
            Document document = Jsoup.parse(response);
            response = "";
            // Using Elements to get the Meta data
            Elements elements = document.select("div[class=trophyimage]");
            for (Element element : elements) {
                response += element.selectFirst("a[href]").attr("href") + "\n";
            }
        }
        ((TextView) getView().findViewById(R.id.settings_text_view)).setText(response);

    }

}
