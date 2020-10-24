package com.kobrakid.retroachievements.viewmodel

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentLoginBinding
import com.kobrakid.retroachievements.view.ui.MainActivity

class LoginViewModel : ViewModel(), View.OnClickListener {

    private var binding: FragmentLoginBinding? = null

    fun init(binding: FragmentLoginBinding, username: String, apiKey: String) {
        this.binding = binding
        binding.loginUsername.setText(trimQuotes(username))
        binding.loginApiKey.apply {
            setText(trimQuotes(apiKey))
            // Log in if the user hits enter on the keyboard
            setOnEditorActionListener { _, actionID, _ ->
                if (actionID == EditorInfo.IME_ACTION_SEND) {
                    binding.login.callOnClick()
                    return@setOnEditorActionListener true
                }
                false
            }
        }
    }

    private fun trimQuotes(s: String): String {
        return if (s.isEmpty() || s.isBlank() || s.indexOf("\"") < 0 || s.indexOf("\"") + 1 > s.lastIndexOf("\"")) s
        else s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""))
    }

    override fun onClick(view: View) {
        val nav = Navigation.findNavController(view)
        val context = view.context
        when (view.id) {
            R.id.login_button -> {
                val raUser = binding?.loginUsername?.text.toString()
                val raApi = binding?.loginApiKey?.text.toString()
                if (raUser.isNotBlank() && raApi.isNotBlank()) {
                    // Successfully logged in, save the new credentials
                    context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                            .edit()
                            .putString(context.getString(R.string.ra_user), raUser)
                            .apply()
                    context.getSharedPreferences(context.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
                            .edit()
                            .putString(context.getString(R.string.ra_api_key), raApi)
                            .apply()
                    (context as MainActivity?)?.setCredentials(raUser, raApi)
                    Toast.makeText(context.applicationContext, context.getString(R.string.new_login_welcome, raUser), Toast.LENGTH_SHORT).show()
                }
                nav.navigate(R.id.action_loginFragment_to_homeFragment)
            }
            R.id.cancel_button -> nav.navigate(R.id.action_loginFragment_to_homeFragment)
            R.id.login_api_help ->
                AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogLogin))
                        .setTitle(context.getString(R.string.api_detect_dialog_title))
                        .setMessage(context.getString(R.string.api_detect_dialog_desc))
                        .setPositiveButton(context.getString(R.string.api_detect_go)) { _: DialogInterface?, _: Int -> nav.navigate(R.id.action_loginFragment_to_apiKeyDetectorFragment) }
                        .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
                        .create()
                        .show()
        }
    }
}