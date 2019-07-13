package com.kobrakid.retroachievements;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements RAAPICallback {

    private OnFragmentInteractionListener mListener;
    private RAAPIConnection apiConnection;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
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
                        case "GetLeaderboardsList":
                            apiConnection.GetLeaderboardsList(callback);
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
        ((TextView) getView().findViewById(R.id.settings_text_view)).setText(response);
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
