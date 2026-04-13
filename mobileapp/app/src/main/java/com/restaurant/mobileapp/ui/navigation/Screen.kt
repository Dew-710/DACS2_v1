package com.restaurant.mobileapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Login : Screen("login", "Login", null)
    
    object AdminDashboard : Screen("admin_dashboard", "Thống kê", Icons.Default.Home)
    object AdminTables : Screen("admin_tables", "Quản lý Bàn", Icons.Default.List)
    object AdminMenu : Screen("admin_menu", "Quản lý Món", Icons.Default.Menu)
    
    object StaffDashboard : Screen("staff_dashboard", "Trang chủ", Icons.Default.Home)
    object StaffQR : Screen("staff_qr", "QR ESP32", Icons.Default.Settings)
    object StaffBooking : Screen("staff_booking", "Lịch Đặt", Icons.Default.DateRange)
    
    object CustomerMenu : Screen("customer_menu", "Thực đơn", Icons.Default.Menu)
    object CustomerOrders : Screen("customer_orders", "Lịch sử", Icons.Default.ShoppingCart)
    object CustomerBooking : Screen("customer_booking", "Đặt bàn", Icons.Default.DateRange)
    
    object Profile : Screen("profile", "Cá nhân", Icons.Default.Person)
}

@Composable
fun AppBottomNavigation(navController: NavController, role: String) {
    val items = when (role) {
        "ADMIN" -> listOf(Screen.AdminDashboard, Screen.AdminTables, Screen.AdminMenu, Screen.Profile)
        "STAFF" -> listOf(Screen.StaffDashboard, Screen.StaffQR, Screen.StaffBooking, Screen.Profile)
        else -> listOf(Screen.CustomerMenu, Screen.CustomerBooking, Screen.CustomerOrders, Screen.Profile)
    }

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { if (screen.icon != null) Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
