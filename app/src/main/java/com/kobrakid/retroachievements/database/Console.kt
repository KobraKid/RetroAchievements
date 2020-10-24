package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "console")
data class Console(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey val id: String,
        @field:ColumnInfo(name = "ConsoleName") val consoleName: String,
        @field:ColumnInfo(name = "NumGames") val games: Int = 0) {

    override fun toString(): String {
        return "[$id] $consoleName ($games Games)"
    }

}