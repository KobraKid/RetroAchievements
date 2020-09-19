package com.kobrakid.retroachievements.view.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R

class ApiKeyDetectorFragment : Fragment(R.layout.fragment_api_key_detector), View.OnClickListener {

    private var username: String = ""
    private var apiKey: String = ""
    private lateinit var navController: NavController

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<View>(R.id.apiKeyDetectButton).setOnClickListener(this)
        view.findViewById<View>(R.id.apiKeyCancelButton).setOnClickListener(this)
        val myWebView = view.findViewById<WebView>(R.id.apiKeyWebView)
        myWebView.settings.javaScriptEnabled = true
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                myWebView.evaluateJavascript(
                        "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/\"([^\\s]*)\"/)[0].slice(1, -1); })();"
                ) { u: String? -> username = u ?: "" }
                myWebView.evaluateJavascript(
                        "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/[A-z0-9]{32}/)[0]; })();"
                ) { key: String? -> apiKey = key ?: "" }
            }
        }
        myWebView.loadUrl(Consts.BASE_URL + "/" + Consts.KEY_URL)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.apiKeyDetectButton -> {
                navController.navigate(ApiKeyDetectorFragmentDirections.actionApiKeyDetectorFragmentToLoginFragment(username, apiKey))
            }
            R.id.apiKeyCancelButton -> {
                navController.popBackStack()
            }
        }
    }

}