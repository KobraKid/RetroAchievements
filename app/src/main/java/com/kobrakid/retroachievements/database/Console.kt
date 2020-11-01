package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kobrakid.retroachievements.model.IConsole

@Entity(tableName = "console")
data class Console(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey override var id: String = "0",
        @field:ColumnInfo(name = "ConsoleName") override var consoleName: String = "",
        @field:ColumnInfo(name = "NumGames") override var games: Int = 0) : IConsole {

    override fun toString(): String {
        return "[$id] $consoleName ($games Games)"
    }

}