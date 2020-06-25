package com.kobrakid.retroachievements.fragment

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RAAPICallback
import com.kobrakid.retroachievements.RAAPIConnectionDeprecated
import com.kobrakid.retroachievements.adapter.GameCommentsAdapter
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.lang.ref.WeakReference
import java.util.*

/**
 * A Fragment to hold recent game comments.
 */
class GameCommentsFragment : Fragment(), RAAPICallback {

    private var gameCommentsAdapter: RecyclerView.Adapter<*>? = null
    private val comments = mutableMapOf<String, List<String>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_game_comments, container, false)
        if (arguments != null) {
            val commentsRecyclerView: RecyclerView = view.findViewById(R.id.game_comments_recycler_view)
            commentsRecyclerView.layoutManager = LinearLayoutManager(context)
            gameCommentsAdapter = context?.let { GameCommentsAdapter(it, comments) }
            commentsRecyclerView.adapter = gameCommentsAdapter
            RAAPIConnectionDeprecated(context).ScrapeGameInfoFromWeb(arguments?.getString("GameID"), this)
        }
        return view
    }

    override fun callback(responseCode: Int, response: String) {
        if (responseCode == RAAPIConnectionDeprecated.RESPONSE_SCRAPE_GAME_PAGE) {
            ParseCommentsAsyncTask(this).execute(response)
        }
    }

    class ParseCommentsAsyncTask internal constructor(fragment: GameCommentsFragment) : AsyncTask<String?, Void?, Map<String, List<String>>>() {
        private val fragmentReference: WeakReference<GameCommentsFragment> = WeakReference(fragment)
        override fun doInBackground(vararg strings: String?): Map<String, List<String>> {
            val response = strings[0]
            val document = Jsoup.parse(response)
            val elements = document.getElementsByClass("feed_comment")
            val comments: MutableMap<String, List<String>> = HashMap()
            val text: MutableList<String> = ArrayList()
            val user: MutableList<String> = ArrayList()
            val acct: MutableList<String> = ArrayList()
            val score: MutableList<String> = ArrayList()
            val rank: MutableList<String> = ArrayList()
            val tag: MutableList<String> = ArrayList()
            val date: MutableList<String> = ArrayList()
            for (i in elements.indices) {
                val e = elements[i]
                text.add(e.selectFirst("td.commenttext").html().trim { it <= ' ' })
                user.add(e.selectFirst("td.iconscommentsingle").selectFirst("img").attr("title").trim { it <= ' ' })
                val tooltip = e.selectFirst("td.iconscommentsingle").selectFirst("div").attr("onmouseover")
                val userCard = Jsoup.parse(Parser.unescapeEntities(tooltip.substring(5, tooltip.length - 2).replace("\\", ""), false))
                acct.add(userCard.selectFirst("td.usercardaccounttype").html().trim { it <= ' ' })
                score.add(userCard.select("td.usercardbasictext")[0].html().substring(15))
                rank.add(userCard.select("td.usercardbasictext")[1].html().substring(18))
                tag.add(if (userCard.selectFirst("span") != null) userCard.selectFirst("span").html() else "RA_NO_TAG_$i")
                date.add(e.selectFirst("td.smalldate").html().trim { it <= ' ' })
            }
            comments["text"] = text
            comments["user"] = user
            comments["acct"] = acct
            comments["score"] = score
            comments["rank"] = rank
            comments["tag"] = tag
            comments["date"] = date
            return comments
        }

        override fun onPostExecute(strings: Map<String, List<String>>) {
            val fragment = fragmentReference.get()
            if (fragment?.gameCommentsAdapter != null) {
                fragment.comments.putAll(strings)
                fragment.gameCommentsAdapter?.notifyDataSetChanged()
            }
        }

    }
}