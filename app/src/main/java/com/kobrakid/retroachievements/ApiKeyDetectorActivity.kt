package com.kobrakid.retroachievements

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class ApiKeyDetectorActivity : AppCompatActivity() {
    private var username: String? = null
    private var apiKey: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_pull)
        setResult(Consts.FAILURE)
        val myWebView = findViewById<WebView>(R.id.apiKeyWebView)
        myWebView.settings.javaScriptEnabled = true
        myWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                myWebView.evaluateJavascript(
                        "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/\"([^\\s]*)\"/)[0].slice(1, -1); })();"
                ) { u: String? -> username = u }
                myWebView.evaluateJavascript(
                        "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/[A-z0-9]{32}/)[0]; })();"
                ) { key: String? -> apiKey = key }
            }
        }
        myWebView.loadUrl(Consts.BASE_URL + "/" + Consts.KEY_URL)
    }

    override fun onBackPressed() {
        setResult(Consts.CANCELLED)
        super.onBackPressed()
    }

    fun detectApiKey(@Suppress("UNUSED_PARAMETER") view: View?) {
        val resultIntent = Intent()
        resultIntent.putExtra(getString(R.string.ra_user), username)
        resultIntent.putExtra(getString(R.string.ra_api_key), apiKey)
        setResult(Consts.SUCCESS, resultIntent)
        finish()
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") view: View?) {
        onBackPressed()
    }
}