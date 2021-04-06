package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "console")
data class Console(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey override var id: String = "0",
        @field:ColumnInfo(name = "ConsoleName") override var consoleName: String = "",
        @field:ColumnInfo(name = "NumGames") override var games: Int = 0) : IConsole, Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "0")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "[$id] $consoleName ($games Games)"
    }

    companion object CREATOR : Parcelable.Creator<Console> {
        override fun createFromParcel(parcel: Parcel): Console {
            return Console(parcel)
        }

        override fun newArray(size: Int): Array<Console?> {
            return arrayOfNulls(size)
        }
    }
}