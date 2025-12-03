package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.MenuItem;
import com.restaurant.backend.Repository.MenuItemRepository;
import com.restaurant.backend.Service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;


    @Override
    public List<MenuItem> findAll() {
        return List.of();
    }

    @Override
    public MenuItem findById(Long id) {
        return null;
    }

    @Override
    public MenuItem create(MenuItem item) {
        return null;
    }

    @Override
    public MenuItem update(Long id, MenuItem item) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public MenuItem getMenuItemById(long id) {
        return null;
    }

    @Override
    public MenuItem getMenuItemByCategoryId(long categoryId) {
        return null;
    }

    @Override
    public MenuItem getMenuItemByStatus(String status) {
        return null;
    }

    @Override
    public List<MenuItem> getAll() {
        return List.of();
    }

    @Override
    public List<MenuItem> getByCategory(Long categoryId) {
        return List.of();
    }
}
