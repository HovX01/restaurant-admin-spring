package com.resadmin.res.dto;

import com.resadmin.res.entity.Delivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {
    private Long id;
    
    private Long orderId;
    
    private UserDTO driver;
    
    private Delivery.DeliveryStatus status;
    
    private LocalDateTime dispatchedAt;
    
    private LocalDateTime deliveredAt;
    
    @Size(max = 500, message = "Delivery address must not exceed 500 characters")
    private String deliveryAddress;
    
    @Size(max = 1000, message = "Delivery notes must not exceed 1000 characters")
    private String deliveryNotes;
}