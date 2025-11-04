package com.platemate.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.dto.CategoryDtos;
import com.platemate.model.Category;
import com.platemate.service.CategoryService;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<List<CategoryDtos.Response>> listCategories() {
        List<CategoryDtos.Response> data = service.getCatogaries().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(data);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDtos.Response> createCategory(@RequestBody CategoryDtos.CreateRequest req) {
        Category category = new Category();
        category.setCategoryName(req.getCategoryName());
        category.setDescription(req.getDescription());
        Category saved = service.createCategory(category);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDtos.Response> updateCategory(@PathVariable Long id, @RequestBody CategoryDtos.UpdateRequest req) {
        Category update = new Category();
        update.setCategoryName(req.getCategoryName());
        update.setDescription(req.getDescription());
        Category saved = service.updateCategory(id, update);
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER', 'CUSTOMER')")
    public ResponseEntity<CategoryDtos.Response> getById(@PathVariable Long id) {
        return service.findActiveById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private CategoryDtos.Response toResponse(Category category) {
        CategoryDtos.Response res = new CategoryDtos.Response();
        res.setId(category.getId());
        res.setCategoryName(category.getCategoryName());
        res.setDescription(category.getDescription());
        return res;
    }
}