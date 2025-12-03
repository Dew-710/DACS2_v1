package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.MenuItem;
import com.restaurant.backend.Service.MenuItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody MenuItem menuItem) {
        MenuItem created = menuItemService.create(menuItem);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Menu item created successfully",
                        "menuItem", created
                )
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        List<MenuItem> menuItems = menuItemService.getAll();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Menu items retrieved successfully",
                        "menuItems", menuItems
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        MenuItem menuItem = menuItemService.findById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Menu item retrieved successfully",
                        "menuItem", menuItem
                )
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MenuItem menuItem) {
        MenuItem updated = menuItemService.update(id, menuItem);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Menu item updated successfully",
                        "menuItem", updated
                )
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        menuItemService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "Menu item deleted successfully")
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategory(@PathVariable Long categoryId) {
        List<MenuItem> menuItems = menuItemService.getByCategory(categoryId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Menu items by category retrieved successfully",
                        "menuItems", menuItems
                )
        );
    }
}
