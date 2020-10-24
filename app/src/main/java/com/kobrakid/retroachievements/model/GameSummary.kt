package com.kobrakid.retroachievements.model

import androidx.core.text.isDigitsOnly

/**
 * Represents a game's summary information.
 * Summarizes the current user's progress towards this game.
 *
 * @property id The game's id
 * @property title The game's title
 * @property imageIcon The game's image icon
 * @property numDistinctCasual The number of distinct casual players that have attempted this game
 * @property numAchievementsEarned The number of achievements the user has earned
 * @property numAchievementsEarnedHC The numeber of achievements the user has earned in hardcore mode
 * @property totalAchievements The total number of achievements this game has
 * @property earnedPoints The number of points the user has earned
 * @property totalPoints The total number of points this game is worth
 * @property earnedTruePoints The number of true points the user has earned
 * @property totalTruePoints The total number of true points this game is worth
 */
data class GameSummary(
        var id: String = "0",
        var title: String = "",
        var imageIcon: String = "",
        var numDistinctCasual: Int = 0,
        var numAchievementsEarned: Int = 0,
        var numAchievementsEarnedHC: Int = 0,
        var totalAchievements: Int = 0,
        var earnedPoints: Int = 0,
        var totalPoints: Int = 0,
        var earnedTruePoints: Int = 0,
        var totalTruePoints: Int = 0) {

    // Utility functions for setting int values from parsed strings

    fun setNumAchievementsEarned(points: String) {
        numAchievementsEarned = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setNumAchievementsEarnedHC(points: String) {
        numAchievementsEarnedHC = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setTotalAchievements(points: String) {
        totalAchievements = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setEarnedPoints(points: String) {
        earnedPoints = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setTotalPoints(points: String) {
        totalPoints = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setEarnedTruePoints(points: String) {
        earnedTruePoints = if (points.isDigitsOnly()) points.toInt() else 0
    }

    fun setTotalTruePoints(points: String) {
        totalTruePoints = if (points.isDigitsOnly()) points.toInt() else 0
    }
}