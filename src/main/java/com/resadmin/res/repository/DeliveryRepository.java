package com.resadmin.res.repository;

import com.resadmin.res.entity.Delivery;
import com.resadmin.res.entity.Order;
import com.resadmin.res.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    Optional<Delivery> findByOrder(Order order);
    
    Optional<Delivery> findByOrderId(Long orderId);
    
    List<Delivery> findByDriver(User driver);
    
    List<Delivery> findByDriverId(Long driverId);
    
    Page<Delivery> findByDriverId(Long driverId, Pageable pageable);
    
    List<Delivery> findByStatus(Delivery.DeliveryStatus status);
    
    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.status IN :statuses")
    List<Delivery> findByDriverIdAndStatusIn(@Param("driverId") Long driverId, @Param("statuses") List<Delivery.DeliveryStatus> statuses);
    
    @Query("SELECT d FROM Delivery d WHERE d.status = 'PENDING' ORDER BY d.dispatchedAt ASC")
    List<Delivery> findPendingDeliveries();
    
    @Query("SELECT d FROM Delivery d WHERE d.status IN ('ASSIGNED', 'OUT_FOR_DELIVERY') ORDER BY d.dispatchedAt ASC")
    List<Delivery> findActiveDeliveries();
    
    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId AND d.status IN ('ASSIGNED', 'OUT_FOR_DELIVERY')")
    List<Delivery> findActiveDeliveriesByDriver(@Param("driverId") Long driverId);
    
    @Query("SELECT d FROM Delivery d WHERE CAST(d.dispatchedAt AS date) = CURRENT_DATE")
    List<Delivery> findTodaysDeliveries();
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE CAST(d.dispatchedAt AS date) = CURRENT_DATE AND d.status = 'DELIVERED'")
    Long countTodaysCompletedDeliveries();
    
    @Query("SELECT d FROM Delivery d WHERE d.dispatchedAt BETWEEN :startDate AND :endDate")
    List<Delivery> findByDispatchedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT d FROM Delivery d WHERE d.status IN :statuses ORDER BY d.dispatchedAt DESC")
    List<Delivery> findByStatusInOrderByDispatchedAtDesc(@Param("statuses") List<Delivery.DeliveryStatus> statuses);
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driver.id = :driverId AND d.status IN ('ASSIGNED', 'OUT_FOR_DELIVERY')")
    Long countActiveDeliveriesByDriver(@Param("driverId") Long driverId);
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.status = 'DELIVERED'")
    long countCompletedDeliveries();
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.status IN ('ASSIGNED', 'OUT_FOR_DELIVERY')")
    long countActiveDeliveries();
}