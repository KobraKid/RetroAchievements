package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.databinding.FragmentGameCommentsBinding
import com.kobrakid.retroachievements.view.adapter.GameCommentsAdapter
import com.kobrakid.retroachievements.viewmodel.GameCommentsViewModel

/**
 * A Fragment to hold recent game comments.
 */
class GameCommentsFragment : Fragment() {

    private val viewModel: GameCommentsViewModel by viewModels()
    private var _binding: FragmentGameCommentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentGameCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gameCommentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GameCommentsAdapter()
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.gameCommentsProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.comments.observe(viewLifecycleOwner) {
            (binding.gameCommentsRecyclerView.adapter as GameCommentsAdapter).setComments(it)
        }
        viewModel.setId(arguments?.getString("GameID") ?: "0")
    }
}