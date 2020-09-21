package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.view.adapter.GameCommentsAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

/**
 * A Fragment to hold recent game comments.
 */
class GameCommentsFragment : Fragment() {

    private val gameCommentsAdapter = GameCommentsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        return inflater.inflate(R.layout.view_pager_game_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            val commentsRecyclerView = view.findViewById<RecyclerView>(R.id.game_comments_recycler_view)
            commentsRecyclerView.layoutManager = LinearLayoutManager(context)
            commentsRecyclerView.adapter = gameCommentsAdapter
            val id = arguments?.getString("GameID") ?: "0"
            CoroutineScope(IO).launch {
                RetroAchievementsApi.ScrapeGameInfoFromWeb(context, id) { parseGameComments(it) }
            }
        }
    }

    private suspend fun parseGameComments(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.SCRAPE_GAME_PAGE -> {
                withContext(Default) {
                    for (comment in Jsoup.parse(response.second).getElementsByClass("feed_comment")) {
                        val userImage = comment.selectFirst("td.iconscommentsingle").selectFirst("img")?.attr("title")?.trim { it <= ' ' }
                                ?: "Unknown"
                        var account = "Unknown"
                        var score = "0"
                        var rank = "0"
                        var tag = "_RA_NO_TAG"
                        val tooltip = comment.selectFirst("td.iconscommentsingle").selectFirst("div").attr("onmouseover")
                        if (tooltip.length > 5) {
                            with(Jsoup.parse(Parser.unescapeEntities(tooltip.substring(5, tooltip.length - 2).replace("\\", ""), false))) {
                                account = selectFirst("td.usercardaccounttype").html().trim { it <= ' ' }
                                score = select("td.usercardbasictext")[0].html().substring(15)
                                rank = select("td.usercardbasictext")[1].html().substring(18)
                                tag = if (selectFirst("span") != null) selectFirst("span").html() else "_RA_NO_TAG"
                            }
                        } else {
                            Log.w(TAG, "${Jsoup.parse(response.second).getElementsByClass("longheader")[0].text()} Tooltip too short: \"$tooltip\"")
                        }
                        gameCommentsAdapter.addComment(
                                comment.selectFirst("td.commenttext").text().trim { it <= ' ' },
                                userImage, account, score, rank, tag,
                                comment.selectFirst("td.smalldate").html().trim { it <= ' ' })
                    }
                    populateViews()
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private fun populateViews() {
        CoroutineScope(Main).launch {
            view?.findViewById<View>(R.id.game_comments_progress_bar)?.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + GameCommentsFragment::class.java.simpleName
    }

}