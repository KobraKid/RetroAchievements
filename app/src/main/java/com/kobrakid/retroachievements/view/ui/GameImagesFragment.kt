package com.kobrakid.retroachievements.view.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.databinding.FragmentGameImagesBinding
import com.kobrakid.retroachievements.viewmodel.GameImagesViewModel
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlin.math.min

/**
 * This [Fragment] displays images relating to the current game.
 */
class GameImagesFragment : Fragment() {

    private val viewModel: GameImagesViewModel by viewModels()
    private var _binding: FragmentGameImagesBinding? = null
    private val binding get() = _binding!!

    private val scrollHeight get() = _binding?.gameImagesScrollview?.height?.minus(32) ?: 0
    private val scrollWidth get() = _binding?.gameImagesScrollview?.width?.minus(32) ?: 0

    private lateinit var boxartTarget: Target
    private lateinit var titleTarget: Target
    private lateinit var ingameTarget: Target

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boxartTarget = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                try {
                    val drawable: Drawable = BitmapDrawable(resources, bitmap)
                    val scale = min(
                            scrollHeight / drawable.intrinsicHeight.toDouble(),
                            scrollWidth / drawable.intrinsicWidth.toDouble())
                    drawable.setBounds(0, 0, (drawable.intrinsicWidth * scale).toInt(), (drawable.intrinsicHeight * scale).toInt())
                    _binding?.imageBoxart?.setCompoundDrawables(null, drawable, null, null)
                    Log.v(TAG, "Loaded image 0: $bitmap from $from @ ${scale}x scale (${drawable.intrinsicWidth} x ${drawable.intrinsicHeight})")
                } catch (e: IllegalStateException) {
                    Log.w(TAG, e)
                }
            }

            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                _binding?.card0Boxart?.visibility = View.GONE
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        viewModel.boxURL.observe(viewLifecycleOwner) {
            Picasso.get()
                    .load(Consts.BASE_URL + it)
                    .into(boxartTarget)
        }
        titleTarget = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                try {
                    val drawable: Drawable = BitmapDrawable(resources, bitmap)
                    val scale = min(
                            scrollHeight / drawable.intrinsicHeight.toDouble(),
                            scrollWidth / drawable.intrinsicWidth.toDouble())
                    drawable.setBounds(0, 0, (drawable.intrinsicWidth * scale).toInt(), (drawable.intrinsicHeight * scale).toInt())
                    _binding?.imageTitle?.setCompoundDrawables(null, drawable, null, null)
                    Log.v(TAG, "Loaded image 1: $bitmap from $from @ ${scale}x scale (${drawable.intrinsicWidth} x ${drawable.intrinsicHeight})")
                } catch (e: IllegalStateException) {
                    Log.w(TAG, e)
                }
            }

            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                _binding?.card1Title?.visibility = View.GONE
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        viewModel.titleURL.observe(viewLifecycleOwner) {
            Picasso.get()
                    .load(Consts.BASE_URL + it)
                    .into(titleTarget)
        }
        ingameTarget = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                try {
                    val drawable: Drawable = BitmapDrawable(resources, bitmap)
                    val scale = min(
                            scrollHeight / drawable.intrinsicHeight.toDouble(),
                            scrollWidth / drawable.intrinsicWidth.toDouble())
                    drawable.setBounds(0, 0, (drawable.intrinsicWidth * scale).toInt(), (drawable.intrinsicHeight * scale).toInt())
                    _binding?.imageIngame?.setCompoundDrawables(null, drawable, null, null)
                    Log.v(TAG, "Loaded image 2: $bitmap from $from @ ${scale}x scale (${drawable.intrinsicWidth} x ${drawable.intrinsicHeight})")
                } catch (e: IllegalStateException) {
                    Log.w(TAG, e)
                }
            }

            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                _binding?.card2Ingame?.visibility = View.GONE
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        viewModel.ingameURL.observe(viewLifecycleOwner) {
            Picasso.get()
                    .load(Consts.BASE_URL + it)
                    .into(ingameTarget)
        }
        viewModel.setId(arguments?.getString("GameID", "0") ?: "0")
    }

    companion object {
        private val TAG = Consts.BASE_TAG + GameImagesFragment::class.java.simpleName
    }
}