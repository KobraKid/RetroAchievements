package com.kobrakid.retroachievements.model

import com.kobrakid.retroachievements.Consts

data class UserSummary(
        val username: String = "",
        val rank: String = "",
        val motto: String = "",
        var totalPoints: String = "",
        var totalTruePoints: String = "",
        var retroRatio: String = "",
        val memberSince: String = "",
        var userPic: String = Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + username + ".png"
)