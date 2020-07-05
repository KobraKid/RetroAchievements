package com.kobrakid.retroachievements.ra

import android.os.Parcel
import android.os.Parcelable

data class Achievement(
        val id: String = "0",
        val badge: String = "",
        val title: String = "",
        val point: String = "",
        val trueRatio: String = "",
        val description: String = "",
        val dateEarned: String = "",
        val earnedHardcore: Boolean = false,
        val numAwarded: String = "",
        val numAwardedHC: String = "",
        val author: String = "",
        val dateCreated: String = "",
        val dateModified: String = "",
        val numDistinctCasual: Double = 0.0) : Parcelable {
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
            parcel.readString() ?: "",
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(badge)
        parcel.writeString(title)
        parcel.writeString(point)
        parcel.writeString(trueRatio)
        parcel.writeString(description)
        parcel.writeString(dateEarned)
        parcel.writeByte(if (earnedHardcore) 1 else 0)
        parcel.writeString(numAwarded)
        parcel.writeString(numAwardedHC)
        parcel.writeString(author)
        parcel.writeString(dateCreated)
        parcel.writeString(dateModified)
        parcel.writeDouble(numDistinctCasual)
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