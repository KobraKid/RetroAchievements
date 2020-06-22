package com.kobrakid.retroachievements.fragment

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.R

/**
 * This fragment holds static info about the app.
 */
class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = "About RetroAchievements"
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        for (i in 0 until (view.findViewById<View>(R.id.about_container) as ViewGroup).childCount) {
            if ((view.findViewById<View>(R.id.about_container) as ViewGroup).getChildAt(i) is TextView)
                ((view.findViewById<View>(R.id.about_container) as ViewGroup).getChildAt(i) as TextView).movementMethod =
                        LinkMovementMethod.getInstance()
        }
        return view
    }
}