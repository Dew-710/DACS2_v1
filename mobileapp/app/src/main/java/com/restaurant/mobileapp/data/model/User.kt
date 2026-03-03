package com.restaurant.mobileapp.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long? = null,
    val username: String,
    val password: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val role: String = "CUSTOMER",
    val status: String = "ACTIVE"
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("full_name")
    val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val role: String = "CUSTOMER"
)

data class LoginResponse(
    val message: String,
    val user: User,
    val token: String
)

