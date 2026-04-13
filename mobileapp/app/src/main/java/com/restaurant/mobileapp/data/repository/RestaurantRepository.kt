package com.restaurant.mobileapp.data.repository

import com.restaurant.mobileapp.data.api.*
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
                    SessionManager.saveAuthToken(loginResponse.token)
                    loginResponse.user.id?.let {
                        SessionManager.saveUser(it, loginResponse.user.role, loginResponse.user.username)
                    }
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
        SessionManager.clearSession()
    }

    // Bookings
    suspend fun createBooking(request: BookingRequest): Result<Booking> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createBooking(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.booking)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to book table"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMyBookings(customerId: Long): Result<List<Booking>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyBookings(customerId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.bookings)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to load bookings"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // User Profile
    suspend fun getUserProfile(userId: Long): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.user)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to get profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Tables & Admin
    suspend fun getDashboardSummary(): Result<DashboardSummaryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDashboardSummary()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to load dashboard"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getAllTables(): Result<List<RestaurantTable>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllTables()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.tables)
                } else {
                    Result.failure(Exception(response.message() ?: "Failed to load tables"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun checkInTable(qrCode: String): Result<RestaurantTable> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkInTable(qrCode)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.table)
                } else {
                    Result.failure(Exception(response.message() ?: "Check-in failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun checkOutTable(tableId: Long): Result<RestaurantTable> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkOutTable(tableId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.table)
                } else {
                    Result.failure(Exception(response.message() ?: "Check-out failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
