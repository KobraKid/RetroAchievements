package com.kobrakid.retroachievements.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kobrakid.retroachievements.fragment.AchievementDistributionFragment;
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment;
import com.kobrakid.retroachievements.fragment.GameImagesFragment;

public class GameDetailsPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private final String gameID;

    public GameDetailsPagerAdapter(FragmentManager fm, Context context, String gameID) {
        super(fm);
        this.context = context;
        this.gameID = gameID;
    }

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
