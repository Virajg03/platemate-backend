package com.platemate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Category;
import com.platemate.repository.CategoryRepository;

@Service
public class CategoryService {
    
    @Autowired
    CategoryRepository categoryRepository;

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getCatogaries() {
        return categoryRepository.findAllByIsActiveTrue();
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        return categoryRepository.findById(id).map(category -> {
            category.setCategoryName(updatedCategory.getCategoryName());
            category.setDescription(updatedCategory.getDescription());
            return categoryRepository.save(category);
        }).orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }
    
    public Category softDelete(Long id) {
        return categoryRepository.findById(id).map(category -> {
            category.setActive(false);
            return categoryRepository.save(category);
        }).orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    public java.util.Optional<Category> findActiveById(Long id) {
        return categoryRepository.findById(id).filter(Category::isActive);
    }
    
}
