package com.bohemians.tttruck

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.bohemians.tttruck.POJO.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WebAppInterface(private val mContext: Context) {

    private lateinit var preferenceHelper: PreferenceHelper
    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }
    @JavascriptInterface
    fun onLoginEvent(accessToken: String) {

        // Handle the login event here
        initRetrofit(accessToken)
    }
    private fun initRetrofit(accessToken: String) {
        val retrofit = Retrofit.Builder().baseUrl("https://api.tttruck.co.kr/api/v1/")
            .addConverterFactory(GsonConverterFactory.create()).build();
        val service = retrofit.create(RetrofitService::class.java);
        preferenceHelper = PreferenceHelper(mContext)

        // Get a value from SharedPreferences
        val fcmToken = preferenceHelper.fcmToken
        if (fcmToken == null) {
            return
        }
        service.registerFCMToken("Bearer " + accessToken, fcmToken).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.isSuccessful){
                    // 정상적으로 통신이 성고된 경우
                    var result: User? = response.body()
                    Log.d("YMC", "onResponse 성공: " + result?.toString());
                }else{
                    // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                    Log.d("YMC", "onResponse 실패")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // 통신 실패 (인터넷 끊킴, 예외 발생 등 시스템적인 이유)
                Log.d("YMC", "onFailure 에러: " + t.message.toString());
            }
        })
    }
}