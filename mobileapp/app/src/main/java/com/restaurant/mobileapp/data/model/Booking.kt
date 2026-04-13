package com.restaurant.mobileapp.data.model

import com.google.gson.annotations.SerializedName

data class Booking(
    val id: Long? = null,
    val date: String,
    val time: String,
    val guests: Int,
    val note: String? = null,
    val status: String = "PENDING",
    val customerCode: String? = null,
    val bookingCode: String? = null,
    val customer: User? = null,
    val table: RestaurantTable? = null
)

data class BookingRequest(
    val customerId: Long,
    val date: String,
    val time: String,
    val guests: Int,
    val note: String? = null,
    val tableId: Long? = null
)

data class BookingResponse(
    val message: String,
    val booking: Booking,
    val bookingCode: String? = null
)

data class BookingListResponse(
    val message: String,
    val bookings: List<Booking>
)
