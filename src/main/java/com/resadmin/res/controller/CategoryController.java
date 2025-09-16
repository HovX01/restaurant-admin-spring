package com.resadmin.res.controller;

import com.resadmin.res.dto.CategoryDTO;
import com.resadmin.res.dto.response.ApiResponseDTO;
import com.resadmin.res.dto.response.PagedResponseDTO;
import com.resadmin.res.mapper.EntityMapper;
import com.resadmin.res.entity.Category;
import com.resadmin.res.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
import com.resadmin.res.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@Tag(name = "Categories", description = "Category management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve a paginated list of categories with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<CategoryDTO>>> getAllCategories(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filter by name (contains)") @RequestParam(required = false) String name) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Category> categoryPage = categoryService.getAllCategoriesWithFilters(pageable, name);
        
        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(EntityMapper::toCategoryDTO)
                .collect(java.util.stream.Collectors.toList());
        
        PagedResponseDTO<CategoryDTO> pagedResponse = EntityMapper.toPagedResponseDTO(categoryPage, categoryDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Categories retrieved successfully", pagedResponse));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category found",
                content = @Content(schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            CategoryDTO categoryDTO = EntityMapper.toCategoryDTO(category.get());
            return ResponseEntity.ok(ApiResponseDTO.success("Category retrieved successfully", categoryDTO));
        } else {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create new category", description = "Create a new category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully",
                content = @Content(schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> createCategory(
            @Parameter(description = "Category data") @Valid @RequestBody Category category) {
        Category savedCategory = categoryService.createCategory(category);
        CategoryDTO categoryDTO = EntityMapper.toCategoryDTO(savedCategory);
        return ResponseEntity.ok(ApiResponseDTO.success("Category created successfully", categoryDTO));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<CategoryDTO>> updateCategory(@PathVariable Long id, @Valid @RequestBody Category categoryDetails) {
        Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
        CategoryDTO categoryDTO = EntityMapper.toCategoryDTO(updatedCategory);
        return ResponseEntity.ok(ApiResponseDTO.success("Category updated successfully", categoryDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Category deleted successfully", null));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<CategoryDTO>>> searchCategories(@RequestParam String name) {
        List<Category> categories = categoryService.searchCategories(name);
        List<CategoryDTO> categoryDTOs = EntityMapper.toCategoryDTOList(categories);
        return ResponseEntity.ok(ApiResponseDTO.success("Categories retrieved successfully", categoryDTOs));
    }
    
    @GetMapping("/exists/{name}")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkCategoryExists(@PathVariable String name) {
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(ApiResponseDTO.success("Category existence checked", exists));
    }
}