package com.kobrakid.retroachievements

class Consts private constructor() {
    companion object {
        // Activity Request Codes
        const val BEGIN_LOGIN = 0
        const val PULL_API_KEY = 1

        // Activity Response Codes
        const val SUCCESS = 0
        const val FAILURE = 1
        const val CANCELLED = 2

        // API
        const val BASE_URL = "https://retroachievements.org"
        const val API_URL = "API"
        const val FORUM_POSTFIX = "viewtopic.php?t="
        const val GAME_POSTFIX = "game"
        const val GAME_BADGE_POSTFIX = "Badge"
        const val KEY_URL = "APIDemo.php"
        const val LEADERBOARDS_POSTFIX = "leaderboardList.php"
        const val LEADERBOARDS_INFO_POSTFIX = "leaderboardinfo.php?i="
        const val LINKED_HASHES_POSTFIX = "linkedhashes.php?g="
        const val USER_PIC_POSTFIX = "UserPic"
        const val USER_POSTFIX = "user"

        // Theming
        // TODO: Come up with some fun new themes
        // TODO: Make this NOT @JvmField
        @JvmField
        val THEMES = arrayOf("RetroAchievements Themes", "Blank", "TwentySixteen", "Green", "Pony", "Red", "Spooky", "More Themes")

        @JvmField
        val THEMES_ENABLE_ARRAY = booleanArrayOf(false, true, true, true, true, true, true, false)
    }

    init {
        throw AssertionError()
    }
}