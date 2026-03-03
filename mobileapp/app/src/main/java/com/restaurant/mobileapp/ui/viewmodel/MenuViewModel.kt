package com.restaurant.mobileapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurant.mobileapp.data.model.Category
import com.restaurant.mobileapp.data.model.MenuItem
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: RestaurantRepository) : ViewModel() {
    
    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> = _menuItems
    
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    
    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadCategories()
        loadMenuItems()
    }
    
    fun loadMenuItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getMenuItems().fold(
                onSuccess = { items ->
                    _menuItems.value = items
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
            )
        }
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().fold(
                onSuccess = { cats ->
                    _categories.value = cats
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
        }
    }
    
    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        if (category != null) {
            loadMenuItemsByCategory(category.id!!)
        } else {
            loadMenuItems()
        }
    }
    
    private fun loadMenuItemsByCategory(categoryId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getMenuItemsByCategory(categoryId).fold(
                onSuccess = { items ->
                    _menuItems.value = items
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

