package com.restaurant.mobileapp.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("display_order")
    val displayOrder: Int = 0,
    @SerializedName("is_active")
    val isActive: Boolean = true
)

data class CategoryResponse(
    val message: String,
    val categories: List<Category>
)

