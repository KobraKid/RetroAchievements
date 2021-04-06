package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
data class Achievement(
        @field:ColumnInfo(name = "AchievementID") @field:PrimaryKey override var achievementID: String = "0",
        @field:ColumnInfo(name = "ID") override var id: String = "0",
        @field:ColumnInfo(name = "NumAwarded") override var numAwarded: String = "0",
        @field:ColumnInfo(name = "NumAwardedHardcore") override var numAwardedHardcore: String = "0",
        @field:ColumnInfo(name = "Title") override var title: String = "",
        @field:ColumnInfo(name = "Description") override var description: String = "",
        @field:ColumnInfo(name = "Points") override var points: String = "0",
        @field:ColumnInfo(name = "TruePoints") override var truePoints: String = "0",
        @field:ColumnInfo(name = "Author") override var author: String = "",
        @field:ColumnInfo(name = "DateModified") override var dateModified: String = "",
        @field:ColumnInfo(name = "DateCreated") override var dateCreated: String = "",
        @field:ColumnInfo(name = "BadgeName") override var badgeName: String = "",
        @field:ColumnInfo(name = "DisplayOrder") override var displayOrder: String = "",
        @field:ColumnInfo(name = "MemAddr") override var memAddr: String = "",
        @field:ColumnInfo(name = "DateEarned") override var dateEarned: String = "",
        @field:ColumnInfo(name = "DateEarnedHardcore") override var dateEarnedHardcore: String = "") : IAchievement, Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "0")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(achievementID)
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