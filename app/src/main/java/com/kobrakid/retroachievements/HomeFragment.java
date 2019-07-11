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
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RAAPIConnection apiConnection;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up API connection
        String ra_user = getArguments().getString(getString(R.string.ra_user));
        String ra_api_key = getArguments().getString(getString(R.string.ra_api_key));
        apiConnection = new RAAPIConnection(ra_user, ra_api_key, getContext());

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        RadioGroup radioGroup = (RadioGroup) getView().findViewById(R.id.radioGroup1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(i);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) {
                    // TODO (Testing)
                    TextView textView = getView().findViewById(R.id.home_text_view);
                    RAAPICallback callback = new RAAPICallback(textView);
                    String radioButtonName = checkedRadioButton.getText().toString();
                    if (radioButtonName.equals("GetTopTenUsers")) {
                        apiConnection.GetTopTenUsers(callback);
                    } else if (radioButtonName.equals("GetGameInfo")) {
                        apiConnection.GetGameInfo("3", callback);
                    } else if (radioButtonName.equals("GetGameInfoExtended")) {
                        apiConnection.GetGameInfoExtended("3", callback);
                    } else if (radioButtonName.equals("GetConsoleIDs")) {
                        apiConnection.GetConsoleIDs(callback);
                    } else if (radioButtonName.equals("GetGameList")) {
                        apiConnection.GetGameList("2", callback);
                    } else if (radioButtonName.equals("GetFeedFor")) {
                        apiConnection.GetFeedFor("KobraKid1337", 5, 0, callback);
                    } else if (radioButtonName.equals("GetUserRankAndScore")) {
                        apiConnection.GetUserRankAndScore("KobraKid1337", callback);
                    } else if (radioButtonName.equals("GetUserProgress")) {
                        apiConnection.GetUserProgress("KobraKid1337", "3", callback);
                    } else if (radioButtonName.equals("GetUserRecentlyPlayedGames")) {
                        apiConnection.GetUserRecentlyPlayedGames("KobraKid1337", 5, 0, callback);
                    } else if (radioButtonName.equals("GetUserSummary")) {
                        apiConnection.GetUserSummary("KobraKid1337", 5, callback);
                    } else if (radioButtonName.equals("GetGameInfoAndUserProgress")) {
                        apiConnection.GetGameInfoAndUserProgress("KobraKid1337", "3", callback);
                    } else if (radioButtonName.equals("GetAchievementsEarnedOnDay")) {
                        apiConnection.GetAchievementsEarnedOnDay("KobraKid1337", "2018-04-16", callback);
                    } else if (radioButtonName.equals("GetAchievementsEarnedBetween")) {
                        apiConnection.GetAchievementsEarnedBetween("KobraKid1337", new Date(118, 4, 15), new Date(118, 4, 20), callback);
                    } else {
                        textView.setText("Uh oh:\n" + radioButtonName);
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
