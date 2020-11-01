package com.kobrakid.retroachievements.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Comment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

class GameCommentsViewModel : ViewModel() {

    private val _comments = MutableLiveData<List<Comment>>()
    private val _loading = MutableLiveData<Boolean>()

    val comments: LiveData<List<Comment>> get() = _comments
    val loading: LiveData<Boolean> get() = _loading

    fun setId(id: String, forceReload: Boolean = false) {
        if (!forceReload && comments.value?.isNotEmpty() == true) return
        _loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.getInstance().ScrapeGameInfoFromWeb(id) { parseGameComments(it) }
        }
    }

    private suspend fun parseGameComments(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.SCRAPE_GAME_PAGE -> {
                withContext(Dispatchers.Default) {
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
                    withContext(Main) {
                        _comments.value = commentList
                        _loading.value = false
                    }
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + GameCommentsViewModel::class.java.simpleName
    }
}