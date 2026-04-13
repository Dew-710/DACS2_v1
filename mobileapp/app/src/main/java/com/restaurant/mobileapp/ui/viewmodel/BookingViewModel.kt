package com.restaurant.mobileapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurant.mobileapp.data.model.Booking
import com.restaurant.mobileapp.data.model.BookingRequest
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class BookingViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private val _bookings = MutableLiveData<List<Booking>>()
    val bookings: LiveData<List<Booking>> = _bookings

    private val _bookingResult = MutableLiveData<Result<Booking>>()
    val bookingResult: LiveData<Result<Booking>> = _bookingResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadMyBookings(customerId: Long) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getMyBookings(customerId)
            if (result.isSuccess) {
                _bookings.postValue(result.getOrDefault(emptyList()))
            }
            _isLoading.postValue(false)
        }
    }

    fun createBooking(request: BookingRequest) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.createBooking(request)
            _bookingResult.postValue(result)
            _isLoading.postValue(false)
        }
    }
}
