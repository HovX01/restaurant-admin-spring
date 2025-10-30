package com.resadmin.res.dto.request;

import com.resadmin.res.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentRequestDTO {
    @NotNull(message = "Payment status is required")
    private Boolean isPaid;
    
    private Order.PaymentMethod paymentMethod;
}
