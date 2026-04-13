package com.restaurant.mobileapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurant.mobileapp.data.model.RestaurantTable
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class TableManagerViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private val _tables = MutableLiveData<List<RestaurantTable>>()
    val tables: LiveData<List<RestaurantTable>> = _tables

    private val _actionResult = MutableLiveData<Result<RestaurantTable>>()
    val actionResult: LiveData<Result<RestaurantTable>> = _actionResult

    fun loadAllTables() {
        viewModelScope.launch {
            val result = repository.getAllTables()
            if (result.isSuccess) {
                _tables.postValue(result.getOrDefault(emptyList()))
            }
        }
    }

    fun checkIn(qrCode: String) {
        viewModelScope.launch {
            val result = repository.checkInTable(qrCode)
            _actionResult.postValue(result)
            loadAllTables() // Refresh
        }
    }

    fun checkOut(tableId: Long) {
        viewModelScope.launch {
            val result = repository.checkOutTable(tableId)
            _actionResult.postValue(result)
            loadAllTables() // Refresh
        }
    }
}
