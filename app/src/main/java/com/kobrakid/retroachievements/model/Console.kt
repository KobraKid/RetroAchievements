package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable

data class Console(
        override var id: String = "0",
        override var consoleName: String = "",
        override var games: Int = 0
) : IConsole, Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "0")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
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