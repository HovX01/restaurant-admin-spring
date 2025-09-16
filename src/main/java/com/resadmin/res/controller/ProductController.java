package com.resadmin.res.controller;

import com.resadmin.res.dto.ProductDTO;
import com.resadmin.res.dto.response.ApiResponseDTO;
import com.resadmin.res.dto.response.PagedResponseDTO;
import com.resadmin.res.mapper.EntityMapper;
import com.resadmin.res.entity.Product;
import com.resadmin.res.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import com.resadmin.res.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "Product management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a paginated list of products with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<ProductDTO>>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by availability") @RequestParam(required = false) Boolean available,
            @Parameter(description = "Filter by name (contains)") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by minimum price") @RequestParam(required = false) java.math.BigDecimal minPrice,
            @Parameter(description = "Filter by maximum price") @RequestParam(required = false) java.math.BigDecimal maxPrice) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productService.getAllProductsWithFilters(
                pageable, category, available, name, minPrice, maxPrice);
        
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(EntityMapper::toProductDTO)
                .collect(java.util.stream.Collectors.toList());
        
        PagedResponseDTO<ProductDTO> pagedResponse = EntityMapper.toPagedResponseDTO(productPage, productDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Products retrieved successfully", pagedResponse));
    }
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getAvailableProducts() {
        List<Product> products = productService.getAvailableProducts();
        List<ProductDTO> productDTOs = EntityMapper.toProductDTOList(products);
        return ResponseEntity.ok(ApiResponseDTO.success("Available products retrieved successfully", productDTOs));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
                content = @Content(schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<ProductDTO>> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            ProductDTO productDTO = EntityMapper.toProductDTO(product.get());
            return ResponseEntity.ok(ApiResponseDTO.success("Product retrieved successfully", productDTO));
        } else {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        List<ProductDTO> productDTOs = EntityMapper.toProductDTOList(products);
        return ResponseEntity.ok(ApiResponseDTO.success("Products retrieved successfully", productDTOs));
    }
    
    @GetMapping("/category/{categoryId}/available")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getAvailableProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getAvailableProductsByCategory(categoryId);
        List<ProductDTO> productDTOs = EntityMapper.toProductDTOList(products);
        return ResponseEntity.ok(ApiResponseDTO.success("Available products retrieved successfully", productDTOs));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create new product", description = "Create a new product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully",
                content = @Content(schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<ProductDTO>> createProduct(
            @Parameter(description = "Product data") @Valid @RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        ProductDTO productDTO = EntityMapper.toProductDTO(savedProduct);
        return ResponseEntity.ok(ApiResponseDTO.success("Product created successfully", productDTO));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        ProductDTO productDTO = EntityMapper.toProductDTO(updatedProduct);
        return ResponseEntity.ok(ApiResponseDTO.success("Product updated successfully", productDTO));
    }
    
    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('KITCHEN_STAFF')")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> toggleProductAvailability(@PathVariable Long id) {
        Product updatedProduct = productService.toggleAvailability(id);
        ProductDTO productDTO = EntityMapper.toProductDTO(updatedProduct);
        return ResponseEntity.ok(ApiResponseDTO.success("Product availability updated", productDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Product deleted successfully", null));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProducts(name);
        List<ProductDTO> productDTOs = EntityMapper.toProductDTOList(products);
        return ResponseEntity.ok(ApiResponseDTO.success("Products retrieved successfully", productDTOs));
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        List<ProductDTO> productDTOs = EntityMapper.toProductDTOList(products);
        return ResponseEntity.ok(ApiResponseDTO.success("Products retrieved successfully", productDTOs));
    }
    
    @GetMapping("/exists/{name}")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkProductExists(@PathVariable String name) {
        boolean exists = productService.existsByName(name);
        return ResponseEntity.ok(ApiResponseDTO.success("Product existence checked", exists));
    }
}