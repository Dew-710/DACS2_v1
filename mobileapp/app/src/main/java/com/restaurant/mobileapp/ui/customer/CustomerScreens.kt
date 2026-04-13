package com.restaurant.mobileapp.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.restaurant.mobileapp.data.model.BookingRequest
import com.restaurant.mobileapp.data.model.Category
import com.restaurant.mobileapp.data.model.MenuItem
import com.restaurant.mobileapp.data.api.SessionManager
import com.restaurant.mobileapp.ui.theme.*
import com.restaurant.mobileapp.ui.viewmodel.BookingViewModel
import com.restaurant.mobileapp.ui.viewmodel.MenuViewModel
import com.restaurant.mobileapp.ui.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun CustomerMenuScreen(
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel
) {
    val categories by menuViewModel.categories.observeAsState(emptyList())
    val menuItems by menuViewModel.menuItems.observeAsState(emptyList())
    val selectedCategory by menuViewModel.selectedCategory.observeAsState()
    val isLoading by menuViewModel.isLoading.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Thực đơn", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { menuViewModel.selectCategory(null) },
                    label = { Text("Tất cả") }
                )
            }
            items(categories.size) { index ->
                val category = categories[index]
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { menuViewModel.selectCategory(category) },
                    label = { Text(category.name) }
                )
            }
        }

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
                    MenuItemCard(menuItems[index])
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            val imageUrl = if (!item.imageUrl.isNullOrEmpty()) {
                if (item.imageUrl.startsWith("http")) item.imageUrl else "http://10.0.2.2:8080/api/images/${item.imageUrl}"
            } else null
            
            AsyncImage(
                model = imageUrl ?: "https://via.placeholder.com/150",
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1)
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            Text(formatter.format(item.price), color = PrimaryColor)
        }
    }
}

@Composable
fun CustomerOrdersScreen(orderViewModel: OrderViewModel) {
    val orders by orderViewModel.orders.observeAsState(emptyList())
    val isLoading by orderViewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        val currentUserId = SessionManager.getUserId()
        orderViewModel.loadMyOrders(currentUserId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Lịch sử Đơn Hàng", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có đơn hàng nào", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders.size) { index ->
                    val order = orders[index]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Đơn hàng #${order.id}", fontWeight = FontWeight.Bold)
                            Text("Ngày: ${order.orderTime ?: ""}")
                            Text("Tổng tiền: ${order.totalAmount ?: "0"}", color = PrimaryColor)
                            Text("Trạng thái: ${order.status ?: "Không rõ"}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerBookingScreen(bookingViewModel: BookingViewModel) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("2") }
    var note by remember { mutableStateOf("") }

    val isLoading by bookingViewModel.isLoading.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đặt Bàn", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Ngày (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Giờ (HH:MM)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = guests,
            onValueChange = { guests = it },
            label = { Text("Số người") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Ghi chú") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val userId = SessionManager.getUserId() ?: return@Button
                val request = BookingRequest(
                    customerId = userId,
                    date = date,
                    time = time,
                    guests = guests.toIntOrNull() ?: 2,
                    note = note
                )
                bookingViewModel.createBooking(request)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading && date.isNotEmpty() && time.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Xác nhận Đặt Bàn")
            }
        }
    }
}
