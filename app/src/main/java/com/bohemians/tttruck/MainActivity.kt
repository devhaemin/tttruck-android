package com.bohemians.tttruck

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.bohemians.tttruck.POJO.User
import com.bohemians.tttruck.POJO.UserCookie
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.net.URLDecoder
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var myWebView: WebView
    var backPressedTime = 0L
    private lateinit var preferenceHelper: PreferenceHelper
    lateinit var webChromeClient: MyWebChromeClient

    private val TAG: String = MainActivity::class.java.getSimpleName()
    private val MY_PERMISSION_REQUEST_LOCATION = 0
    val MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 123
    private val FCMToken = ""

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferenceHelper = PreferenceHelper(this)

        askNotificationPermission()
        myWebView = findViewById(R.id.webview)
        val cookieManager: CookieManager = CookieManager.getInstance()

// Accepts cookies
        cookieManager.setAcceptCookie(true)

// If you want to accept third party cookies
        cookieManager.setAcceptThirdPartyCookies(myWebView, true)


        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")
        permissionCheck()
        initFirebaseToken()
        //initRetrofit()
    }
    fun getCookie(url: String, key: String): String? {
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie(url)

        cookies?.let {
            val cookieArray = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (cookie in cookieArray) {
                val pair = cookie.split("=".toRegex(), 2).toTypedArray()
                if (pair.size == 2 && pair[0].trim() == key) {
                    return pair[1]
                }
            }
        }
        return null
    }

    fun sendTokenToServer(token: String) {
        val cookie = URLDecoder.decode(getCookie("https://www.tttruck.co.kr","userInfo"), "UTF-8")
        Log.e(TAG,cookie)
        val gson = Gson()
        val user = gson.fromJson(cookie, UserCookie::class.java)
        val client = OkHttpClient()
        val content = "{\"fcmToken\":\""+token+"\"}"
        Log.e(TAG,content)
        val request = Request.Builder()
            .url("https://api.tttruck.co.kr/api/v1/alarm/fcm/register")
            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),content ))
            .addHeader("Authorization","Bearer "+user.accessToken)
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the error
                e.message?.let { Log.e(TAG, it) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    // Handle the error
                    Log.e(TAG,response.toString())
                }
                // Handle the response
                Log.e(TAG,"SUCCEED")
            }
        })
    }

    @SuppressLint("StringFormatInvalid")
    private fun initFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            preferenceHelper.fcmToken = token

            sendTokenToServer(token)
            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Log.d(TAG, "FCM Token = $token")
        })
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.allowFileAccess = true;
        myWebView.webViewClient = MyWebViewClient()
        val prefs: SharedPreferences = getSharedPreferences("cookies", MODE_PRIVATE)
        val cookies: String? = prefs.getString("cookies", null)
        if (cookies != null) {
            CookieManager.getInstance().setCookie("https://www.tttruck.co.kr", cookies)
        }

        webChromeClient = MyWebChromeClient(this) { resultCode, data ->
            var results: Array<Uri>? = null

            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    results = arrayOf()
                } else {
                    val dataString = data.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            (webChromeClient as MyWebChromeClient).mUploadMessage?.onReceiveValue(results)
            (webChromeClient as MyWebChromeClient).mUploadMessage = null
        }
        myWebView.webChromeClient = webChromeClient
        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")
        myWebView.loadUrl("https://www.tttruck.co.kr")
        val edit: SharedPreferences.Editor = prefs.edit()
        edit.putString("cookies", CookieManager.getInstance().getCookie("https://www.tttruck.co.kr"))
        edit.apply()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        webChromeClient.let {
            (it as MyWebChromeClient).onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
            return true
        }
        if (System.currentTimeMillis() - backPressedTime > 3000) {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "앱을 종료하시려면 뒤로가기 버튼을 한번 더 눌러주세요.", LENGTH_SHORT).show()
            return true
        }
        //If it wasn't the Back key or there's no web page history, bubble up to the default
        //system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    private class MyWebViewClient : WebViewClient() {
        private val urlStack = Stack<String>()
        private var currentUrl: String? = null
        private var view = null

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            currentUrl = url?.toString()
            if (urlStack.size >= 3) {
                // Remove the oldest entry from the stack
                urlStack.removeAt(0)
            }
            urlStack.push(currentUrl)
            if (Uri.parse(url).host == "www.tttruck.co.kr") {
                // This is my website, so do not override; let my WebView load the page
                return false
            }
            //Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            if (view != null) {
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    startActivity(view.context, this, null)
                }
                return true
            }
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            currentUrl = url
            super.onPageFinished(view, url)
        }

        fun canGoBackLimited(): Boolean {
            return urlStack.size > 1
        }

        fun goBackLimited() {
            urlStack.pop()
            val url = urlStack.pop()
            currentUrl = url
//            view?.loadUrl(url)
        }
    }
//    private class MyWebViewClient : WebViewClient() {
//
//        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            if (Uri.parse(url).host == "www.tttruck.co.kr") {
//                // This is my website, so do not override; let my WebView load the page
//                return false
//            }
//            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
//                startActivity(this)
//            }
//            return true
//        }
//    }

    class MyWebChromeClient(
        private val activity: Activity,
        private val callback: (resultCode: Int, data: Intent?) -> Unit
    ) : WebChromeClient() {
        var mUploadMessage: ValueCallback<Array<Uri>>? = null

        // For Android >= 5.0
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            mUploadMessage?.onReceiveValue(null)
            mUploadMessage = filePathCallback

            val intent = fileChooserParams.createIntent()
            try {
                activity.startActivityForResult(intent, REQUEST_SELECT_FILE)
            } catch (e: ActivityNotFoundException) {
                mUploadMessage = null
                Toast.makeText(activity, "Cannot open file chooser", Toast.LENGTH_LONG).show()
                return false
            }

            return true
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode != REQUEST_SELECT_FILE || mUploadMessage == null) return
            callback(resultCode, data)
        }

        companion object {
            const val REQUEST_SELECT_FILE = 100
        }

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult): Boolean {
//            if (view != null) {
//                AlertDialog.Builder(view.context)
//                    .setTitle("땡땡트럭")
//                    .setMessage(message)
//                    .setPositiveButton(
//                        android.R.string.ok
//                    ) { dialog, which ->
//                        //TODO("Not yet implemented")
//                    }
//                    .setCancelable(false)
//                    .create()
//                    .show()
//                return true
//            }
            return super.onJsAlert(view, url, message, result);
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback)
            callback.invoke(origin, true, false)
        }
    }

    private fun permissionCheck() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 승낙 상태 일때
            initWebView()
        } else {
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 거절 상태 일때
            //사용자에게 접근권한 설정을 요구하는 다이얼로그를 띄운다.
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_LOCATION
            )
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 승낙 상태 일때
            initWebView()
        } else {
            //Manifest.permission.ACCESS_FINE_LOCATION 접근 거절 상태 일때
            //사용자에게 접근권한 설정을 요구하는 다이얼로그를 띄운다.
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_REQUEST_LOCATION) {
            initWebView()
        }
        if (requestCode == MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            initWebView()
        }
    }
}