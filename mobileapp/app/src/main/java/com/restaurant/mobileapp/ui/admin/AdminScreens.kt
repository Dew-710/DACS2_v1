package com.restaurant.mobileapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.restaurant.mobileapp.data.model.MenuItem
import com.restaurant.mobileapp.data.model.RestaurantTable
import com.restaurant.mobileapp.ui.theme.PrimaryColor
import com.restaurant.mobileapp.ui.viewmodel.AdminDashboardViewModel
import com.restaurant.mobileapp.ui.viewmodel.MenuViewModel
import com.restaurant.mobileapp.ui.viewmodel.TableManagerViewModel
import com.restaurant.mobileapp.ui.customer.MenuItemCard
import com.restaurant.mobileapp.ui.staff.TableCard

@Composable
fun AdminDashboardScreen(dashboardViewModel: AdminDashboardViewModel) {
    val summary by dashboardViewModel.summary.observeAsState()
    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Thống Kê", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Spacer(modifier = Modifier.height(24.dp))
            summary?.let {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tổng Doanh Thu", color = Color.Gray)
                        Text("${it.totalRevenue} VNĐ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Đơn Hàng", color = Color.Gray)
                            Text("${it.totalOrders}", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Khách Hàng", color = Color.Gray)
                            Text("${it.totalCustomers}", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Lịch đặt chờ duyệt", color = Color.Gray)
                        Text("${it.pendingReservations}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                    }
                }
            } ?: run {
                Text("Không có dữ liệu thống kê")
            }
    }
}

@Composable
fun AdminTablesScreen(tableViewModel: TableManagerViewModel) {
    val tables by tableViewModel.tables.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        tableViewModel.loadAllTables()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Quản lý Bàn", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(tables.size) { index ->
                val table = tables[index]
                TableCard(table = table, onCheckout = { table.id?.let { tableViewModel.checkOut(it) } })
            }
        }
    }
}

@Composable
fun AdminMenuScreen(menuViewModel: MenuViewModel) {
    val menuItems by menuViewModel.menuItems.observeAsState(emptyList())
    val isLoading by menuViewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        menuViewModel.loadMenuItems()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Quản lý Thực Đơn", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(menuItems.size) { index ->
                    val item = menuItems[index]
                    MenuItemCard(item)
                }
            }
        }
    }
}
