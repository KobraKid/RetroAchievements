package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable

data class Achievement(
        val id: String = "0",
        val badge: String = "",
        val title: String = "",
        val points: String = "",
        val truePoints: String = "",
        val description: String = "",
        val dateEarned: String = "",
        val earnedHardcore: Boolean = false,
        val numAwarded: String = "",
        val numAwardedHC: String = "",
        val author: String = "",
        val dateCreated: String = "",
        val dateModified: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "0",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(badge)
        parcel.writeString(title)
        parcel.writeString(points)
        parcel.writeString(truePoints)
        parcel.writeString(description)
        parcel.writeString(dateEarned)
        parcel.writeByte(if (earnedHardcore) 1 else 0)
        parcel.writeString(numAwarded)
        parcel.writeString(numAwardedHC)
        parcel.writeString(author)
        parcel.writeString(dateCreated)
        parcel.writeString(dateModified)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Achievement> {
        override fun createFromParcel(parcel: Parcel): Achievement {
            return Achievement(parcel)
        }

        override fun newArray(size: Int): Array<Achievement?> {
            return arrayOfNulls(size)
        }
    }
}