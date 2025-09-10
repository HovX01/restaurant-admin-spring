package com.resadmin.res.repository;

import com.resadmin.res.entity.Category;
import com.resadmin.res.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    List<Product> findByCategory(Category category);
    
    List<Product> findByCategoryId(Long categoryId);
    
    List<Product> findByIsAvailableTrue();
    
    List<Product> findByCategoryAndIsAvailableTrue(Category category);
    
    List<Product> findByCategoryIdAndIsAvailableTrue(Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isAvailable = true ORDER BY p.name ASC")
    List<Product> findAvailableProductsByCategoryOrderByName(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.isAvailable = true ORDER BY p.category.name ASC, p.name ASC")
    List<Product> findAllAvailableOrderByCategoryAndName();
    
    boolean existsByName(String name);
    
    Optional<Product> findByName(String name);
}