package com.restaurant.mobileapp.data.api

import com.restaurant.mobileapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @GET("users/profile/{id}")
    suspend fun getUserProfile(@Path("id") id: Long): Response<UserResponse>

    @PUT("users/profile/{id}")
    suspend fun updateProfile(@Path("id") id: Long, @Body request: UpdateProfileRequest): Response<UserResponse>

    @POST("users/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<SimpleResponse>
    
    // Menu Items
    @GET("menu-items/list")
    suspend fun getMenuItems(): Response<MenuItemResponse>
    
    @GET("menu-items/category/{categoryId}")
    suspend fun getMenuItemsByCategory(@Path("categoryId") categoryId: Long): Response<MenuItemResponse>
    
    // Categories
    @GET("categories/list")
    suspend fun getCategories(): Response<CategoryResponse>
    
    // Tables
    @GET("tables/list")
    suspend fun getTables(): Response<TablesResponse>
    
    @GET("tables/available")
    suspend fun getAvailableTables(): Response<TablesResponse>
    
    // Orders
    @GET("orders/list")
    suspend fun getOrders(): Response<OrdersResponse>
    
    @GET("orders/my-orders")
    suspend fun getMyOrders(
        @Query("customerId") customerId: Long?
    ): Response<OrdersResponse>
    
    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") orderId: Long
    ): Response<OrderResponse>
    
    @POST("orders/table/{tableId}/get-or-create")
    suspend fun getOrCreateActiveOrder(
        @Path("tableId") tableId: Long,
        @Query("customerId") customerId: Long?
    ): Response<OrderResponse>
    
    @POST("orders/table/{tableId}/add-items")
    suspend fun addItemsToOrder(
        @Path("tableId") tableId: Long,
        @Query("customerId") customerId: Long?,
        @Body items: List<OrderItemRequest>
    ): Response<OrderResponse>
    
    @PUT("orders/{orderId}/close")
    suspend fun closeOrder(
        @Path("orderId") orderId: Long
    ): Response<OrderResponse>
    
    // Bookings
    @POST("bookings/create")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>
    
    @GET("bookings/my-bookings")
    suspend fun getMyBookings(@Query("customerId") customerId: Long): Response<BookingListResponse>
    
    @GET("bookings/availability")
    suspend fun checkAvailability(
        @Query("date") date: String,
        @Query("time") time: String,
        @Query("guests") guests: Int
    ): Response<AvailableTablesResponse>
    
    // Admin & Staff
    @GET("admin/dashboard/summary")
    suspend fun getDashboardSummary(): Response<DashboardSummaryResponse>
    
    @GET("tables/all")
    suspend fun getAllTables(): Response<TablesResponse>

    @PUT("tables/{id}/status-update/{status}")
    suspend fun updateTableStatus(
        @Path("id") tableId: Long,
        @Path("status") status: String
    ): Response<TableResponse>

    @POST("tables/checkin/{qrCode}")
    suspend fun checkInTable(@Path("qrCode") qrCode: String): Response<TableResponse>

    @POST("tables/{id}/checkout")
    suspend fun checkOutTable(@Path("id") tableId: Long): Response<TableResponse>
}

data class SimpleResponse(val message: String)

data class UserResponse(val message: String, val user: User)

data class UpdateProfileRequest(
    val fullName: String?,
    val phone: String?,
    val email: String?
)

data class ForgotPasswordRequest(val email: String)

data class TableResponse(val message: String, val table: RestaurantTable)

data class AvailableTablesResponse(
    val message: String,
    val availableTables: List<RestaurantTable>
)

data class DashboardSummaryResponse(
    val totalRevenue: Double,
    val totalOrders: Int,
    val totalCustomers: Int,
    val pendingReservations: Int
)

data class RegisterResponse(
    val message: String,
    val user: User
)

data class TablesResponse(
    val message: String,
    val tables: List<RestaurantTable>
)

