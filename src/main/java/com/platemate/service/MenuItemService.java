package com.platemate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.MenuItem;
import com.platemate.repository.MenuItemRepository;

@Service
public class MenuItemService {
    
    @Autowired
    MenuItemRepository menuItemRepository;


    public MenuItem addMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

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
}
