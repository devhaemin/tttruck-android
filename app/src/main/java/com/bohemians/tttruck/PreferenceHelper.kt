package com.bohemians.tttruck

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    var fcmToken: String?
        get() = sharedPreferences.getString(FCM_TOKEN_KEY, null)
        set(value) = sharedPreferences.edit().putString(FCM_TOKEN_KEY, value).apply()

    companion object {
        private const val PREF_FILE_NAME = "tttruck_app_preferences"
        private const val FCM_TOKEN_KEY = "fcmToken"
    }
}
