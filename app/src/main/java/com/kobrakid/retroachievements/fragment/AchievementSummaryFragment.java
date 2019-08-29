package com.kobrakid.retroachievements.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kobrakid.retroachievements.GameDetailsActivity;
import com.kobrakid.retroachievements.MainActivity;
import com.kobrakid.retroachievements.R;
import com.kobrakid.retroachievements.adapter.AchievementAdapter;
import com.kobrakid.retroachievements.manager.AchievementLayoutManager;

/**
 * This class is responsible for displaying summary information on all the achievements for a
 * particular game.
 */
public class AchievementSummaryFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_achievements_summary, container, false);

        final GameDetailsActivity activity = ((GameDetailsActivity) getActivity());

        if (activity != null) {
            activity.adapter = new AchievementAdapter(
                    this,
                    activity.ids,
                    activity.badges,
                    activity.titles,
                    activity.points,
                    activity.trueRatios,
                    activity.descriptions,
                    activity.datesEarned,
                    activity.numsAwarded,
                    activity.numsAwardedHC,
                    activity.authors,
                    activity.datesCreated,
                    activity.datesModified,
                    activity.hardcoreEarnings,
                    "1");

            activity.recyclerView = view.findViewById(R.id.game_details_achievements_recycler_view);
            activity.recyclerView.setHasFixedSize(true);
            activity.layoutManager = new AchievementLayoutManager(activity);
            activity.recyclerView.setLayoutManager(activity.layoutManager);
            activity.recyclerView.setAdapter(activity.adapter);

            activity.apiConnection.GetGameInfoAndUserProgress(MainActivity.ra_user, activity.gameID, activity);
        }

        return view;
    }

}
