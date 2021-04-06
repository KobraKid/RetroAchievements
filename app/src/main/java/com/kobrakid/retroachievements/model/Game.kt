package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.database.RetroAchievementsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

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
        @field:ColumnInfo(name = "UserCompletionHardcore") override var userCompletionHardcore: String = "") : IGame, Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "0")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "$title | $consoleName"
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }

        suspend fun getGame(user: String?, id: String?, callback: suspend (IGame) -> Unit) {
            if (id?.isNotEmpty() == true) {
                // get game from db
                RetroAchievementsDatabase.getInstance().gameDao().getGameWithID(id).let {
                    if (it.size == 1) {
                        callback(it[0])
                    }
                }
                // update game from network
                CoroutineScope(IO).launch {
                    if (user?.isNotEmpty() == true)
                        RetroAchievementsApi.getInstance().GetGameInfoAndUserProgress(user, id) { parseGame(it, callback) }
                    else
                        RetroAchievementsApi.getInstance().GetGame(id) { parseGame(it, callback) }
                }
            } else {
                // no game can be returned
                callback(Game())
            }
        }

        private suspend fun parseGame(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (IGame) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_GAME, RetroAchievementsApi.RESPONSE.GET_GAME_EXTENDED, RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                    withContext(Dispatchers.Default) {
                        var game = Game()
                        try {
                            game = convertJsonStringToModel(JSONObject(response.second))
                            withContext(IO) { RetroAchievementsDatabase.getInstance().gameDao().insertGame(game) }
                        } catch (e: JSONException) {
                            Log.e(TAG, "unable to parse game details", e)
                        } finally {
                            callback(game)
                        }
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        fun convertJsonStringToModel(game: JSONObject): Game {
            return Game().apply {
                if (game.has("ID")) id = game.getString("ID")
                else if (game.has("GameID")) id = game.getString("GameID")
                if (game.has("Title")) title = Jsoup.parse(game.getString("Title").trim { it <= ' ' }).text().let { title ->
                    if (title.contains(", The"))
                        "The " + title.indexOf(", The").let {
                            title.substring(0, it) + title.substring(it + 5)
                        }
                    else title
                }
                if (game.has("ConsoleID")) consoleID = game.getString("ConsoleID")
                if (game.has("ForumTopicID")) forumTopicID = game.getString("ForumTopicID")
                if (game.has("Flags") && game.getString("Flags").isDigitsOnly()) flags = game.getInt("Flags")
                if (game.has("ImageIcon")) imageIcon = game.getString("ImageIcon")
                if (game.has("ImageTitle")) imageTitle = game.getString("ImageTitle").let { if (it.contains("000002.png")) "" else it }
                if (game.has("ImageIngame")) imageIngame = game.getString("ImageIngame").let { if (it.contains("000002.png")) "" else it }
                if (game.has("ImageBoxArt")) imageBoxArt = game.getString("ImageBoxArt").let { if (it.contains("000002.png")) "" else it }
                if (game.has("Publisher")) publisher = game.getString("Publisher").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                if (game.has("Developer")) developer = game.getString("Developer").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                if (game.has("Genre")) genre = game.getString("Genre").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                if (game.has("Released")) released = game.getString("Released").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                if (game.has("IsFinal")) isFinal = game.getBoolean("IsFinal")
                if (game.has("ConsoleName")) consoleName = game.getString("ConsoleName")
                if (game.has("RichPresencePatch")) richPresencePatch = game.getString("RichPresencePatch")
                if (game.has("NumAchievements")) numAchievements = game.getInt("NumAchievements")
                if (game.has("NumDistinctPlayersCasual")) numDistinctPlayersCasual = game.getInt("NumDistinctPlayersCasual")
                if (game.has("NumDistinctPlayersHardcore")) numDistinctPlayersHardcore = game.getInt("NumDistinctPlayersHardcore")
                if (game.has("NumAwardedToUser")) numAwardedToUser = game.getInt("NumAwardedToUser")
                if (game.has("NumAwardedToUserHardcore")) numAwardedToUserHardcore = game.getInt("NumAwardedToUserHardcore")
                if (game.has("UserCompletion")) userCompletion = game.getString("UserCompletion")
                if (game.has("UserCompletionHardcore")) userCompletionHardcore = game.getString("UserCompletionHardcore")
            }
        }

        private val TAG = Consts.BASE_TAG + Game::class.java.simpleName
    }
}