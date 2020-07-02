package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game")
class Game(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey var id: Int,
        @field:ColumnInfo(name = "GameName") var gameName: String,
        @field:ColumnInfo(name = "AchievementCount") var achievementCount: Int) {

    override fun toString(): String {
        return "$gameName | ID: $id, # Achievements $achievementCount"
    }

}