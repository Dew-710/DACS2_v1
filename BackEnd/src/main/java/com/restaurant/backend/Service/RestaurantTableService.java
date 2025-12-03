package com.restaurant.backend.Service;

import com.restaurant.backend.Entity.RestaurantTable;

import java.util.List;

public interface RestaurantTableService {
    List<RestaurantTable> findAll();

    RestaurantTable findById(Long id);

    RestaurantTable create(RestaurantTable table);

    RestaurantTable update(Long id, RestaurantTable table);

    void delete(Long id);

    RestaurantTable GetTablebyId(long id);

    List<RestaurantTable> getAll();

    RestaurantTable updateStatus(Long id, String status);

    // New methods for improved system
    RestaurantTable findByQrCode(String qrCode);

    List<RestaurantTable> findByStatus(String status);

    List<RestaurantTable> findByCapacity(int minCapacity);

    RestaurantTable generateQrCode(Long tableId);

    List<RestaurantTable> getAvailableTables();

    RestaurantTable checkInTable(String qrCode, Long customerId);

    RestaurantTable checkOutTable(Long tableId);
}
