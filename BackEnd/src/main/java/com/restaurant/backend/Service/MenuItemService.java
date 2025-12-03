package com.restaurant.backend.Service;

import com.restaurant.backend.Entity.MenuItem;

import java.util.List;

public interface MenuItemService {
    List<MenuItem> findAll();

    MenuItem findById(Long id);

    MenuItem create(MenuItem item);

    MenuItem update(Long id, MenuItem item);

    void delete(Long id);

    MenuItem getMenuItemById(long id);
    MenuItem getMenuItemByCategoryId(long categoryId);
    MenuItem getMenuItemByStatus(String status);

    List<MenuItem> getAll();

    List<MenuItem> getByCategory(Long categoryId);
}
