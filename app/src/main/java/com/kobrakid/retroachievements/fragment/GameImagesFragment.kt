package com.kobrakid.retroachievements.fragment

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.RAAPICallback
import com.kobrakid.retroachievements.RAAPIConnection
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import org.json.JSONException
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class GameImagesFragment : Fragment(), RAAPICallback {
    private var boxURL: String? = null
    private var titleURL: String? = null
    private var ingameURL: String? = null
    private var myView: View? = null
    private var isActive = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retainInstance = true
        myView = inflater.inflate(R.layout.view_pager_game_images, container, false)
        if (savedInstanceState == null)
            RAAPIConnection(context).GetGameInfo(arguments?.getString("GameID", "0"), this)
        else
            myView!!.findViewById<View>(R.id.card_0_boxart).post { populateViews() }

        return myView
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

    override fun callback(responseCode: Int, response: String) {
        if (!isActive) return
        if (responseCode == RAAPIConnection.RESPONSE_GET_GAME_INFO) {
            try {
                val reader = JSONObject(response)
                boxURL = reader.getString("ImageBoxArt")
                titleURL = reader.getString("ImageTitle")
                ingameURL = reader.getString("ImageIngame")
                populateViews()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun populateViews() {
        val res = myView?.context?.resources
        Picasso.get()
                .load(Consts.BASE_URL + "/" + boxURL)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                        val drawable: Drawable = BitmapDrawable(res, bitmap)
                        val scale = ((myView?.findViewById<View>(R.id.card_0_boxart)?.width
                                ?: 0) - 16) / drawable.intrinsicWidth
                        drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                        (myView?.findViewById<View>(R.id.image_boxart) as TextView).setCompoundDrawables(null, drawable, null, null)
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        Picasso.get()
                .load(Consts.BASE_URL + "/" + titleURL)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                        val drawable: Drawable = BitmapDrawable(res, bitmap)
                        val scale = ((myView?.findViewById<View>(R.id.card_1_title)?.width
                                ?: 0) - 16) / drawable.intrinsicWidth
                        drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                        (myView?.findViewById<View>(R.id.image_title) as TextView).setCompoundDrawables(null, drawable, null, null)
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        Picasso.get()
                .load(Consts.BASE_URL + "/" + ingameURL)
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                        val drawable: Drawable = BitmapDrawable(res, bitmap)
                        val scale = ((myView?.findViewById<View>(R.id.card_2_ingame)?.width
                                ?: 0) - 16) / drawable.intrinsicWidth
                        drawable.setBounds(0, 0, drawable.intrinsicWidth * scale, drawable.intrinsicHeight * scale)
                        (myView?.findViewById<View>(R.id.image_ingame) as TextView).setCompoundDrawables(null, drawable, null, null)
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
    }
}