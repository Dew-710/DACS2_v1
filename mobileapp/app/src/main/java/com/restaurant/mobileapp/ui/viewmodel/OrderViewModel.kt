package com.restaurant.mobileapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurant.mobileapp.data.model.Order
import com.restaurant.mobileapp.data.model.OrderItemRequest
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class OrderViewModel(private val repository: RestaurantRepository) : ViewModel() {
    
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders
    
    private val _currentOrder = MutableLiveData<Order?>()
    val currentOrder: LiveData<Order?> = _currentOrder
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadMyOrders(customerId: Long?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getMyOrders(customerId).fold(
                onSuccess = { orderList ->
                    _orders.value = orderList
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun getOrCreateActiveOrder(tableId: Long, customerId: Long?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getOrCreateActiveOrder(tableId, customerId).fold(
                onSuccess = { order ->
                    _currentOrder.value = order
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun addItemsToOrder(tableId: Long, customerId: Long?, items: List<OrderItemRequest>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.addItemsToOrder(tableId, customerId, items).fold(
                onSuccess = { order ->
                    _currentOrder.value = order
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun closeOrder(orderId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.closeOrder(orderId).fold(
                onSuccess = { order ->
                    _currentOrder.value = order
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }
}

