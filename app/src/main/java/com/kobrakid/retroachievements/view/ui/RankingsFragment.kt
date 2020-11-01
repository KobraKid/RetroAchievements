package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentRankingsBinding
import com.kobrakid.retroachievements.view.adapter.UserRankingAdapter
import com.kobrakid.retroachievements.viewmodel.RankingsViewModel

class RankingsFragment : Fragment(), View.OnClickListener {

    private val viewModel: RankingsViewModel by viewModels()
    private var _binding: FragmentRankingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRankingsBinding.inflate(inflater, container, false)
        activity?.title = "User Rankings"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rankingsUsers.apply {
            adapter = UserRankingAdapter(this@RankingsFragment, (activity as MainActivity?)?.user?.username)
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.users.observe(viewLifecycleOwner) {
            (binding.rankingsUsers.adapter as UserRankingAdapter).populateUsers(it)
        }
        viewModel.getTopUsers((activity as MainActivity?)?.user?.username)
    }

    override fun onClick(view: View) {
        view.findViewById<TextView>(R.id.participant_username)?.let {
            Navigation.findNavController(view).navigate(
                    RankingsFragmentDirections.actionRankingsFragmentToUserSummaryFragment(
                            it.text.toString()))
        }
    }
}