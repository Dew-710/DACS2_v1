package com.restaurant.mobileapp.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class RestaurantTable(
    val id: Long? = null,
    @SerializedName("table_name")
    val tableName: String,
    val capacity: Int = 0,
    val status: String? = null,
    @SerializedName("qr_code")
    val qrCode: String? = null
)

data class OrderItem(
    val id: Long? = null,
    @SerializedName("menu_item")
    val menuItem: MenuItem,
    val quantity: Int = 1,
    val price: BigDecimal? = null,
    val notes: String? = null,
    val status: String? = "PENDING",
    @SerializedName("round_number")
    val roundNumber: Int? = 1,
    @SerializedName("is_confirmed")
    val isConfirmed: Boolean? = false
)

data class Order(
    val id: Long? = null,
    val customer: User? = null,
    val staff: User? = null,
    val table: RestaurantTable? = null,
    @SerializedName("order_time")
    val orderTime: String? = null,
    val status: String? = null,
    @SerializedName("total_amount")
    val totalAmount: BigDecimal? = null,
    @SerializedName("order_items")
    val orderItems: List<OrderItem>? = emptyList(),
    @SerializedName("payment_status")
    val paymentStatus: String? = null
)

data class OrderResponse(
    val message: String,
    val order: Order
)

data class OrdersResponse(
    val message: String,
    val orders: List<Order>
)

data class AddItemsRequest(
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("menu_item")
    val menuItem: MenuItem,
    val quantity: Int = 1,
    val notes: String? = null
)

