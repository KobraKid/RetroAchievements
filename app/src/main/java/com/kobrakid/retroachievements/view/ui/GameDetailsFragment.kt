package com.kobrakid.retroachievements.view.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.kobrakid.retroachievements.model.Game
import com.kobrakid.retroachievements.view.adapter.GameDetailsPagerAdapter
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

class GameDetailsFragment : Fragment(R.layout.fragment_game_details), View.OnClickListener {

    private val args: GameDetailsFragmentArgs by navArgs()
    private var game = Game()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true
        activity?.title = "Game Details"
        game.id = args.id

        view.findViewById<ViewPager>(R.id.game_details_view_pager).apply {
            adapter = GameDetailsPagerAdapter(childFragmentManager, game.id)
            offscreenPageLimit = GameDetailsPagerAdapter.GAME_DETAILS_PAGES - 1
        }

        // These views only exist in landscape, thus they require null-safe access
        view.findViewById<ImageButton>(R.id.game_details_button_page_0)?.setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.game_details_button_page_1)?.setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.game_details_button_page_2)?.setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.game_details_button_page_3)?.setOnClickListener(this)

        // TODO Linked hashes requires login
        CoroutineScope(Dispatchers.IO).launch {
            RetroAchievementsApi.GetGameInfoAndUserProgress(context, MainActivity.raUser, game.id) { parseGameInfoUserProgress(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_overflow, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_forum -> {
                val forumUrl = Consts.BASE_URL + "/" + Consts.FORUM_POSTFIX + game.forumTopicID
                val forumIntent = Intent(Intent.ACTION_VIEW)
                forumIntent.data = Uri.parse(forumUrl)
                startActivity(forumIntent)
            }
            R.id.action_webpage -> {
                val raUrl = Consts.BASE_URL + "/" + Consts.GAME_POSTFIX + "/" + game.id
                val raIntent = Intent(Intent.ACTION_VIEW)
                raIntent.data = Uri.parse(raUrl)
                startActivity(raIntent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onClick(view: View) {
        val page = when (view.id) {
            R.id.game_details_button_page_0 -> 0
            R.id.game_details_button_page_1 -> 1
            R.id.game_details_button_page_2 -> 2
            R.id.game_details_button_page_3 -> 3
            else -> 0
        }
        view.rootView.findViewById<View>(R.id.game_details_frame)
                .findFragment<AchievementSummaryFragment>()
                .childFragmentManager
                .popBackStackImmediate()
        view.rootView.findViewById<ViewPager>(R.id.game_details_view_pager).currentItem = page
    }

    private suspend fun parseGameInfoUserProgress(response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> Log.w(TAG, response.second)
            RetroAchievementsApi.RESPONSE.GET_GAME_INFO_AND_USER_PROGRESS -> {
                withContext(Dispatchers.Default) {
                    try {
                        val reader = JSONObject(response.second)
                        game.title = Jsoup.parse(reader.getString("Title").trim { it <= ' ' }).text()
                        if (game.title.contains(", The"))
                            game.title = "The " + game.title.indexOf(", The").let {
                                game.title.substring(0, it) + game.title.substring(it + 5)
                            }
                        game.console = reader.getString("ConsoleName")
                        game.imageIcon = reader.getString("ImageIcon")
                        game.developer = reader.getString("Developer")
                        game.developer = if (game.developer == "null") "????" else Jsoup.parse(game.developer).text()
                        game.publisher = reader.getString("Publisher")
                        game.publisher = if (game.publisher == "null") "????" else Jsoup.parse(game.publisher).text()
                        game.genre = reader.getString("Genre")
                        game.genre = if (game.genre == "null") "????" else Jsoup.parse(game.genre).text()
                        game.released = reader.getString("Released")
                        game.released = if (game.released == "null") "????" else Jsoup.parse(game.released).text()
                        game.forumTopicID = reader.getString("ForumTopicID")
                    } catch (e: JSONException) {
                        Log.e(TAG, "unable to parse game details", e)
                    }
                }
                withContext(Dispatchers.Main) {
                    populateElements()
                }
            }
            else -> Log.v(TAG, "${response.first}: ${response.second}")
        }
    }

    private fun populateElements() {
        activity?.title = "${game.title} (${game.console})"
        Picasso.get()
                .load(Consts.BASE_URL + game.imageIcon)
                .placeholder(R.drawable.game_placeholder)
                .into(view?.findViewById(R.id.game_details_image_icon))
        view?.findViewById<TextView>(R.id.game_details_developer)?.text = getString(R.string.developed, game.developer)
        view?.findViewById<TextView>(R.id.game_details_publisher)?.text = getString(R.string.published, game.publisher)
        view?.findViewById<TextView>(R.id.game_details_genre)?.text = getString(R.string.genre, game.genre)
        view?.findViewById<TextView>(R.id.game_details_release_date)?.text = getString(R.string.released, game.released)
    }

    companion object {
        private val TAG = Consts.BASE_TAG + GameDetailsFragment::class.java.simpleName
    }

}