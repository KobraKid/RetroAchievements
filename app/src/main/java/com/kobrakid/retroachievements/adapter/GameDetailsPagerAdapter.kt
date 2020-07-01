package com.kobrakid.retroachievements.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kobrakid.retroachievements.fragment.AchievementDistributionFragment
import com.kobrakid.retroachievements.fragment.AchievementSummaryFragment
import com.kobrakid.retroachievements.fragment.GameCommentsFragment
import com.kobrakid.retroachievements.fragment.GameImagesFragment

class GameDetailsPagerAdapter(fm: FragmentManager, private val gameID: String) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putString("GameID", gameID)
        val fragment: Fragment = when (position) {
            0 -> AchievementSummaryFragment()
            1 -> AchievementDistributionFragment()
            2 -> GameImagesFragment()
            3 -> GameCommentsFragment()
            else -> AchievementSummaryFragment()
        }
        fragment.arguments = bundle
        return fragment
    }

    /**
     * @return The number of fragments we want to maintain
     */
    override fun getCount(): Int {
        return GAME_DETAILS_PAGES
    }

    companion object {
        const val GAME_DETAILS_PAGES = 4
    }

}