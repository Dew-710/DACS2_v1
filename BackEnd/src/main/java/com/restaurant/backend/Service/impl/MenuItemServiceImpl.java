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
        return menuItemRepository.findAll();
    }

    @Override
    public MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
    }

    @Override
    public MenuItem create(MenuItem item) {
        return menuItemRepository.save(item);
    }

    @Override
    public MenuItem update(Long id, MenuItem item) {
        MenuItem existing = findById(id);
        existing.setName(item.getName());
        existing.setDescription(item.getDescription());
        existing.setPrice(item.getPrice());
        existing.setImageUrl(item.getImageUrl());
        existing.setCategory(item.getCategory());
        existing.setIsAvailable(item.getIsAvailable());
        existing.setPreparationTime(item.getPreparationTime());
        existing.setCalories(item.getCalories());
        existing.setAllergens(item.getAllergens());
        return menuItemRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        menuItemRepository.deleteById(id);
    }

    @Override
    public MenuItem getMenuItemById(long id) {
        return findById(id);
    }

    @Override
    public MenuItem getMenuItemByCategoryId(long categoryId) {
        return null; // Not implemented in interface
    }

    @Override
    public MenuItem getMenuItemByStatus(String status) {
        return null; // Not implemented in interface
    }

    @Override
    public List<MenuItem> getAll() {
        return menuItemRepository.findAll();
    }

    @Override
    public List<MenuItem> getByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }
}
