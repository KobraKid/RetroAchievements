package com.kobrakid.retroachievements.database

import androidx.room.*

@Entity(tableName = "game")
data class Game(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey val id: String,
        @field:ColumnInfo(name = "Title") var title: String,
        @field:ColumnInfo(name = "ConsoleID") val consoleID: String,
        @field:ColumnInfo(name = "ConsoleName") val consoleName: String,
        @field:ColumnInfo(name = "ImageIcon") var imageIcon: String,
        @field:ColumnInfo(name = "ImageTitle") var imageTitle: String = "",
        @field:ColumnInfo(name = "ImageIngame") var imageIngame: String = "",
        @field:ColumnInfo(name = "ImageBoxArt") var imageBoxArt: String = "",
        @field:ColumnInfo(name = "Developer") var developer: String = "",
        @field:ColumnInfo(name = "Publisher") var publisher: String = "",
        @field:ColumnInfo(name = "Genre") var genre: String = "",
        @field:ColumnInfo(name = "ForumTopicID") val forumTopicID: String = "",
        @field:ColumnInfo(name = "Flags") var flags: Int = 0,
        @field:ColumnInfo(name = "Released") var released: String = "",
        @field:ColumnInfo(name = "IsFinal") var isFinal: Boolean = true,
        @field:ColumnInfo(name = "RichPresencePatch") var richPresencePatch: String = "",
        @field:ColumnInfo(name = "NumAchievements") var numAchievements: Int = 0,
        @field:ColumnInfo(name = "NumDistinctPlayersCasual") var numDistinctPlayersCasual: String = "",
        @field:ColumnInfo(name = "NumDistinctPlayersHardcore") var numDistinctPlayersHardcore: String = "",
        @field:ColumnInfo(name = "NumAwardedToUser") var numAwardedToUser: Int = 0,
        @field:ColumnInfo(name = "NumAwardedToUserHardcore") var numAwardedToUserHardcore: Int = 0,
        @field:ColumnInfo(name = "UserCompletion") var userCompletion: String = "",
        @field:ColumnInfo(name = "UserCompletionHardcore") var userCompletionHardcore: String = "") {

    override fun toString(): String {
        return "$title | $consoleName"
    }

}

data class GameWithAchievements(
        @Embedded val game: Game,
        @Relation(
                parentColumn = "ID",
                entityColumn = "ID")
        val achievements: List<Achievement>
) {
    override fun toString(): String {
        return "${game.title} | ${game.consoleName}"
    }
}