package com.kobrakid.retroachievements.view.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentApiKeyDetectorBinding

class ApiKeyDetectorFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentApiKeyDetectorBinding? = null
    private val binding get() = _binding!!
    private var username: String = ""
    private var apiKey: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentApiKeyDetectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apiKeyDetectButton.setOnClickListener(this)
        binding.apiKeyCancelButton.setOnClickListener(this)
        binding.apiKeyWebView.apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    view.loadUrl(request.url.toString())
                    return false
                }

                override fun onPageFinished(view: WebView, url: String) {
                    evaluateJavascript(
                            "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/\"([^\\s]*)\"/)[0].slice(1, -1); })();"
                    ) { u: String? -> username = u ?: "" }
                    evaluateJavascript(
                            "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/[A-z0-9]{32}/)[0]; })();"
                    ) { key: String? -> apiKey = key ?: "" }
                }
            }
            loadUrl(Consts.BASE_URL + "/" + Consts.KEY_URL)
        }
    }

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