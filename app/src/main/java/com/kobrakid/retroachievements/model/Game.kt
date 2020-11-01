package com.kobrakid.retroachievements.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
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

data class Game(
        override var id: String = "0",
        override var title: String = "",
        override var consoleID: String = "0",
        override var forumTopicID: String = "0",
        override var flags: Int = 0,
        override var imageIcon: String = "",
        override var imageTitle: String = "",
        override var imageIngame: String = "",
        override var imageBoxArt: String = "",
        override var publisher: String = "",
        override var developer: String = "",
        override var genre: String = "",
        override var released: String = "",
        override var isFinal: Boolean = true,
        override var consoleName: String = "",
        override var richPresencePatch: String = "",
        override var numAchievements: Int = 0,
        override var numDistinctPlayersCasual: Int = 0,
        override var numDistinctPlayersHardcore: Int = 0,
        override var numAwardedToUser: Int = 0,
        override var numAwardedToUserHardcore: Int = 0,
        override var userCompletion: String = "",
        override var userCompletionHardcore: String = ""
) : IGame, Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "0")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "[$id] $title | $consoleName"
    }

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }

        suspend fun getGame(user: String?, id: String?, callback: suspend (IGame) -> Unit) {
            if (user?.isNotEmpty() == true && id?.isNotEmpty() == true) {
                // get game from db
                RetroAchievementsDatabase.getInstance().gameDao().getGameWithID(id).let {
                    if (it.size == 1) {
                        callback(it[0])
                    }
                }
                // update game from network
                CoroutineScope(IO).launch {
                    RetroAchievementsApi.getInstance().GetGameInfoAndUserProgress(user, id) { parseGameInfoAndUserProgress(it, callback) }
                }
            } else {
                // no game can be returned
                callback(Game())
            }
        }

        private suspend fun parseGameInfoAndUserProgress(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (IGame) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                    withContext(Dispatchers.Default) {
                        val game = Game()
                        try {
                            val reader = JSONObject(response.second)
                            game.apply {
                                id = reader.getString("ID")
                                title = Jsoup.parse(reader.getString("Title").trim { it <= ' ' }).text().let { title ->
                                    if (title.contains(", The"))
                                        "The " + title.indexOf(", The").let {
                                            title.substring(0, it) + title.substring(it + 5)
                                        }
                                    else title
                                }
                                consoleID = reader.getString("ConsoleID")
                                forumTopicID = reader.getString("ForumTopicID")
                                flags = reader.getInt("Flags")
                                imageIcon = reader.getString("ImageIcon")
                                imageTitle = reader.getString("ImageTitle")
                                imageIngame = reader.getString("ImageIngame")
                                imageBoxArt = reader.getString("ImageBoxArt")
                                publisher = reader.getString("Publisher").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                                developer = reader.getString("Developer").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                                genre = reader.getString("Genre").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                                released = reader.getString("Released").let { if (it == "null") "????" else Jsoup.parse(it).text() }
                                isFinal = reader.getBoolean("IsFinal")
                                consoleName = reader.getString("ConsoleName")
                                richPresencePatch = reader.getString("RichPresencePatch")
                                numAchievements = reader.getInt("NumAchievements")
                                numDistinctPlayersCasual = reader.getInt("NumDistinctPlayersCasual")
                                numDistinctPlayersHardcore = reader.getInt("NumDistinctPlayersHardcore")
                                numAwardedToUser = reader.getInt("NumAwardedToUser")
                                numAwardedToUserHardcore = reader.getInt("NumAwardedToUserHardcore")
                                userCompletion = reader.getString("UserCompletion")
                                userCompletionHardcore = reader.getString("UserCompletionHardcore")
                            }
                            withContext(IO) { RetroAchievementsDatabase.getInstance().gameDao().insertGame(convertGameModelToDatabase(game)) }
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

        private fun convertGameModelToDatabase(game: Game): com.kobrakid.retroachievements.database.Game {
            return com.kobrakid.retroachievements.database.Game(
                    id = game.id,
                    title = game.title,
                    consoleID = game.consoleID,
                    forumTopicID = game.forumTopicID,
                    flags = game.flags,
                    imageIcon = game.imageIcon,
                    imageTitle = game.imageTitle,
                    imageIngame = game.imageIngame,
                    imageBoxArt = game.imageBoxArt,
                    publisher = game.publisher,
                    developer = game.developer,
                    genre = game.genre,
                    released = game.released,
                    isFinal = game.isFinal,
                    consoleName = game.consoleName,
                    richPresencePatch = game.richPresencePatch,
                    numAchievements = game.numAchievements,
                    numDistinctPlayersCasual = game.numDistinctPlayersCasual,
                    numDistinctPlayersHardcore = game.numDistinctPlayersHardcore,
                    numAwardedToUser = game.numAwardedToUser,
                    numAwardedToUserHardcore = game.numAwardedToUserHardcore,
                    userCompletion = game.userCompletion,
                    userCompletionHardcore = game.userCompletionHardcore
            )
        }

        private val TAG = Consts.BASE_TAG + Game::class.java.simpleName
    }
}