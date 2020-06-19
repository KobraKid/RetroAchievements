package com.kobrakid.retroachievements;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ApiPullActivity extends AppCompatActivity {

    private String username;
    private String apiKey;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_pull);
        setResult(Consts.FAILURE);
        WebView myWebView = findViewById(R.id.apiKeyWebView);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                myWebView.evaluateJavascript(
                        "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/\"([^\\s]*)\"/)[0].slice(1, -1); })();",
                        u -> { username = u; });
                myWebView.evaluateJavascript(
                        "(function() { return document.getElementById('ctorDiv').getElementsByTagName('code')[1].innerHTML.match(/[A-z0-9]{32}/)[0]; })();",
                        key -> { apiKey = key; });
            }
        });
        myWebView.loadUrl(Consts.BASE_URL + "/" + Consts.KEY_URL);
    }

    @Override
    public void onBackPressed() {
        setResult(Consts.CANCELLED);
        super.onBackPressed();
    }

    public void detectApiKey(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(getString(R.string.ra_user), username);
        resultIntent.putExtra(getString(R.string.ra_api_key), apiKey);
        setResult(Consts.SUCCESS, resultIntent);
        finish();
    }

    public void cancel(@SuppressWarnings("unused") View view) {
        onBackPressed();
    }
}