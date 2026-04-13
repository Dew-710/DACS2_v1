package com.restaurant.mobileapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurant.mobileapp.data.api.DashboardSummaryResponse
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class AdminDashboardViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private val _summary = MutableLiveData<DashboardSummaryResponse>()
    val summary: LiveData<DashboardSummaryResponse> = _summary

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadDashboard() {
        viewModelScope.launch {
            val result = repository.getDashboardSummary()
            if (result.isSuccess) {
                _summary.postValue(result.getOrNull())
            } else {
                _error.postValue(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
