package com.bohemians.tttruck

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseService:FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
    }
}