package com.resadmin.res.repository;

import com.resadmin.res.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    List<Order> findByOrderType(Order.OrderType orderType);
    
    List<Order> findByStatusAndOrderType(Order.OrderStatus status, Order.OrderType orderType);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByStatusInOrderByCreatedAtDesc(@Param("statuses") List<Order.OrderStatus> statuses);
    
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' OR o.status = 'PREPARING' ORDER BY o.createdAt ASC")
    List<Order> findKitchenOrders();
    
    @Query("SELECT o FROM Order o WHERE o.status = 'READY_FOR_DELIVERY' ORDER BY o.createdAt ASC")
    List<Order> findOrdersReadyForDelivery();
    
    @Query("SELECT o FROM Order o WHERE o.orderType = 'DELIVERY' AND o.status = 'OUT_FOR_DELIVERY'")
    List<Order> findActiveDeliveryOrders();
    
    @Query("SELECT o FROM Order o WHERE CAST(o.createdAt AS date) = CURRENT_DATE ORDER BY o.createdAt DESC")
    List<Order> findTodaysOrders();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE CAST(o.createdAt AS date) = CURRENT_DATE")
    Long countTodaysOrders();
    
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE CAST(o.createdAt AS date) = CURRENT_DATE AND o.status = 'COMPLETED'")
    BigDecimal getTodaysRevenue();
    
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllOrderByCreatedAtDesc();
    
    List<Order> findByIsPaid(Boolean isPaid);
    
    List<Order> findByPaymentMethod(Order.PaymentMethod paymentMethod);
    
    @Query("SELECT o FROM Order o WHERE o.isPaid = :isPaid AND o.status = :status")
    List<Order> findByIsPaidAndStatus(@Param("isPaid") Boolean isPaid, @Param("status") Order.OrderStatus status);
}