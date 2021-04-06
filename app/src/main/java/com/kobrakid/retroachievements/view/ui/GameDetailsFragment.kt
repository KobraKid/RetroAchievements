package com.kobrakid.retroachievements.view.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentGameDetailsBinding
import com.kobrakid.retroachievements.view.adapter.GameDetailsPagerAdapter
import com.kobrakid.retroachievements.viewmodel.GameDetailsViewModel
import com.squareup.picasso.Picasso

class GameDetailsFragment : Fragment(R.layout.fragment_game_details), View.OnClickListener {

    private val args: GameDetailsFragmentArgs by navArgs()
    private val viewModel: GameDetailsViewModel by viewModels()
    private var _binding: FragmentGameDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGameDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true
        activity?.title = "Game Details"

        binding.gameDetailsViewPager.apply {
            adapter = GameDetailsPagerAdapter(childFragmentManager, args.id)
            offscreenPageLimit = GameDetailsPagerAdapter.GAME_DETAILS_PAGES - 1
        }

        // These views only exist in landscape, thus they require null-safe access
        binding.gameDetailsButtonPage0?.setOnClickListener(this)
        binding.gameDetailsButtonPage1?.setOnClickListener(this)
        binding.gameDetailsButtonPage2?.setOnClickListener(this)
        binding.gameDetailsButtonPage3?.setOnClickListener(this)

        viewModel.game.observe(viewLifecycleOwner) { game ->
            activity?.title = "${game.title} (${game.consoleName})"
            Picasso.get()
                    .load(Consts.BASE_URL + game.imageIcon)
                    .placeholder(R.drawable.game_placeholder)
                    .into(binding.gameDetailsImageIcon)
            binding.gameDetailsDeveloper.text = getString(R.string.developed, game.developer)
            binding.gameDetailsPublisher.text = getString(R.string.published, game.publisher)
            binding.gameDetailsGenre.text = getString(R.string.genre, game.genre)
            binding.gameDetailsReleaseDate.text = getString(R.string.released, game.released)
        }
        viewModel.getGameInfoForUser((activity as MainActivity?)?.user?.username, args.id)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_overflow, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_forum -> {
                val forumUrl = Consts.BASE_URL + "/" + Consts.FORUM_POSTFIX + viewModel.game.value?.forumTopicID
                val forumIntent = Intent(Intent.ACTION_VIEW)
                forumIntent.data = Uri.parse(forumUrl)
                startActivity(forumIntent)
            }
            R.id.action_webpage -> {
                val raUrl = Consts.BASE_URL + "/" + Consts.GAME_POSTFIX + "/" + args.id
                val raIntent = Intent(Intent.ACTION_VIEW)
                raIntent.data = Uri.parse(raUrl)
                startActivity(raIntent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onClick(view: View?) {
        val page = when (view?.id) {
            R.id.game_details_button_page_0 -> 0
            R.id.game_details_button_page_1 -> 1
            R.id.game_details_button_page_2 -> 2
            R.id.game_details_button_page_3 -> 3
            else -> 0
        }
        view?.rootView?.findViewById<View>(R.id.game_details_frame)
                ?.findFragment<AchievementSummaryFragment>()
                ?.childFragmentManager
                ?.popBackStackImmediate()
        binding.gameDetailsViewPager.currentItem = page
    }

}