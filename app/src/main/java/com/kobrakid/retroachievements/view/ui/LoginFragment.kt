package com.kobrakid.retroachievements.view.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentLoginBinding
import com.kobrakid.retroachievements.viewmodel.LoginViewModel

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener {

    private val args: LoginFragmentArgs by navArgs()
    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Home"
        binding.loginButton.setOnClickListener(this)
        binding.cancelButton.setOnClickListener(this)
        binding.loginApiHelp.setOnClickListener(this)
        binding.loginApiKey.setOnEditorActionListener { _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_SEND) {
                binding.login.callOnClick()
            }
            false
        }
        viewModel.username.observe(viewLifecycleOwner) {
            binding.loginUsername.setText(it)
        }
        viewModel.apiKey.observe(viewLifecycleOwner) {
            binding.loginApiKey.setText(it)
        }
        viewModel.init(args.username, args.apiKey)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.login_button -> {
                val raUser = binding.loginUsername.text.toString()
                val raApi = binding.loginApiKey.text.toString()
                if (raUser.isNotBlank() && raApi.isNotBlank()) {
                    // Successfully logged in, save the new credentials
                    context?.getSharedPreferences(context?.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                            ?.edit()
                            ?.putString(context?.getString(R.string.ra_user), raUser)
                            ?.apply()
                    context?.getSharedPreferences(context?.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                            ?.edit()
                            ?.putString(context?.getString(R.string.ra_api_key), raApi)
                            ?.apply()
                    (context as MainActivity?)?.setCredentials(raUser, raApi)
                    Toast.makeText(context?.applicationContext, context?.getString(R.string.new_login_welcome, raUser), Toast.LENGTH_SHORT).show()
                }
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment)
            }
            R.id.cancel_button -> Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment)
            R.id.login_api_help ->
                AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogLogin))
                        .setTitle(context?.getString(R.string.api_detect_dialog_title))
                        .setMessage(context?.getString(R.string.api_detect_dialog_desc))
                        .setPositiveButton(context?.getString(R.string.api_detect_go)) { _: DialogInterface?, _: Int ->
                            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_apiKeyDetectorFragment)
                        }
                        .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
                        .create()
                        .show()
        }
    }
}