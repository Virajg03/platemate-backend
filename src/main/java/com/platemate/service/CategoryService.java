package com.platemate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.platemate.model.Category;
import com.platemate.repository.CategoryRepository;

public class CategoryService {
    
    @Autowired
    CategoryRepository categoryRepository;

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getCatogaries() {
        return categoryRepository.findAll();
    }
    
}
