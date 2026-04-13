package com.restaurant.mobileapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurant.mobileapp.data.model.User
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private val _userProfile = MutableLiveData<User>()
    val userProfile: LiveData<User> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getUserProfile(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getUserProfile(userId)
            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load profile"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
    }
}
