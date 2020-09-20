package com.kobrakid.retroachievements.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "console")
class Console(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey var id: String,
        @field:ColumnInfo(name = "ConsoleName") var consoleName: String) {

    override fun toString(): String {
        return "[$id] $consoleName"
    }

}