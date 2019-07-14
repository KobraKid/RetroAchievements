package com.kobrakid.retroachievements.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.RAAPICallback;
import com.kobrakid.retroachievements.RAAPIConnection;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LeaderboardsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LeaderboardsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeaderboardsFragment extends Fragment implements RAAPICallback {

    private OnFragmentInteractionListener mListener;
    private RAAPIConnection apiConnection;

    private boolean isActive = false;

    public LeaderboardsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LeaderboardsFragment.
     */
    public static LeaderboardsFragment newInstance(String param1, String param2) {
        return new LeaderboardsFragment();
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
        return inflater.inflate(R.layout.fragment_leaderboards, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        apiConnection.GetTopTenUsers(this);
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
        if (responseCode == RAAPIConnection.RESPONSE_GET_TOP_TEN_USERS) {
            ((TextView) getActivity().findViewById(R.id.leaderboards_text_view)).setText(response);
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
