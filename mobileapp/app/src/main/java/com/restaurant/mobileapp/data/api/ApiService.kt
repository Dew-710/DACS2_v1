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
}

data class RegisterResponse(
    val message: String,
    val user: User
)

data class TablesResponse(
    val message: String,
    val tables: List<RestaurantTable>
)

