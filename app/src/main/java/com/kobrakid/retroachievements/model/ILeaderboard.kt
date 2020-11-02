package com.kobrakid.retroachievements.model

interface ILeaderboard {
    var id: String
    var gameId: String
    var icon: String
    var console: String
    var title: String
    var description: String
    var type: String
    var numResults: String
}