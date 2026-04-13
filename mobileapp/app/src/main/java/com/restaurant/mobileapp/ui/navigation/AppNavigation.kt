package com.restaurant.mobileapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.restaurant.mobileapp.data.api.SessionManager
import com.restaurant.mobileapp.data.repository.RestaurantRepository
import com.restaurant.mobileapp.ui.login.LoginScreen
import com.restaurant.mobileapp.ui.admin.*
import com.restaurant.mobileapp.ui.staff.*
import com.restaurant.mobileapp.ui.customer.*
import com.restaurant.mobileapp.ui.profile.*
import com.restaurant.mobileapp.ui.viewmodel.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var userRole by remember { mutableStateOf(SessionManager.getUserRole()) }
    
    val repository = remember { RestaurantRepository() }
    val factory = remember { ViewModelFactory(repository) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    
    // Auth Check: Runs only once when AppNavigation is composed
    LaunchedEffect(Unit) {
        val token = SessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        } else {
            val startDest = when (userRole) {
                "ADMIN" -> Screen.AdminDashboard.route
                "STAFF" -> Screen.StaffDashboard.route
                else -> Screen.CustomerMenu.route
            }
            navController.navigate(startDest) {
                popUpTo(0)
            }
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Login.route) {
                AppBottomNavigation(
                    navController = navController,
                    role = userRole
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        userRole = role
                        val dest = when (role) {
                            "ADMIN" -> Screen.AdminDashboard.route
                            "STAFF" -> Screen.StaffDashboard.route
                            else -> Screen.CustomerMenu.route
                        }
                        navController.navigate(dest) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // ADMIN
            composable(Screen.AdminDashboard.route) {
                val vm: AdminDashboardViewModel = viewModel(factory = factory)
                AdminDashboardScreen(vm)
            }
            composable(Screen.AdminTables.route) {
                val vm: TableManagerViewModel = viewModel(factory = factory)
                AdminTablesScreen(vm)
            }
            composable(Screen.AdminMenu.route) {
                val vm: MenuViewModel = viewModel(factory = factory)
                AdminMenuScreen(vm)
            }
            
            // STAFF
            composable(Screen.StaffDashboard.route) {  
                val vm: TableManagerViewModel = viewModel(factory = factory)
                StaffDashboardScreen(vm)
            }
            composable(Screen.StaffQR.route) { StaffQRScreen() }
            composable(Screen.StaffBooking.route) { 
                val vm: BookingViewModel = viewModel(factory = factory)
                StaffBookingScreen(vm)
            }
            
            // CUSTOMER
            composable(Screen.CustomerMenu.route) {
                val vm: MenuViewModel = viewModel(factory = factory)
                val orderVm: OrderViewModel = viewModel(factory = factory)
                CustomerMenuScreen(vm, orderVm)
            }
            composable(Screen.CustomerOrders.route) {
                val vm: OrderViewModel = viewModel(factory = factory)
                CustomerOrdersScreen(vm)
            }
            
            // COMMON
            composable(Screen.Profile.route) {
                val vm: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = vm,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }
        }
    }
}
