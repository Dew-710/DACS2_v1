package com.restaurant.mobileapp

import android.app.Application
import com.restaurant.mobileapp.data.api.SessionManager

class RestaurantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
