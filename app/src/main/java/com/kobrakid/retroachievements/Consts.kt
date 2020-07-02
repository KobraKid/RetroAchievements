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
    }

    enum class Theme(val themeName: String, val enabled: Boolean = true) {
        RA("RetroAchievements Themes", false),
        BLANK("Blank"),
        TWENTYSIXTEEN("TwentySixteen"),
        GREEN("Green"),
        PONY("Pony"),
        RED("Red"),
        SPOOKY("Spooky"),
        MORE("More Themes", false);

        override fun toString(): String {
            return themeName
        }
    }

    init {
        throw AssertionError()
    }
}