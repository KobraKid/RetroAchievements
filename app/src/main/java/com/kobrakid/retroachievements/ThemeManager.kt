package com.kobrakid.retroachievements

import android.content.Context
import android.content.SharedPreferences

/**
 * This class is responsible for translating between Strings and their corresponding themes.
 */
internal object ThemeManager {
    @JvmStatic
    fun getTheme(context: Context, sharedPreferences: SharedPreferences): Int {
        // A default value is provided so theme is not null
        return when (sharedPreferences.getString(context.resources.getString(R.string.theme_setting), "Blank")) {
            "Blank" -> R.style.BlankTheme
            "TwentySixteen" -> R.style.TwentySixteenTheme
            "Green" -> R.style.GreenTheme
            "Pony" -> R.style.PonyTheme
            "Red" -> R.style.RedTheme
            "Spooky" -> R.style.SpookyTheme
            else -> R.style.BlankTheme
        }
    }
}