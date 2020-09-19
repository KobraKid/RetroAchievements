package com.kobrakid.retroachievements.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.activity.MainActivity

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener {

    private val args: LoginFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var loginTextView: TextView
    private lateinit var apiKeyTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Home"
        navController = Navigation.findNavController(view)
        view.findViewById<View>(R.id.login_button).setOnClickListener(this)
        view.findViewById<View>(R.id.cancel_button).setOnClickListener(this)
        view.findViewById<View>(R.id.login_api_help).setOnClickListener(this)
        loginTextView = view.findViewById(R.id.login_username)
        loginTextView.text = trimQuotes(args.username)
        apiKeyTextView = view.findViewById(R.id.login_api_key)
        apiKeyTextView.text = trimQuotes(args.apiKey)
        // Log in if the user hits enter on the keyboard
        apiKeyTextView.setOnEditorActionListener { _, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_SEND) {
                onClick(view.findViewById(R.id.login))
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.login_button -> {
                val raUser = loginTextView.text.toString()
                val raApi = apiKeyTextView.text.toString()
                if (raUser.isNotBlank() && raApi.isNotBlank()) {
                    // Successfully logged in, save the new credentials
                    context?.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)?.edit()?.putString(getString(R.string.ra_user), raUser)?.apply()
                    context?.getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)?.edit()?.putString(getString(R.string.ra_api_key), raApi)?.apply()
                    (activity as MainActivity?)?.setCredentials(raUser, raApi)
                    Toast.makeText(context?.applicationContext, getString(R.string.new_login_welcome, raUser), Toast.LENGTH_SHORT).show()
                }
                navController.navigate(R.id.action_loginFragment_to_homeFragment)
            }
            R.id.cancel_button -> navController.navigate(R.id.action_loginFragment_to_homeFragment)
            R.id.login_api_help ->
                AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogLogin))
                        .setTitle(getString(R.string.api_detect_dialog_title))
                        .setMessage(getString(R.string.api_detect_dialog_desc))
                        .setPositiveButton(getString(R.string.api_detect_go)) { _: DialogInterface?, _: Int -> navController.navigate(R.id.action_loginFragment_to_apiKeyDetectorFragment) }
                        .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
                        .create()
                        .show()
        }
    }

    private fun trimQuotes(s: String): String {
        return if (s.isEmpty() || s.isBlank() || s.indexOf("\"") < 0 || s.indexOf("\"") + 1 > s.lastIndexOf("\"")) s
        else s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""))
    }
}