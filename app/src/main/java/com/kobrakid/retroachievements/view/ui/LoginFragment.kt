package com.kobrakid.retroachievements.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentLoginBinding
import com.kobrakid.retroachievements.viewmodel.LoginViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var navController: NavController
    private val args: LoginFragmentArgs by navArgs()
    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Home"
        navController = Navigation.findNavController(view)
        viewModel.init(binding, args.username, args.apiKey)
        binding.loginButton.setOnClickListener(viewModel)
        binding.cancelButton.setOnClickListener(viewModel)
        binding.loginApiHelp.setOnClickListener(viewModel)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
    }
}