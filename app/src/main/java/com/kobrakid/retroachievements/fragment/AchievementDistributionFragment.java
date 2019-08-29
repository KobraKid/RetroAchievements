package com.kobrakid.retroachievements.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.R;

public class AchievementDistributionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_achievement_distribution, container, false);

        GameDetailsActivity activity = (GameDetailsActivity) getActivity();
        if (activity != null) {
            activity.achievementDistroLoadingBar = view.findViewById(R.id.game_details_achievement_distro_loading);
            activity.achievementDistro = view.findViewById(R.id.game_details_achievement_distribution);
            activity.apiConnection.GetAchievementDistribution(activity.gameID, activity);
        }

        return view;
    }
}
