package com.restaurant.mobileapp.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MenuItem(
    val id: Long? = null,
    val name: String,
    val price: BigDecimal? = null,
    val description: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val category: Category? = null,
    @SerializedName("is_available")
    val isAvailable: Boolean = true,
    @SerializedName("preparation_time")
    val preparationTime: Int? = null,
    val calories: Int? = null,
    val allergens: Array<String>? = null
)

data class MenuItemResponse(
    val message: String,
    @SerializedName("menuItems")
    val menuItems: List<MenuItem>
)

