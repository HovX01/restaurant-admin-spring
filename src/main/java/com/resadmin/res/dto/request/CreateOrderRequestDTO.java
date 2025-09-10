package com.resadmin.res.dto.request;

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
    @NotBlank(message = "Customer details are required")
    @Size(max = 500, message = "Customer details must not exceed 500 characters")
    private String customerDetails;
    
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    private BigDecimal totalPrice;
    
    @NotNull(message = "Order type is required")
    private Order.OrderType orderType;
    
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<CreateOrderItemRequestDTO> orderItems;
}