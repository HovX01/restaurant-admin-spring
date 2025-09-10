package com.resadmin.res.dto;

import com.resadmin.res.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    
    @NotBlank(message = "Customer details are required")
    @Size(max = 500, message = "Customer details must not exceed 500 characters")
    private String customerDetails;
    
    private Order.OrderStatus status;
    
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    private BigDecimal totalPrice;
    
    @NotNull(message = "Order type is required")
    private Order.OrderType orderType;
    
    private LocalDateTime createdAt;
    
    private List<OrderItemDTO> orderItems;
    
    private DeliveryDTO delivery;
}