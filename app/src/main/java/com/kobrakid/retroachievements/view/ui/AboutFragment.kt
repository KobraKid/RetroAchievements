package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentAboutBinding

/**
 * This fragment holds static info about the app.
 */
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.title = "About RetroAchievements"
        for (i in 0 until view.findViewById<ViewGroup>(R.id.about_container).childCount) {
            if (binding.aboutContainer.getChildAt(i) is TextView) {
                (binding.aboutContainer.getChildAt(i) as TextView).movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }
}