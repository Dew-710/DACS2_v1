package com.restaurant.backend.Repository;

import com.restaurant.backend.Entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    Optional<RestaurantTable> findByQrCode(String qrCode);

    List<RestaurantTable> findByStatus(String status);

    List<RestaurantTable> findByCapacityGreaterThanEqual(int capacity);

    List<RestaurantTable> findByTableType(String tableType);

    boolean existsByQrCode(String qrCode);
}
