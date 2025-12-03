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
        return List.of();
    }

    @Override
    public Category findById(Long id) {
        return null;
    }

    @Override
    public Category create(Category category) {
        return null;
    }

    @Override
    public Category update(Long id, Category category) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Category GetCategoryById(long id) {
        return null;
    }

    @Override
    public List<Category> getAll() {
        return List.of();
    }
}
