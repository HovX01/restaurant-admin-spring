package com.resadmin.res.dto.request;

import com.resadmin.res.entity.Delivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequestDTO {
    @NotNull(message = "Status is required")
    private Delivery.DeliveryStatus status;
}