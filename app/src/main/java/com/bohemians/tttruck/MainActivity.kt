package com.bohemians.tttruck

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat.startActivity

class MainActivity : AppCompatActivity() {
    lateinit var myWebView: WebView
    var backPressedTime = 0L
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myWebView = findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true
        myWebView.webViewClient = MyWebViewClient()
        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")
        myWebView.loadUrl("https://www.tttruck.co.kr")

    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         //Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
            return true
        }
        if(System.currentTimeMillis() - backPressedTime > 3000){
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "앱을 종료하시려면 뒤로가기 버튼을 한번 더 눌러주세요.", LENGTH_SHORT).show()
            return true
        }
         //If it wasn't the Back key or there's no web page history, bubble up to the default
         //system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event)
    }
    private class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (Uri.parse(url).host == "www.tttruck.co.kr") {
                // This is my website, so do not override; let my WebView load the page
                return false
            }
            //Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            super.shouldOverrideUrlLoading(view, url)
            return true
        }
    }
}