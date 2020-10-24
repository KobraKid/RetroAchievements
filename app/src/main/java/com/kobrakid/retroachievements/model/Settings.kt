package com.kobrakid.retroachievements.model

import com.kobrakid.retroachievements.R

data class Settings(
        var theme: Int = R.style.BlankTheme,
        var user: String = "",
        var hideEmptyConsoles: Boolean = false,
        var hideEmptyGames: Boolean = false
) {
    constructor(copy: Settings?) : this(
            copy?.theme ?: R.style.BlankTheme,
            copy?.user ?: "",
            copy?.hideEmptyConsoles ?: false,
            copy?.hideEmptyGames ?: false
    )
}