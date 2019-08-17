package com.kobrakid.retroachievements;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class is responsible for translating between Strings and their corresponding themes.
 */
class ThemeManager {

    static int getTheme(Context context, SharedPreferences sharedPreferences) {
        String theme = sharedPreferences.getString(context.getResources().getString(R.string.theme_setting), "Blank");
        switch (theme) {
            case "TwentySixteen":
                return R.style.TwentySixteenTheme;
            case "Green":
                return R.style.GreenTheme;
            case "Pony":
                return R.style.PonyTheme;
            case "Red":
                return R.style.RedTheme;
            case "Spooky":
                return R.style.SpookyTheme;
            case "Blank":
                return R.style.BlankTheme;
            default:
                return -1;
        }
    }

}
