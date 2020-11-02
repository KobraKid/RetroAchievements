package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kobrakid.retroachievements.model.ILeaderboard

@Entity(tableName = "leaderboard")
data class Leaderboard(
        @field:ColumnInfo @field:PrimaryKey override var id: String = "0",
        @field:ColumnInfo override var gameId: String = "",
        @field:ColumnInfo override var icon: String = "",
        @field:ColumnInfo override var console: String = "",
        @field:ColumnInfo override var title: String = "",
        @field:ColumnInfo override var description: String = "",
        @field:ColumnInfo override var type: String = "",
        @field:ColumnInfo override var numResults: String = "0") : ILeaderboard