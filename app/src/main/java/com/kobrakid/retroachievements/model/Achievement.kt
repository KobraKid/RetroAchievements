package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable

data class Achievement(
        override val achievementID: String = "0",
        override val id: String = "0",
        override val numAwarded: String = "",
        override val numAwardedHardcore: String = "",
        override val title: String = "",
        override val description: String = "",
        override val points: String = "",
        override val truePoints: String = "",
        override val author: String = "",
        override val dateModified: String = "",
        override val dateCreated: String = "",
        override val badgeName: String = "",
        override val displayOrder: String = "0",
        override val memAddr: String = "",
        override val dateEarned: String = "",
        override val dateEarnedHardcore: String = "") : IAchievement, Parcelable {

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

        fun convertAchievementModelToDatabase(achievement: Achievement): com.kobrakid.retroachievements.database.Achievement {
            return com.kobrakid.retroachievements.database.Achievement(
                    achievementID = achievement.achievementID,
                    id = achievement.id,
                    numAwarded = achievement.numAwarded,
                    numAwardedHardcore = achievement.numAwardedHardcore,
                    title = achievement.title,
                    description = achievement.description,
                    points = achievement.points,
                    truePoints = achievement.truePoints,
                    author = achievement.author,
                    dateModified = achievement.dateModified,
                    dateCreated = achievement.dateCreated,
                    badgeName = achievement.badgeName,
                    displayOrder = achievement.displayOrder,
                    memAddr = achievement.memAddr,
                    dateEarned = achievement.dateEarned,
                    dateEarnedHardcore = achievement.dateEarnedHardcore
            )
        }
    }
}