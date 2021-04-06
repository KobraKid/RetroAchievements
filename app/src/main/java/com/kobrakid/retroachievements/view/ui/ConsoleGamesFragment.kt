package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kobrakid.retroachievements.databinding.FragmentConsoleGamesBinding
import com.kobrakid.retroachievements.view.adapter.GameSummaryAdapter
import com.kobrakid.retroachievements.viewmodel.ConsoleGamesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

/**
 * This Activity lists all of the games of a certain console.
 */
class ConsoleGamesFragment : Fragment(), View.OnClickListener {

    private val args: ConsoleGamesFragmentArgs by navArgs()
    private val viewModel: ConsoleGamesViewModel by viewModels()
    private var _binding: FragmentConsoleGamesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConsoleGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = args.console?.consoleName
        binding.listGames.apply {
            adapter = GameSummaryAdapter(this@ConsoleGamesFragment, context)
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.listHidingProgress.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.consoleGamesList.observe(viewLifecycleOwner) {
            (binding.listGames.adapter as GameSummaryAdapter?)?.setGames(it)
            binding.listNoGames.visibility = if (it.isEmpty() && viewModel.loading.value == false) View.VISIBLE else View.GONE
        }
        binding.listGamesFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                (binding.listGames.adapter as GameSummaryAdapter?)?.filter?.filter(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        CoroutineScope(Main).launch { viewModel.setConsoleID(args.console?.id) }
    }

    override fun onClick(view: View) {
        Navigation.findNavController(view).navigate(
                ConsoleGamesFragmentDirections.actionConsoleGamesFragmentToGameDetailsFragment(
                        view.id.toString()))
    }
}