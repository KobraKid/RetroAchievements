package com.kobrakid.retroachievements.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.view.ui.ApiKeyDetectorFragmentDirections

class ApiKeyDetectorViewModel : ViewModel(), View.OnClickListener {

    var username: String = ""
    var apiKey: String = ""

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.apiKeyDetectButton -> {
                Navigation.findNavController(view).navigate(ApiKeyDetectorFragmentDirections.actionApiKeyDetectorFragmentToLoginFragment(username, apiKey))
            }
            R.id.apiKeyCancelButton -> {
                Navigation.findNavController(view).popBackStack()
            }
        }
    }

}