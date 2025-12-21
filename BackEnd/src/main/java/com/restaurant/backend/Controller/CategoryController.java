package com.restaurant.backend.Controller;

import com.restaurant.backend.Service.CategoryService;
import com.restaurant.backend.Entity.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category Management", description = "APIs for managing menu categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Category category) {
        Category created = categoryService.create(category);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Category created successfully",
                        "category", created
                )
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        List<Category> categories = categoryService.getAll();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Categories retrieved successfully",
                        "categories", categories
                )
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.update(id, category);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Category updated successfully",
                        "category", updated
                )
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "Category deleted successfully")
        );
    }

    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<?> cleanupDuplicates() {
        try {
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Please run the SQL script: cleanup_duplicate_categories.sql",
                            "action", "Run the SQL script to clean up duplicate categories"
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Error: " + e.getMessage())
            );
        }
    }
}
