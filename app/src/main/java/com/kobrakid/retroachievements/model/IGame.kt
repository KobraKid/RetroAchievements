package com.kobrakid.retroachievements.model

interface IGame {
    var id: String
    var title: String
    var consoleID: String
    var forumTopicID: String
    var flags: Int
    var imageIcon: String
    var imageTitle: String
    var imageIngame: String
    var imageBoxArt: String
    var publisher: String
    var developer: String
    var genre: String
    var released: String
    var isFinal: Boolean
    var consoleName: String
    var richPresencePatch: String
    var numAchievements: Int
    var numDistinctPlayersCasual: Int
    var numDistinctPlayersHardcore: Int
    var numAwardedToUser: Int
    var numAwardedToUserHardcore: Int
    var userCompletion: String
    var userCompletionHardcore: String

    override fun toString(): String
}