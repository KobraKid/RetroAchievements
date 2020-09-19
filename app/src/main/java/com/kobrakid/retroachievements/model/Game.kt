package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable

data class Game(
        var id: String = "0",
        var console: String = "",
        var imageIcon: String = "",
        var title: String = "",
        var developer: String = "",
        var publisher: String = "",
        var genre: String = "",
        var released: String = "",
        var forumTopicID: String = "") : Parcelable {

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