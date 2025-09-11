package com.resadmin.res.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryRequestDTO {
    @Size(max = 500, message = "Delivery address must not exceed 500 characters")
    private String deliveryAddress;
    
    @Size(max = 1000, message = "Delivery notes must not exceed 1000 characters")
    private String deliveryNotes;
}