package com.kobrakid.retroachievements

class Consts private constructor() {
    companion object {
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

    @Suppress("unused")
    enum class Theme(val themeName: String, val themeAttr: Int, val enabled: Boolean = true) {
        RA("RetroAchievements Themes", 0, false),
        BLANK("Blank", R.style.BlankTheme),
        TWENTYSIXTEEN("TwentySixteen", R.style.TwentySixteenTheme),
        GREEN("Green", R.style.GreenTheme),
        PONY("Pony", R.style.PonyTheme),
        RED("Red", R.style.RedTheme),
        SPOOKY("Spooky", R.style.SpookyTheme),
        MORE("More Themes", 0, false),
        SYSTEM("System Default", R.style.MyDayNight);

        override fun toString(): String {
            return themeName
        }
    }

    init {
        throw AssertionError()
    }
}