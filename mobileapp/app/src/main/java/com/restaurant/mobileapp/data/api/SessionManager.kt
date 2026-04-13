package com.restaurant.mobileapp.data.api

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private var prefs: SharedPreferences? = null
    private var token: String? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("restaurant_prefs", Context.MODE_PRIVATE)
        token = prefs?.getString("auth_token", null)
    }

    fun saveAuthToken(newToken: String?) {
        if (newToken == null) {
            token = null
            prefs?.edit()?.remove("auth_token")?.apply()
        } else {
            token = "Bearer $newToken"
            prefs?.edit()?.putString("auth_token", token)?.apply()
        }
    }

    fun getAuthToken(): String? {
        return if (token != null && token != "Bearer null") token else null
    }

    fun saveUser(id: Long, role: String, username: String) {
        prefs?.edit()?.apply {
            putLong("user_id", id)
            putString("user_role", role)
            putString("username", username)
            apply()
        }
    }

    fun getUserId(): Long {
        return prefs?.getLong("user_id", -1L) ?: -1L
    }

    fun getUserRole(): String {
        return prefs?.getString("user_role", "CUSTOMER") ?: "CUSTOMER"
    }

    fun getUsername(): String {
        return prefs?.getString("username", "") ?: ""
    }

    fun clearSession() {
        token = null
        prefs?.edit()?.clear()?.apply()
    }
}
