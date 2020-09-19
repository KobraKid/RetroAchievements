package com.kobrakid.retroachievements.view.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.kobrakid.retroachievements.view.ui.AchievementDistributionFragment
import com.kobrakid.retroachievements.view.ui.AchievementSummaryFragment
import com.kobrakid.retroachievements.view.ui.GameCommentsFragment
import com.kobrakid.retroachievements.view.ui.GameImagesFragment

class GameDetailsPagerAdapter(fm: FragmentManager, private val gameID: String) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> AchievementSummaryFragment()
            1 -> AchievementDistributionFragment()
            2 -> GameImagesFragment()
            3 -> GameCommentsFragment()
            else -> AchievementSummaryFragment()
        }
        return fragment.apply { arguments = bundleOf("GameID" to gameID) }
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