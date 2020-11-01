package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kobrakid.retroachievements.model.IAchievement

@Entity(tableName = "achievement")
data class Achievement(
        @field:ColumnInfo(name = "AchievementID") @field:PrimaryKey override val achievementID: String,
        @field:ColumnInfo(name = "ID") override val id: String,
        @field:ColumnInfo(name = "NumAwarded") override var numAwarded: String,
        @field:ColumnInfo(name = "NumAwardedHardcore") override var numAwardedHardcore: String,
        @field:ColumnInfo(name = "Title") override var title: String,
        @field:ColumnInfo(name = "Description") override var description: String,
        @field:ColumnInfo(name = "Points") override var points: String,
        @field:ColumnInfo(name = "TruePoints") override var truePoints: String,
        @field:ColumnInfo(name = "Author") override var author: String,
        @field:ColumnInfo(name = "DateModified") override var dateModified: String,
        @field:ColumnInfo(name = "DateCreated") override val dateCreated: String,
        @field:ColumnInfo(name = "BadgeName") override var badgeName: String,
        @field:ColumnInfo(name = "DisplayOrder") override var displayOrder: String,
        @field:ColumnInfo(name = "MemAddr") override val memAddr: String,
        @field:ColumnInfo(name = "DateEarned") override var dateEarned: String,
        @field:ColumnInfo(name = "DateEarnedHardcore") override var dateEarnedHardcore: String,
) : IAchievement