package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentUserSummaryBinding
import com.kobrakid.retroachievements.viewmodel.UserSummaryViewModel
import com.squareup.picasso.Picasso

class UserSummaryFragment : Fragment() {

    private lateinit var navController: NavController
    private val args: UserSummaryFragmentArgs by navArgs()
    private val viewModel: UserSummaryViewModel by viewModels()
    private var _binding: FragmentUserSummaryBinding? = null
    private val binding get() = _binding!! // Only valid between onCreateView and onDestroyView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentUserSummaryBinding.inflate(inflater, container, false)
        activity?.title = "User Summary"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        activity?.title = "User Summary: ${args.username}"
        viewModel.userSummaryState.observe(viewLifecycleOwner, { user ->
            if (user.username.isNotEmpty()) {
                Picasso.get()
                        .load(user.userPic)
                        .placeholder(R.drawable.user_placeholder)
                        .into(binding.userSummaryImage)
                binding.userSummaryUsername.text = user.username
                binding.userSummaryMotto.text = user.motto
                binding.userSummaryRank.text = user.rank
                binding.userSummaryPoints.text = user.totalPoints
                binding.userSummaryRatio.text = user.retroRatio
                binding.userSummaryJoined.text = user.memberSince
            }
        })
        viewModel.setUsername(context, args.username)
    }

    companion object {
        private val TAG = Consts.BASE_TAG + UserSummaryFragment::class.java.simpleName
    }

}