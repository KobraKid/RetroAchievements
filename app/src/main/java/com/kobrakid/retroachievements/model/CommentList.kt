package com.kobrakid.retroachievements.model

import android.util.Log
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

class CommentList {

    companion object {
        suspend fun getCommentsOnGame(id: String, callback: suspend (List<Comment>) -> Unit) {
            RetroAchievementsApi.getInstance().ScrapeGameInfoFromWeb(id) { parseGameComments(it, callback) }
        }

        private suspend fun parseGameComments(response: Pair<RetroAchievementsApi.RESPONSE, String>, callback: suspend (List<Comment>) -> Unit) {
            when (response.first) {
                RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
                RetroAchievementsApi.RESPONSE.SCRAPE_GAME_PAGE -> {
                    withContext(Default) {
                        val commentList = mutableListOf<Comment>()
                        for (comment in Jsoup.parse(response.second).getElementsByClass("feed_comment")) {
                            val username = comment.selectFirst("td.iconscommentsingle").selectFirst("img")?.attr("title")?.trim { it <= ' ' }
                                    ?: "Unknown"
                            var account = "Unknown"
                            var score = "0"
                            var rank = "0"
                            var tag = ""
                            val tooltip = comment.selectFirst("td.iconscommentsingle").selectFirst("div").attr("onmouseover")
                            if (tooltip.length > 5) {
                                with(Jsoup.parse(Parser.unescapeEntities(tooltip.substring(5, tooltip.length - 2).replace("\\", ""), false))) {
                                    account = selectFirst("td.usercardaccounttype").html().trim { it <= ' ' }
                                    score = select("td.usercardbasictext")[0].html().substring(15)
                                    rank = select("td.usercardbasictext")[1].html().substring(18)
                                    tag = if (selectFirst("span") != null) selectFirst("span").html() else ""
                                }
                            } else {
                                Log.w(TAG, "${Jsoup.parse(response.second).getElementsByClass("longheader")[0].text()} Tooltip too short: \"$tooltip\"")
                            }
                            commentList.add(Comment(
                                    comment.selectFirst("td.commenttext").text().trim { it <= ' ' },
                                    username, account, score, rank, tag,
                                    comment.selectFirst("td.smalldate").html().trim { it <= ' ' }))
                        }
                        callback(commentList)
                    }
                }
                else -> Log.v(TAG, "${response.first}: ${response.second}")
            }
        }

        private val TAG = Consts.BASE_TAG + CommentList::class.java.simpleName
    }
}