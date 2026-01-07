package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Repository.RestaurantTableRepository;
import com.restaurant.backend.Service.OrderService;
import com.restaurant.backend.Service.QRCodeService;
import com.restaurant.backend.Service.RestaurantTableService;
import com.restaurant.backend.websocket.IoTWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TableServiceImpl implements RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final QRCodeService qrCodeService;
    private final IoTWebSocketHandler webSocketHandler;
    private final OrderService orderService;

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findAll() {
        return tableRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTable findById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + id));
    }

    @Override
    public RestaurantTable create(RestaurantTable table) {
        // Generate QR code if not provided
        if (table.getQrCode() == null || table.getQrCode().isEmpty()) {
            table.setQrCode(generateUniqueQrCode());
        }

        // Set default status
        if (table.getStatus() == null) {
            table.setStatus("AVAILABLE");
        }

        table.setLastUpdated(LocalDateTime.now());

        return tableRepository.save(table);
    }

    @Override
    public RestaurantTable update(Long id, RestaurantTable table) {
        RestaurantTable existingTable = findById(id);
        existingTable.setTableName(table.getTableName());
        existingTable.setCapacity(table.getCapacity());
        existingTable.setStatus(table.getStatus());
        existingTable.setTableType(table.getTableType());
        existingTable.setLocation(table.getLocation());
        existingTable.setLastUpdated(LocalDateTime.now());

        return tableRepository.save(existingTable);
    }

    @Override
    public void delete(Long id) {
        tableRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTable GetTablebyId(long id) {
        return findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> getAll() {
        return tableRepository.findAll();
    }

    @Override
    public RestaurantTable updateStatus(Long id, String status) {
        RestaurantTable table = findById(id);
        String oldStatus = table.getStatus();

        table.setStatus(status);
        table.setLastUpdated(LocalDateTime.now());
        RestaurantTable updatedTable = tableRepository.save(table);

        // Auto-send QR code when table becomes OCCUPIED
        if ("OCCUPIED".equals(status) && !status.equals(oldStatus) && table.getQrCode() != null) {
            try {
                System.out.println("🎯 Auto-sending QR code for table " + table.getTableName() + " (ID: " + id + ")");

                // Generate frontend URL
                String frontendUrl = System.getenv("FRONTEND_URL");
                if (frontendUrl == null || frontendUrl.isEmpty()) {
                    frontendUrl = "http://localhost:3000";
                }
                String qrUrl = frontendUrl + "/menu/" + table.getQrCode();

                // Generate QR code image
                byte[] qrImageBytes = qrCodeService.generateQRCodeImageBytes(qrUrl, 128, 128);

                // Send to ESP32 with table ID
                webSocketHandler.broadcastImageBytes(qrImageBytes, id);

                System.out.println("✅ Auto-sent QR code for occupied table " + table.getTableName());
            } catch (Exception e) {
                System.err.println("❌ Failed to auto-send QR code for table " + table.getTableName() + ": " + e.getMessage());
            }
        }

        return updatedTable;
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTable findByQrCode(String qrCode) {
        return tableRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Table not found with QR code: " + qrCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findByStatus(String status) {
        return tableRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findByCapacity(int minCapacity) {
        return tableRepository.findByCapacityGreaterThanEqual(minCapacity);
    }

    @Override
    public RestaurantTable generateQrCode(Long tableId) {
        RestaurantTable table = findById(tableId);
        String qrCode = generateUniqueQrCode();
        table.setQrCode(qrCode);
        table.setLastUpdated(LocalDateTime.now());
        return tableRepository.save(table);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> getAvailableTables() {
        return tableRepository.findByStatus("AVAILABLE");
    }

    @Override
    public RestaurantTable checkInTable(String qrCode, Long customerId) {
        RestaurantTable table = findByQrCode(qrCode);

        // Check if table is available
        if (!"AVAILABLE".equals(table.getStatus()) && !"RESERVED".equals(table.getStatus())) {
            throw new RuntimeException("Table is not available for check-in");
        }

        table.setStatus("OCCUPIED");
        table.setLastUpdated(LocalDateTime.now());

        return tableRepository.save(table);
    }

    @Override
    public RestaurantTable checkOutTable(Long tableId) {
        RestaurantTable table = findById(tableId);
        
        // IMPORTANT: Close all active orders on this table before changing status
        // BUT: Skip empty orders (orders without items) - they shouldn't be closed
        List<Order> activeOrders = orderService.getActiveOrdersByTable(tableId);
        for (Order order : activeOrders) {
            try {
                // Skip orders without items (empty orders)
                if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                    log.info("⏭️ Skipping empty order #{} (no items)", order.getId());
                    continue;
                }
                
                log.info("🔒 Closing order #{} for table checkout ({} items)", order.getId(), order.getOrderItems().size());
                orderService.closeOrder(order.getId());
                log.info("✅ Order #{} closed successfully", order.getId());
            } catch (Exception e) {
                log.error("❌ Failed to close order #{}: {}", order.getId(), e.getMessage(), e);
                // Continue with other orders even if one fails
            }
        }
        
        table.setStatus("CLEANING");
        table.setLastUpdated(LocalDateTime.now());

        // After cleaning, table becomes vacant again
        // This could be handled by a scheduled task or manual staff action
        return tableRepository.save(table);
    }

    /**
     * Generate unique QR code for table
     */
    private String generateUniqueQrCode() {
        String qrCode;
        do {
            qrCode = "TABLE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (tableRepository.existsByQrCode(qrCode));

        return qrCode;
    }
}
