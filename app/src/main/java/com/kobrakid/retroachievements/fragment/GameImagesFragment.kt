package com.kobrakid.retroachievements.fragment

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class GameImagesFragment : Fragment() {

    private var boxURL = ""
    private var titleURL = ""
    private var ingameURL = ""
    private var isActive = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        val view = inflater.inflate(R.layout.view_pager_game_images, container, false)
        if (savedInstanceState == null) {
            val ctx = context?.applicationContext
            val id = arguments?.getString("GameID", "0")
            CoroutineScope(IO).launch {
                if (ctx != null && id != null)
                    RetroAchievementsApi.GetGame(ctx, id) { parseGameImages(view, it) }
            }
        } else {
            awaitImagePopulation(view)
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onStart() {
        super.onStart()
        isActive = true
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
                        withContext(Main) { awaitImagePopulation(view) }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Couldn't parse game images", e)
                    }
                    withContext(Main) {
                        awaitImagePopulation(view)
                    }
                }
            }
            else -> {
                Log.v(TAG, "${response.first}: ${response.second}")
            }
        }
    }

    private fun awaitImagePopulation(view: View) {
        val res = view.context.resources
        view.findViewById<View>(R.id.card_0_boxart).post {
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + boxURL)
                    .into(object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                            val drawable: Drawable = BitmapDrawable(res, bitmap)
                            val scale = (view.findViewById<View>(R.id.card_0_boxart).width - 16) / drawable.intrinsicWidth
                            drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                            view.findViewById<TextView>(R.id.image_boxart).setCompoundDrawables(null, drawable, null, null)
                        }

                        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                            view.findViewById<View>(R.id.card_0_boxart).visibility = View.GONE
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    })
        }
        view.findViewById<View>(R.id.card_1_title).post {
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + titleURL)
                    .into(object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                            val drawable: Drawable = BitmapDrawable(res, bitmap)
                            val scale = (view.findViewById<View>(R.id.card_1_title).width - 16) / drawable.intrinsicWidth
                            drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                            view.findViewById<TextView>(R.id.image_title).setCompoundDrawables(null, drawable, null, null)
                        }

                        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                            view.findViewById<View>(R.id.card_1_title).visibility = View.GONE
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    })
        }
        view.findViewById<View>(R.id.card_2_ingame).post {
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + ingameURL)
                    .into(object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                            val drawable: Drawable = BitmapDrawable(res, bitmap)
                            val scale = (view.findViewById<View>(R.id.card_2_ingame).width - 16) / drawable.intrinsicWidth
                            drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                            view.findViewById<TextView>(R.id.image_ingame).setCompoundDrawables(null, drawable, null, null)
                        }

                        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
                            view.findViewById<View>(R.id.card_2_ingame).visibility = View.GONE
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    })
        }
    }

    companion object {
        private val TAG = GameImagesFragment::class.java.simpleName
    }
}