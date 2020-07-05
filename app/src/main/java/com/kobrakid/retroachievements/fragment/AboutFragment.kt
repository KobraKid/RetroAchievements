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
        requireActivity().title = "About RetroAchievements"
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        for (i in 0 until view.findViewById<ViewGroup>(R.id.about_container).childCount) {
            if (view.findViewById<ViewGroup>(R.id.about_container).getChildAt(i) is TextView)
                (view.findViewById<ViewGroup>(R.id.about_container).getChildAt(i) as TextView).movementMethod =
                        LinkMovementMethod.getInstance()
        }
    }
}