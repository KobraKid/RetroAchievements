package com.kobrakid.retroachievements.ra

import android.os.Parcel
import android.os.Parcelable

data class Console(val id: String = "0", val name: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "0",
            parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
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