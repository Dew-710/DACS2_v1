package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.Category;
import com.restaurant.backend.Repository.CategoryRepository;
import com.restaurant.backend.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, Category category) {
        Category existing = findById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setImageUrl(category.getImageUrl());
        existing.setDisplayOrder(category.getDisplayOrder());
        existing.setIsActive(category.getIsActive());
        return categoryRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Category GetCategoryById(long id) {
        return findById(id);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }
}
