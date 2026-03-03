package com.restaurant.mobileapp.data.repository

import com.restaurant.mobileapp.data.api.ApiService
import com.restaurant.mobileapp.data.api.RegisterResponse
import com.restaurant.mobileapp.data.api.RetrofitClient
import com.restaurant.mobileapp.data.api.TokenManager
import com.restaurant.mobileapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RestaurantRepository {
    
    private val apiService: ApiService = RetrofitClient.apiService
    
    // Authentication
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    TokenManager.setToken(loginResponse.token)
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception(response.message() ?: "Login failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(request)

                if (response.isSuccessful) {
                    response.body()?.let {
                        return@withContext Result.success(it)
                    } ?: return@withContext Result.failure(
                        Exception("Empty response body")
                    )
                } else {
                    val errorMessage = response.errorBody()?.string()
                        ?: "Registration failed with code ${response.code()}"

                    return@withContext Result.failure(
                        Exception(errorMessage)
                    )
                }

            } catch (e: Exception) {
                return@withContext Result.failure(
                    Exception("Network error: ${e.localizedMessage}")
                )
            }
        }
    }
    // Menu
    suspend fun getMenuItems(): Result<List<MenuItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMenuItems()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.menuItems)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to load menu"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCategories()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.categories)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to load categories"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getMenuItemsByCategory(categoryId: Long): Result<List<MenuItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMenuItemsByCategory(categoryId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.menuItems)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to load menu items"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Orders
    suspend fun getMyOrders(customerId: Long?): Result<List<Order>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyOrders(customerId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.orders)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to load orders"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getOrCreateActiveOrder(tableId: Long, customerId: Long?): Result<Order> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrCreateActiveOrder(
                    tableId,
                    customerId
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.order)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to create order"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun addItemsToOrder(
        tableId: Long,
        customerId: Long?,
        items: List<OrderItemRequest>
    ): Result<Order> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addItemsToOrder(
                    tableId,
                    customerId,
                    items
                )
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.order)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to add items"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun closeOrder(orderId: Long): Result<Order> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.closeOrder(orderId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.order)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to close order"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun logout() {
        TokenManager.clearToken()
    }
}

