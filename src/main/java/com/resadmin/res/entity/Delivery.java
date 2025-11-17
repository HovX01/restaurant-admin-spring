package com.resadmin.res.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "dispatched_at", updatable = false)
    private LocalDateTime dispatchedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;
    
    @Column(name = "delivery_notes", length = 1000)
    private String deliveryNotes;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    // Enum for delivery status
    public enum DeliveryStatus {
        PENDING, ASSIGNED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }
    
    // Constructors
    public Delivery() {}
    
    public Delivery(Order order, String deliveryAddress) {
        this.order = order;
        this.deliveryAddress = deliveryAddress;
        this.status = DeliveryStatus.PENDING;
    }
    
    public Delivery(Order order, User driver, String deliveryAddress) {
        this.order = order;
        this.driver = driver;
        this.deliveryAddress = deliveryAddress;
        this.status = DeliveryStatus.ASSIGNED;
    }
    
    // Helper methods
    public void assignDriver(User driver) {
        this.driver = driver;
        this.status = DeliveryStatus.ASSIGNED;
    }
    
    public void markAsOutForDelivery() {
        this.status = DeliveryStatus.OUT_FOR_DELIVERY;
    }
    
    public void markAsDelivered() {
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = DeliveryStatus.CANCELLED;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public User getDriver() {
        return driver;
    }
    
    public void setDriver(User driver) {
        this.driver = driver;
    }
    
    public DeliveryStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }
    
    public void setDispatchedAt(LocalDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getDeliveryNotes() {
        return deliveryNotes;
    }
    
    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    @Override
    public String toString() {
        return "Delivery{" +
                "id=" + id +
                ", status=" + status +
                ", dispatchedAt=" + dispatchedAt +
                ", deliveredAt=" + deliveredAt +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                '}';
    }
}