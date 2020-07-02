package com.kobrakid.retroachievements.ra

data class Achievement(
        val id: String,
        val badge: String,
        val title: String,
        val point: String,
        val trueRatio: String,
        val description: String,
        val dateEarned: String,
        val earnedHardcore: Boolean,
        val numAwarded: String,
        val numAwardedHC: String,
        val author: String,
        val dateCreated: String,
        val dateModified: String)