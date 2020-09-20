package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game")
class Game(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey var id: String,
        @field:ColumnInfo(name = "Title") var title: String,
        @field:ColumnInfo(name = "ImageIcon") var imageIcon: String,
        @field:ColumnInfo(name = "ConsoleID") var consoleID: String,
        @field:ColumnInfo(name = "ConsoleName") var consoleName: String) {

    override fun toString(): String {
        return "[$id] $title | $consoleName"
    }

}