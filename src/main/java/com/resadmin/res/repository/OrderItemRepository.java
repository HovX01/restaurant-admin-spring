package com.resadmin.res.repository;

import com.resadmin.res.entity.Order;
import com.resadmin.res.entity.OrderItem;
import com.resadmin.res.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrder(Order order);
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByProduct(Product product);
    
    List<OrderItem> findByProductId(Long productId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findItemsByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o WHERE o.status IN :statuses")
    List<OrderItem> findByOrderStatuses(@Param("statuses") List<Order.OrderStatus> statuses);
    
    @Query("SELECT p.name, SUM(oi.quantity) as totalQuantity FROM OrderItem oi JOIN oi.product p GROUP BY p.id, p.name ORDER BY totalQuantity DESC")
    List<Object[]> findMostOrderedProducts();
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.product.id = :productId")
    OrderItem findByOrderIdAndProductId(@Param("orderId") Long orderId, @Param("productId") Long productId);
    
    void deleteByOrderId(Long orderId);
}