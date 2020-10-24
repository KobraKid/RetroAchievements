package com.kobrakid.retroachievements.model

import com.kobrakid.retroachievements.Consts

data class User(
        var username: String = "",
        var rank: String = "",
        var motto: String = "",
        var totalPoints: String = "",
        var totalTruePoints: String = "",
        var memberSince: String = "",
        var userPic: String = Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + username + ".png"
) {
    constructor(copy: User?) : this(
            copy?.username ?: "",
            copy?.rank ?: "",
            copy?.motto ?: "",
            copy?.totalPoints ?: "",
            copy?.totalTruePoints ?: "",
            copy?.memberSince ?: "",
            copy?.userPic ?: ""
    )

    val retroRatio: Float get() = totalTruePoints.toFloat().div(totalPoints.toFloat())
}