package com.resadmin.res.service;

import com.resadmin.res.entity.Delivery;
import com.resadmin.res.entity.Order;
import com.resadmin.res.entity.User;
import com.resadmin.res.repository.DeliveryRepository;
import com.resadmin.res.repository.OrderRepository;
import com.resadmin.res.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeliveryService {
    
    @Autowired
    private DeliveryRepository deliveryRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderService orderService;
    
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
    
    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }
    
    public Optional<Delivery> getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId);
    }
    
    public List<Delivery> getDeliveriesByDriver(Long driverId) {
        return deliveryRepository.findByDriverId(driverId);
    }
    
    public List<Delivery> getDeliveriesByStatus(Delivery.DeliveryStatus status) {
        return deliveryRepository.findByStatus(status);
    }
    
    public List<Delivery> getPendingDeliveries() {
        return deliveryRepository.findPendingDeliveries();
    }
    
    public List<Delivery> getActiveDeliveries() {
        return deliveryRepository.findActiveDeliveries();
    }
    
    public List<Delivery> getTodaysDeliveries() {
        return deliveryRepository.findTodaysDeliveries();
    }
    
    public Delivery assignDelivery(Long orderId, Long driverId, String deliveryAddress, String deliveryNotes) {
        // Validate order exists and is ready for delivery
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.READY_FOR_DELIVERY) {
            throw new RuntimeException("Order must be READY_FOR_DELIVERY to assign delivery. Current status: " + order.getStatus());
        }
        
        // Validate driver exists and is delivery staff
        User driver = userRepository.findById(driverId)
            .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));
        
        if (driver.getRole() != User.Role.DELIVERY_STAFF) {
            throw new RuntimeException("User must have DELIVERY_STAFF role to be assigned as driver");
        }
        
        if (!driver.getEnabled()) {
            throw new RuntimeException("Driver account is disabled");
        }
        
        // Check if delivery already exists for this order
        Optional<Delivery> existingDelivery = deliveryRepository.findByOrderId(orderId);
        if (existingDelivery.isPresent()) {
            throw new RuntimeException("Delivery already exists for order id: " + orderId);
        }
        
        // Create new delivery
        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setDriver(driver);
        delivery.setDeliveryAddress(deliveryAddress);
        delivery.setDeliveryNotes(deliveryNotes);
        delivery.setStatus(Delivery.DeliveryStatus.ASSIGNED);
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        
        // Update order status to OUT_FOR_DELIVERY
        orderService.updateOrderStatus(orderId, Order.OrderStatus.OUT_FOR_DELIVERY);
        
        return savedDelivery;
    }
    
    public Delivery updateDeliveryStatus(Long deliveryId, Delivery.DeliveryStatus newStatus) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));
        
        // Validate status transition
        validateStatusTransition(delivery.getStatus(), newStatus);
        
        Delivery.DeliveryStatus oldStatus = delivery.getStatus();
        delivery.setStatus(newStatus);
        
        // Set delivery timestamp when status changes to DELIVERED
        if (newStatus == Delivery.DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            // Also update order status to COMPLETED
            orderService.updateOrderStatus(delivery.getOrder().getId(), Order.OrderStatus.COMPLETED);
        }
        
        return deliveryRepository.save(delivery);
    }
    
    public Delivery updateDelivery(Long id, String deliveryAddress, String deliveryNotes) {
        Delivery delivery = deliveryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + id));
        
        // Only allow updates if delivery is not yet delivered
        if (delivery.getStatus() == Delivery.DeliveryStatus.DELIVERED) {
            throw new RuntimeException("Cannot update delivered delivery");
        }
        
        delivery.setDeliveryAddress(deliveryAddress);
        delivery.setDeliveryNotes(deliveryNotes);
        
        return deliveryRepository.save(delivery);
    }
    
    public Delivery reassignDriver(Long deliveryId, Long newDriverId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));
        
        // Only allow reassignment if delivery is not yet out for delivery
        if (delivery.getStatus() != Delivery.DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Cannot reassign driver for delivery with status: " + delivery.getStatus());
        }
        
        // Validate new driver
        User newDriver = userRepository.findById(newDriverId)
            .orElseThrow(() -> new RuntimeException("Driver not found with id: " + newDriverId));
        
        if (newDriver.getRole() != User.Role.DELIVERY_STAFF) {
            throw new RuntimeException("User must have DELIVERY_STAFF role to be assigned as driver");
        }
        
        if (!newDriver.getEnabled()) {
            throw new RuntimeException("Driver account is disabled");
        }
        
        delivery.setDriver(newDriver);
        return deliveryRepository.save(delivery);
    }
    
    public void cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));
        
        if (delivery.getStatus() == Delivery.DeliveryStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered delivery");
        }
        
        // Update order status back to READY_FOR_DELIVERY
        orderService.updateOrderStatus(delivery.getOrder().getId(), Order.OrderStatus.READY_FOR_DELIVERY);
        
        deliveryRepository.delete(delivery);
    }
    
    public List<Delivery> getDeliveriesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return deliveryRepository.findByDispatchedAtBetween(startDate, endDate);
    }
    
    public Long getCompletedDeliveryCount() {
        return deliveryRepository.countCompletedDeliveries();
    }
    
    public Long getActiveDeliveryCount() {
        return deliveryRepository.countActiveDeliveries();
    }
    
    public List<User> getAvailableDrivers() {
        return userRepository.findAvailableDeliveryStaff();
    }
    
    private void validateStatusTransition(Delivery.DeliveryStatus currentStatus, Delivery.DeliveryStatus newStatus) {
        switch (currentStatus) {
            case ASSIGNED:
                if (newStatus != Delivery.DeliveryStatus.OUT_FOR_DELIVERY) {
                    throw new RuntimeException("Invalid status transition from ASSIGNED to " + newStatus);
                }
                break;
            case OUT_FOR_DELIVERY:
                if (newStatus != Delivery.DeliveryStatus.DELIVERED) {
                    throw new RuntimeException("Invalid status transition from OUT_FOR_DELIVERY to " + newStatus);
                }
                break;
            case DELIVERED:
                throw new RuntimeException("Cannot change status from DELIVERED");
        }
    }
}