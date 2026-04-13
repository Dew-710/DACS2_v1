package com.restaurant.mobileapp.ui.staff

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.restaurant.mobileapp.data.model.RestaurantTable
import com.restaurant.mobileapp.ui.theme.PrimaryColor
import com.restaurant.mobileapp.ui.viewmodel.BookingViewModel
import com.restaurant.mobileapp.ui.viewmodel.TableManagerViewModel

@Composable
fun StaffDashboardScreen(tableViewModel: TableManagerViewModel) {
    val tables by tableViewModel.tables.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        tableViewModel.loadAllTables()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Staff Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
fun TableCard(table: RestaurantTable, onCheckout: () -> Unit) {
    val isOccupied = table.status == "OCCUPIED"
    val cardColor = if (isOccupied) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(table.tableName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(table.status ?: "UNKNOWN", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            if (isOccupied) {
                Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
                    Text("Check-out")
                }
            }
        }
    }
}

@Composable
fun StaffQRScreen() {
    var qrCode by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gửi QR ESP32", style = MaterialTheme.typography.headlineMedium, color = PrimaryColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = qrCode,
            onValueChange = { qrCode = it },
            label = { Text("Quét mã QR bàn") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* Mokup sending to ESP32 */ },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Gửi Lệnh Tới ESP32")
        }
    }
}

@Composable
fun StaffBookingScreen(bookingViewModel: BookingViewModel) {
    // Note: Temporary logic. In a real app we would call adminService.getPendingReservations()
    // For now we just display list if available or placeholder
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lịch Đặt Bàn", style = MaterialTheme.typography.headlineMedium, color = PrimaryColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Đang tải dữ liệu...", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
