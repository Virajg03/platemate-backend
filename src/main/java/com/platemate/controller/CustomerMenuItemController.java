package com.platemate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platemate.dto.MenuItemDtos;
import com.platemate.enums.MealType;
import com.platemate.model.MenuItem;
import com.platemate.service.MenuItemService;

@RestController
@RequestMapping("/api/customers/menu-items")
public class CustomerMenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    /**
     * Get all available menu items with pagination
     * Query params: page (default 0), size (default 20), sort (default "id,asc")
     */
    @GetMapping
    public ResponseEntity<MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse>> getAllMenuItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MenuItem> menuItemsPage = menuItemService.getAvailableMenuItems(pageable);
        
        Page<MenuItemDtos.CustomerResponse> responsePage = menuItemsPage.map(this::toCustomerResponse);
        MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse> paginatedResponse = 
            new MenuItemDtos.PaginatedResponse<>(responsePage);
        return ResponseEntity.ok(paginatedResponse);
    }

    /**
     * Get menu items by provider ID with pagination
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse>> getMenuItemsByProvider(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MenuItem> menuItemsPage = menuItemService.getAvailableMenuItemsByProvider(providerId, pageable);
        
        Page<MenuItemDtos.CustomerResponse> responsePage = menuItemsPage.map(this::toCustomerResponse);
        MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse> paginatedResponse = 
            new MenuItemDtos.PaginatedResponse<>(responsePage);
        return ResponseEntity.ok(paginatedResponse);
    }

    /**
     * Get menu items by category ID with pagination
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse>> getMenuItemsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MenuItem> menuItemsPage = menuItemService.getAvailableMenuItemsByCategory(categoryId, pageable);
        
        Page<MenuItemDtos.CustomerResponse> responsePage = menuItemsPage.map(this::toCustomerResponse);
        MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse> paginatedResponse = 
            new MenuItemDtos.PaginatedResponse<>(responsePage);
        return ResponseEntity.ok(paginatedResponse);
    }

    /**
     * Get menu items by meal type with pagination
     */
    @GetMapping("/meal-type/{mealType}")
    public ResponseEntity<MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse>> getMenuItemsByMealType(
            @PathVariable MealType mealType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MenuItem> menuItemsPage = menuItemService.getAvailableMenuItemsByMealType(mealType, pageable);
        
        Page<MenuItemDtos.CustomerResponse> responsePage = menuItemsPage.map(this::toCustomerResponse);
        MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse> paginatedResponse = 
            new MenuItemDtos.PaginatedResponse<>(responsePage);
        return ResponseEntity.ok(paginatedResponse);
    }

    /**
     * Search menu items by name or description with pagination
     */
    @GetMapping("/search")
    public ResponseEntity<MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse>> searchMenuItems(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MenuItem> menuItemsPage = menuItemService.searchAvailableMenuItems(q, pageable);
        
        Page<MenuItemDtos.CustomerResponse> responsePage = menuItemsPage.map(this::toCustomerResponse);
        MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse> paginatedResponse = 
            new MenuItemDtos.PaginatedResponse<>(responsePage);
        return ResponseEntity.ok(paginatedResponse);
    }

    /**
     * Get menu items by provider and category with pagination
     */
    @GetMapping("/provider/{providerId}/category/{categoryId}")
    public ResponseEntity<MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse>> getMenuItemsByProviderAndCategory(
            @PathVariable Long providerId,
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MenuItem> menuItemsPage = menuItemService.getAvailableMenuItemsByProviderAndCategory(
                providerId, categoryId, pageable);
        
        Page<MenuItemDtos.CustomerResponse> responsePage = menuItemsPage.map(this::toCustomerResponse);
        MenuItemDtos.PaginatedResponse<MenuItemDtos.CustomerResponse> paginatedResponse = 
            new MenuItemDtos.PaginatedResponse<>(responsePage);
        return ResponseEntity.ok(paginatedResponse);
    }

    /**
     * Get a single menu item by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDtos.CustomerResponse> getMenuItemById(@PathVariable Long id) {
        MenuItem menuItem = menuItemService.getAvailableMenuItemById(id);
        return ResponseEntity.ok(toCustomerResponse(menuItem));
    }

    /**
     * Convert MenuItem entity to CustomerResponse DTO
     */
    private MenuItemDtos.CustomerResponse toCustomerResponse(MenuItem item) {
        MenuItemDtos.CustomerResponse response = new MenuItemDtos.CustomerResponse();
        response.setId(item.getId());
        response.setItemName(item.getItemName());
        response.setDescription(item.getDescription());
        response.setPrice(item.getPrice());
        response.setIngredients(item.getIngredients());
        response.setMealType(item.getMealType());
        
        if (item.getCategory() != null) {
            response.setCategoryId(item.getCategory().getId());
            response.setCategoryName(item.getCategory().getCategoryName());
        }
        
        if (item.getProvider() != null) {
            response.setProviderId(item.getProvider().getId());
            response.setProviderBusinessName(item.getProvider().getBusinessName());
            if (item.getProvider().getUser() != null) {
                response.setProviderName(item.getProvider().getUser().getUsername());
            }
        }
        
        // Map images to base64 lists
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            java.util.List<String> base64List = item.getImages().stream()
                    .map(com.platemate.model.Image::getBase64Data)
                    .toList();
            java.util.List<String> fileTypeList = item.getImages().stream()
                    .map(com.platemate.model.Image::getFileType)
                    .toList();
            response.setImageBase64List(base64List);
            response.setImageFileTypeList(fileTypeList);
        }
        
        return response;
    }
}

