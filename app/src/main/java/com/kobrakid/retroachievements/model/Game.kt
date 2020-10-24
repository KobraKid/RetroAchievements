package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable

data class Game(
        val id: String = "0",
        val console: String = "",
        val imageIcon: String = "",
        val title: String = "",
        val developer: String = "",
        val publisher: String = "",
        val genre: String = "",
        val released: String = "",
        val forumTopicID: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "0",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(console)
        parcel.writeString(imageIcon)
        parcel.writeString(title)
        parcel.writeString(developer)
        parcel.writeString(publisher)
        parcel.writeString(genre)
        parcel.writeString(released)
        parcel.writeString(forumTopicID)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }
    }
}