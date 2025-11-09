package com.platemate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.platemate.enums.MealType;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.MenuItem;
import com.platemate.repository.CategoryRepository;
import com.platemate.repository.MenuItemRepository;

@Service
public class MenuItemService {
    
    @Autowired
    MenuItemRepository menuItemRepository;

    @Autowired
    CategoryRepository categoryRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public MenuItem updateMenuItem(Long id, MenuItem updatedItem) {
        return menuItemRepository.findById(id).map(item -> {
            item.setCategory(updatedItem.getCategory());
            item.setDescription(updatedItem.getDescription());
            item.setItemName(updatedItem.getItemName());
            item.setIngredients(updatedItem.getIngredients());
            item.setMealType(updatedItem.getMealType());
            item.setPrice(updatedItem.getPrice());
            return menuItemRepository.save(item);
        }).orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + id));
    }

    // Customer-facing methods with pagination
    public Page<MenuItem> getAvailableMenuItems(Pageable pageable) {
        return menuItemRepository.findAvailableMenuItems(pageable);
    }

    public Page<MenuItem> getAvailableMenuItemsByProvider(Long providerId, Pageable pageable) {
        return menuItemRepository.findAvailableMenuItemsByProvider(providerId, pageable);
    }

    public Page<MenuItem> getAvailableMenuItemsByCategory(Long categoryId, Pageable pageable) {
        return menuItemRepository.findAvailableMenuItemsByCategory(categoryId, pageable);
    }

    public Page<MenuItem> getAvailableMenuItemsByMealType(MealType mealType, Pageable pageable) {
        return menuItemRepository.findAvailableMenuItemsByMealType(mealType, pageable);
    }

    public Page<MenuItem> searchAvailableMenuItems(String searchTerm, Pageable pageable) {
        return menuItemRepository.searchAvailableMenuItems(searchTerm, pageable);
    }

    public Page<MenuItem> getAvailableMenuItemsByProviderAndCategory(Long providerId, Long categoryId, Pageable pageable) {
        return menuItemRepository.findAvailableMenuItemsByProviderAndCategory(providerId, categoryId, pageable);
    }

    public MenuItem getAvailableMenuItemById(Long itemId) {
        return menuItemRepository.findAvailableMenuItemById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found or not available"));
    }
}
