package com.kobrakid.retroachievements.database

import androidx.room.*
import com.kobrakid.retroachievements.model.IGame

@Entity(tableName = "game")
data class Game(
        @field:ColumnInfo(name = "ID") @field:PrimaryKey override var id: String = "0",
        @field:ColumnInfo(name = "Title") override var title: String = "",
        @field:ColumnInfo(name = "ConsoleID") override var consoleID: String = "0",
        @field:ColumnInfo(name = "ForumTopicID") override var forumTopicID: String = "0",
        @field:ColumnInfo(name = "Flags") override var flags: Int = 0,
        @field:ColumnInfo(name = "ImageIcon") override var imageIcon: String = "",
        @field:ColumnInfo(name = "ImageTitle") override var imageTitle: String = "",
        @field:ColumnInfo(name = "ImageIngame") override var imageIngame: String = "",
        @field:ColumnInfo(name = "ImageBoxArt") override var imageBoxArt: String = "",
        @field:ColumnInfo(name = "Publisher") override var publisher: String = "",
        @field:ColumnInfo(name = "Developer") override var developer: String = "",
        @field:ColumnInfo(name = "Genre") override var genre: String = "",
        @field:ColumnInfo(name = "Released") override var released: String = "",
        @field:ColumnInfo(name = "IsFinal") override var isFinal: Boolean = true,
        @field:ColumnInfo(name = "ConsoleName") override var consoleName: String = "",
        @field:ColumnInfo(name = "RichPresencePatch") override var richPresencePatch: String = "",
        @field:ColumnInfo(name = "NumAchievements") override var numAchievements: Int = 0,
        @field:ColumnInfo(name = "NumDistinctPlayersCasual") override var numDistinctPlayersCasual: Int = 0,
        @field:ColumnInfo(name = "NumDistinctPlayersHardcore") override var numDistinctPlayersHardcore: Int = 0,
        @field:ColumnInfo(name = "NumAwardedToUser") override var numAwardedToUser: Int = 0,
        @field:ColumnInfo(name = "NumAwardedToUserHardcore") override var numAwardedToUserHardcore: Int = 0,
        @field:ColumnInfo(name = "UserCompletion") override var userCompletion: String = "",
        @field:ColumnInfo(name = "UserCompletionHardcore") override var userCompletionHardcore: String = "") : IGame {

    override fun toString(): String {
        return "[$id] $title | $consoleName"
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