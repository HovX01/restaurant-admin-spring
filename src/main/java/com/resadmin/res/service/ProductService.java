package com.resadmin.res.service;

import com.resadmin.res.entity.Category;
import com.resadmin.res.entity.Product;
import com.resadmin.res.exception.ResourceNotFoundException;
import com.resadmin.res.repository.CategoryRepository;
import com.resadmin.res.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> getAvailableProducts() {
        return productRepository.findAllAvailableOrderByCategoryAndName();
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    public List<Product> getAvailableProductsByCategory(Long categoryId) {
        return productRepository.findAvailableProductsByCategoryOrderByName(categoryId);
    }
    
    public Product createProduct(Product product) {
        if (productRepository.existsByName(product.getName())) {
            throw new DataIntegrityViolationException("Product with name '" + product.getName() + "' duplicate");
        }
        
        // Validate category exists
        Category category = categoryRepository.findById(product.getCategory().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.getCategory().getId()));
        
        product.setCategory(category);
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!product.getName().equals(productDetails.getName()) && 
            productRepository.existsByName(productDetails.getName())) {
            throw new DataIntegrityViolationException("Product with name '" + productDetails.getName() + "' duplicate");
        }
        
        // Validate category exists if being changed
        if (!product.getCategory().getId().equals(productDetails.getCategory().getId())) {
            Category category = categoryRepository.findById(productDetails.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDetails.getCategory().getId()));
            product.setCategory(category);
        }
        
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setIsAvailable(productDetails.getIsAvailable());
        
        return productRepository.save(product);
    }
    
    public Product toggleAvailability(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        product.setIsAvailable(!product.getIsAvailable());
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        productRepository.delete(product);
    }
    
    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
    
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }
    
    public org.springframework.data.domain.Page<Product> getAllProductsWithFilters(
            org.springframework.data.domain.Pageable pageable,
            String category,
            Boolean available,
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        
        org.springframework.data.jpa.domain.Specification<Product> spec = 
                (root, query, criteriaBuilder) -> {
                    java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
                    
                    if (category != null && !category.trim().isEmpty()) {
                        predicates.add(criteriaBuilder.equal(
                                root.get("category").get("name"), category));
                    }
                    
                    if (available != null) {
                        predicates.add(criteriaBuilder.equal(
                                root.get("isAvailable"), available));
                    }
                    
                    if (name != null && !name.trim().isEmpty()) {
                        predicates.add(criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"));
                    }
                    
                    if (minPrice != null) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                                root.get("price"), minPrice));
                    }
                    
                    if (maxPrice != null) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(
                                root.get("price"), maxPrice));
                    }
                    
                    return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
        
        return productRepository.findAll(spec, pageable);
    }
}