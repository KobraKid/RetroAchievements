package com.kobrakid.retroachievements.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kobrakid.retroachievements.fragment.AchievementDistributionFragment;
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment;
import com.kobrakid.retroachievements.fragment.GameImagesFragment;

public class GameDetailsPagerAdapter extends FragmentPagerAdapter {

    private final String gameID;

    public GameDetailsPagerAdapter(FragmentManager fm, String gameID) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.gameID = gameID;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putString("GameID", gameID);
        switch (position) {
            case 0:
            default:
                fragment = new AchievementSummaryFragment();
                break;
            case 1:
                fragment = new AchievementDistributionFragment();
                break;
            case 2:
                fragment = new GameImagesFragment();
                break;
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

}
