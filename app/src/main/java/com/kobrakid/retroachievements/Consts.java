package com.kobrakid.retroachievements;

@SuppressWarnings("SpellCheckingInspection")
public final class Consts {

    // Activity Request Codes
    public static final int BEGIN_LOGIN = 0;
    public static final int PULL_API_KEY = 1;
    // Activity Response Codes
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    public static final int CANCELLED = 2;

    // API
    public static final String BASE_URL = "https://retroachievements.org";
    public static final String API_URL = "API";
    public static final String FORUM_POSTFIX = "viewtopic.php?t=";
    public static final String GAME_POSTFIX = "game";
    public static final String GAME_BADGE_POSTFIX = "Badge";
    public static final String KEY_URL = "APIDemo.php";
    public static final String LEADERBOARDS_POSTFIX = "leaderboardList.php";
    public static final String LEADERBOARDS_INFO_POSTFIX = "leaderboardinfo.php?i=";
    public static final String LINKED_HASHES_POSTFIX = "linkedhashes.php?g=";
    public static final String USER_PIC_POSTFIX = "UserPic";
    public static final String USER_POSTFIX = "user";

    // Theming
    public static final String[] THEMES = new String[]{"RetroAchievements Themes", "Blank", "TwentySixteen", "Green", "Pony", "Red", "Spooky", "More Themes"};
    public static final boolean[] THEMES_ENABLE_ARRAY = new boolean[]{false, true, true, true, true, true, true, false};

    private Consts() {
        throw new AssertionError();
    }
}
