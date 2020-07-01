package com.kobrakid.retroachievements.ra

import android.os.Parcel
import android.os.Parcelable

data class Leaderboard(
        val id: String = "0",
        val image: String = "",
        val game: String = "",
        val console: String = "",
        val title: String = "",
        val description: String = "",
        val type: String = "",
        val numResults: String = "0") : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "0",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "0")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(game)
        parcel.writeString(console)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(type)
        parcel.writeString(numResults)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "#$id: $title ($game - $console) [$numResults]"
    }

    companion object CREATOR : Parcelable.Creator<Leaderboard> {
        override fun createFromParcel(parcel: Parcel): Leaderboard {
            return Leaderboard(parcel)
        }

        override fun newArray(size: Int): Array<Leaderboard?> {
            return arrayOfNulls(size)
        }
    }
}