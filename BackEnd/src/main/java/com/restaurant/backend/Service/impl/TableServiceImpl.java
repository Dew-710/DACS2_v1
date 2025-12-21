package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Repository.RestaurantTableRepository;
import com.restaurant.backend.Service.RestaurantTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TableServiceImpl implements RestaurantTableService {

    private final RestaurantTableRepository tableRepository;

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
        table.setStatus(status);
        table.setLastUpdated(LocalDateTime.now());
        return tableRepository.save(table);
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
