package com.platemate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platemate.enums.ImageType;
import com.platemate.enums.MealType;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.MenuItem;
import com.platemate.repository.CategoryRepository;
import com.platemate.repository.ImageRepository;
import com.platemate.repository.MenuItemRepository;

@Service
public class MenuItemService {
    
    @Autowired
    MenuItemRepository menuItemRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ImageRepository imageRepository;

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = menuItemRepository.findAll();
        items.forEach(this::loadExtras);
        return items;
    }

    public MenuItem updateMenuItem(Long id, MenuItem updatedItem) {
        return menuItemRepository.findById(id).map(item -> {
            item.setCategory(updatedItem.getCategory());
            item.setDescription(updatedItem.getDescription());
            item.setItemName(updatedItem.getItemName());
            item.setIngredients(updatedItem.getIngredients());
            item.setMealType(updatedItem.getMealType());
            item.setPrice(updatedItem.getPrice());
            MenuItem saved = menuItemRepository.save(item);
            loadExtras(saved);
            return saved;
        }).orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + id));
    }

    // Customer-facing methods with pagination
    public Page<MenuItem> getAvailableMenuItems(Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findAvailableMenuItems(pageable);
        page.getContent().forEach(this::loadExtras);
        return page;
    }

    public Page<MenuItem> getAvailableMenuItemsByProvider(Long providerId, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findAvailableMenuItemsByProvider(providerId, pageable);
        page.getContent().forEach(this::loadExtras);
        return page;
    }

    public Page<MenuItem> getAvailableMenuItemsByCategory(Long categoryId, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findAvailableMenuItemsByCategory(categoryId, pageable);
        page.getContent().forEach(this::loadExtras);
        return page;
    }

    public Page<MenuItem> getAvailableMenuItemsByMealType(MealType mealType, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findAvailableMenuItemsByMealType(mealType, pageable);
        page.getContent().forEach(this::loadExtras);
        return page;
    }

    public Page<MenuItem> searchAvailableMenuItems(String searchTerm, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.searchAvailableMenuItems(searchTerm, pageable);
        page.getContent().forEach(this::loadExtras);
        return page;
    }

    public Page<MenuItem> getAvailableMenuItemsByProviderAndCategory(Long providerId, Long categoryId, Pageable pageable) {
        Page<MenuItem> page = menuItemRepository.findAvailableMenuItemsByProviderAndCategory(providerId, categoryId, pageable);
        page.getContent().forEach(this::loadExtras);
        return page;
    }

    public MenuItem getAvailableMenuItemById(Long itemId) {
        MenuItem item = menuItemRepository.findAvailableMenuItemById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found or not available"));
        loadExtras(item);
        return item;
    }

    // ---------------- Load product images ----------------
    @Transactional(readOnly = true)
    private void loadExtras(MenuItem menuItem) {
        List<com.platemate.model.Image> images = imageRepository.findAllByImageTypeAndOwnerId(
                ImageType.PRODUCT, menuItem.getId());
        menuItem.setImages(images);
    }

    // Public method to load extras for a menu item (used by controllers)
    public void loadMenuItemExtras(MenuItem menuItem) {
        loadExtras(menuItem);
    }
}
