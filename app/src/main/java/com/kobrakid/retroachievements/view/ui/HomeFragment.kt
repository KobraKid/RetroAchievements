package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentHomeBinding
import com.kobrakid.retroachievements.viewmodel.HomeViewModel
import com.squareup.picasso.Picasso

class HomeFragment : Fragment(), View.OnClickListener {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        activity?.title = "Home"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeViewMore.setOnClickListener(this)
        binding.homeUsername.visibility = View.VISIBLE
        viewModel.masteries.observe(viewLifecycleOwner) {
            binding.masteries.removeAllViews()
            it.forEach { mastery ->
                val imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    adjustViewBounds = true
                    if (mastery.id.isDigitsOnly()) {
                        id = mastery.id.toInt()
                        setOnClickListener(this@HomeFragment)
                    } else {
                        Log.w(TAG, "Trophy was not a valid RA game: $mastery")
                    }
                    if (mastery.mastered) background = ContextCompat.getDrawable(context, R.drawable.image_view_border)
                }
                Picasso.get()
                        .load(Consts.BASE_URL + mastery.icon)
                        .placeholder(R.drawable.game_placeholder)
                        .into(imageView)
                binding.masteries.addView(imageView)
            }
            binding.masteries.visibility = View.VISIBLE
        }
        viewModel.recentGames.observe(viewLifecycleOwner) { recentGames ->
            binding.homeUsername.text = (activity as MainActivity?)?.user?.username
            Picasso.get()
                    .load(Consts.BASE_URL + "/" + Consts.USER_PIC_POSTFIX + "/" + (activity as MainActivity?)?.user?.username + ".png")
                    .placeholder(R.drawable.favicon)
                    .into(binding.homeProfilePicture)
            binding.homeStats.apply {
                text = getString(R.string.score_rank, (activity as MainActivity?)?.user?.totalPoints, (activity as MainActivity?)?.user?.rank)
                visibility = View.VISIBLE
            }
            binding.homeRecentGames.removeViews(0, binding.homeRecentGames.childCount - 1)
            recentGames.forEach { game ->
                binding.homeRecentGames.addView(View.inflate(context, R.layout.view_holder_game_summary, null).also {
                    it.id = game.id.toInt()
                    Picasso.get()
                            .load(Consts.BASE_URL + game.imageIcon)
                            .placeholder(R.drawable.game_placeholder)
                            .into(it.findViewById<ImageView>(R.id.game_summary_image_icon))
                    it.findViewById<TextView>(R.id.game_summary_title).text = game.title
                    it.findViewById<TextView>(R.id.game_summary_stats).text = getString(R.string.game_stats,
                            game.numAchievementsEarned.coerceAtLeast(game.numAchievementsEarnedHC),
                            game.totalAchievements,
                            game.earnedPoints,
                            game.totalPoints)
                    if (game.id.isDigitsOnly()) {
                        it.id = game.id.toInt()
                        it.setOnClickListener(this@HomeFragment)
                    }
                }, recentGames.indexOf(game))
            }
            binding.homeViewMore.visibility = View.VISIBLE
        }
        viewModel.setUser((activity as MainActivity?)?.user?.username)

        binding.homeScrollview.background =
                if ((activity as MainActivity?)?.user?.username?.isNotEmpty() == true) null
                else context?.let { ContextCompat.getDrawable(it, R.drawable.ic_baseline_arrow_up_left) }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.home_view_more -> Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_recentGamesFragment)
            else -> Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToGameDetailsFragment(view.id.toString()))
        }
    }

    companion object {
        private val TAG = Consts.BASE_TAG + HomeFragment::class.java.simpleName
    }
}