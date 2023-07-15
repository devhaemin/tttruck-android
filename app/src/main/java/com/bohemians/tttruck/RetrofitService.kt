package com.bohemians.tttruck

import com.bohemians.tttruck.POJO.User
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

//RetorifitService.kt
interface RetrofitService {


    //POST 예제
    @FormUrlEncoded
    @POST("alarm/fcm/register")
    fun registerFCMToken(@Header("Authorization") accessToken: String, @Field("FCMToken") fcmToken: String): Call<User>
}
