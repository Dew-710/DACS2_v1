package com.restaurant.mobileapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurant.mobileapp.data.api.RegisterResponse
import com.restaurant.mobileapp.data.model.LoginResponse
import com.restaurant.mobileapp.data.model.RegisterRequest
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: RestaurantRepository) : ViewModel() {
    
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult
    
    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(username, password)
            _loginResult.value = result
            _isLoading.value = false
        }
    }
    
    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(request)
            _registerResult.value = result
            _isLoading.value = false
        }
    }
    
    fun logout() {
        repository.logout()
    }
}

