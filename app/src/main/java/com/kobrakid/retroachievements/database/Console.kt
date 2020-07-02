package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "console")
class Console(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey var id: Int,
        @field:ColumnInfo(name = "ConsoleName") var consoleName: String,
        @field:ColumnInfo(name = "GameCount") var gameCount: Int) {

    override fun toString(): String {
        return "$consoleName | ID: $id, Count: $gameCount"
    }

}