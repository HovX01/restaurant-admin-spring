package com.resadmin.res.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.resadmin.res.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {
    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;
    
    @NotBlank(message = "Customer phone is required")
    @Size(max = 20, message = "Customer phone must not exceed 20 characters")
    private String customerPhone;
    
    @Size(max = 200, message = "Customer address must not exceed 200 characters")
    private String customerAddress;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @JsonAlias({"totalPrice"})
    private BigDecimal totalAmount;
    
    private Order.OrderType orderType;
    
    private Order.PaymentMethod paymentMethod;
    
    @NotEmpty(message = "Order items are required")
    @Valid
    @JsonAlias({"orderItems"})
    private List<CreateOrderItemRequestDTO> items;
    
    public String getCustomerDetails() {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(customerName);
        details.append(" | Phone: ").append(customerPhone);
        if (customerAddress != null && !customerAddress.isBlank()) {
            details.append(" | Address: ").append(customerAddress);
        }
        if (notes != null && !notes.isBlank()) {
            details.append(" | Notes: ").append(notes);
        }
        return details.toString();
    }
    
    public BigDecimal getTotalPrice() {
        return totalAmount;
    }
    
    public List<CreateOrderItemRequestDTO> getOrderItems() {
        return items;
    }
    
    public Order.OrderType getOrderType() {
        if (orderType == null) {
            if (customerAddress != null && !customerAddress.isBlank()) {
                return Order.OrderType.DELIVERY;
            }
            return Order.OrderType.TAKEOUT;
        }
        return orderType;
    }
}