package com.kobrakid.retroachievements.model

interface IAchievement {
    val achievementID: String
    val id: String
    val numAwarded: String
    val numAwardedHardcore: String
    val title: String
    val description: String
    val points: String
    val truePoints: String
    val author: String
    val dateModified: String
    val dateCreated: String
    val badgeName: String
    val displayOrder: String
    val memAddr: String
    val dateEarned: String
    val dateEarnedHardcore: String
}