package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class Achievement(
        @field:ColumnInfo(name = "AchievementID") @field:PrimaryKey val achievementID: String,
        @field:ColumnInfo(name = "ID") val id: String,
        @field:ColumnInfo(name = "NumAwarded") var numAwarded: String,
        @field:ColumnInfo(name = "NumAwardedHardcore") var numAwardedHardcore: String,
        @field:ColumnInfo(name = "Title") var title: String,
        @field:ColumnInfo(name = "Description") var description: String,
        @field:ColumnInfo(name = "Points") var points: String,
        @field:ColumnInfo(name = "TrueRatio") var trueRatio: String,
        @field:ColumnInfo(name = "Author") var author: String,
        @field:ColumnInfo(name = "DateModified") var dateModified: String,
        @field:ColumnInfo(name = "DateCreated") val dateCreated: String,
        @field:ColumnInfo(name = "BadgeName") var badgeName: String,
        @field:ColumnInfo(name = "DisplayOrder") var displayOrder: String,
        @field:ColumnInfo(name = "MemAddr") val memAddr: String,
        @field:ColumnInfo(name = "DateEarned") var dateEarned: String,
        @field:ColumnInfo(name = "DateEarnedHardcore") var dateEarnedHardcore: String,
)