package com.restaurant.backend.Service;

import com.restaurant.backend.Entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();

    Category findById(Long id);

    Category create(Category category);

    Category update(Long id, Category category);

    void delete(Long id);

    Category GetCategoryById(long id);


    List<Category> getAll();
}
