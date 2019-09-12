package com.kobrakid.retroachievements.fragment;


import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.kobrakid.retroachievements.R;

import java.util.Objects;

/**
 * This fragment holds static info about the app.
 */
public class AboutFragment extends Fragment {

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).setTitle("About RetroAchievements");
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        for (int i = 0; i < ((ViewGroup) view.findViewById(R.id.about_container)).getChildCount(); i++) {
            if (((ViewGroup) view.findViewById(R.id.about_container)).getChildAt(i) instanceof TextView)
                ((TextView) ((ViewGroup) view.findViewById(R.id.about_container)).getChildAt(i))
                        .setMovementMethod(LinkMovementMethod.getInstance());
        }
        return view;
    }

}
