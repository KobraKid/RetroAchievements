package com.kobrakid.retroachievements.fragment

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RetroAchievementsApi
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * This [Fragment] displays images relating to the current game.
 */
class GameImagesFragment : Fragment(R.layout.view_pager_game_images) {

    private var boxURL = ""
    private var titleURL = ""
    private var ingameURL = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true
        if (savedInstanceState == null) {
            val ctx = context?.applicationContext
            val id = arguments?.getString("GameID", "0")
            CoroutineScope(IO).launch {
                if (ctx != null && id != null)
                    RetroAchievementsApi.GetGame(ctx, id) { parseGameImages(view, it) }
            }
        } else {
            populateImages(view)
        }
    }

    private suspend fun parseGameImages(view: View, response: Pair<RetroAchievementsApi.RESPONSE, String>) {
        when (response.first) {
            RetroAchievementsApi.RESPONSE.ERROR -> {
                Log.w(TAG, response.second)
            }
            RetroAchievementsApi.RESPONSE.GET_GAME -> {
                withContext(Default) {
                    try {
                        val reader = JSONObject(response.second)
                        boxURL = reader.getString("ImageBoxArt")
                        titleURL = reader.getString("ImageTitle")
                        ingameURL = reader.getString("ImageIngame")
                        withContext(Main) { populateImages(view) }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse game images", e)
                    }
                    withContext(Main) {
                        populateImages(view)
                    }
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    private fun populateImages(view: View) {
        Picasso.get()
                .load(Consts.BASE_URL + boxURL)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                        Log.i(TAG, "Loaded image $bitmap from $from")
                        val drawable: Drawable = BitmapDrawable(requireContext().resources, bitmap)
                        val scale = (view.findViewById<View>(R.id.card_0_boxart).width - 16) / drawable.intrinsicWidth
                        drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                        view.findViewById<TextView>(R.id.image_boxart).setCompoundDrawables(null, drawable, null, null)
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        view.findViewById<View>(R.id.card_0_boxart).visibility = View.GONE
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        Picasso.get()
                .load(Consts.BASE_URL + titleURL)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                        val drawable: Drawable = BitmapDrawable(requireContext().resources, bitmap)
                        val scale = (view.findViewById<View>(R.id.card_1_title).width - 16) / drawable.intrinsicWidth
                        drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                        view.findViewById<TextView>(R.id.image_title).setCompoundDrawables(null, drawable, null, null)
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        view.findViewById<View>(R.id.card_1_title).visibility = View.GONE
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        Picasso.get()
                .load(Consts.BASE_URL + ingameURL)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                        val drawable: Drawable = BitmapDrawable(requireContext().resources, bitmap)
                        val scale = (view.findViewById<View>(R.id.card_2_ingame).width - 16) / drawable.intrinsicWidth
                        drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                        view.findViewById<TextView>(R.id.image_ingame).setCompoundDrawables(null, drawable, null, null)
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        view.findViewById<View>(R.id.card_2_ingame).visibility = View.GONE
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
    }

    companion object {
        private val TAG = GameImagesFragment::class.java.simpleName
    }
}