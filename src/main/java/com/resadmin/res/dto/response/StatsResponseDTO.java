package com.resadmin.res.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponseDTO {
    private Long orderCount;
    private java.math.BigDecimal revenue;
    private Long completedCount;
    private Long activeCount;
    
    // Factory methods for different types of stats
    public static StatsResponseDTO orderStats(Long orderCount, java.math.BigDecimal revenue) {
        return StatsResponseDTO.builder()
                .orderCount(orderCount)
                .revenue(revenue)
                .build();
    }
    
    public static StatsResponseDTO deliveryStats(Long completedCount, Long activeCount) {
        return StatsResponseDTO.builder()
                .completedCount(completedCount)
                .activeCount(activeCount)
                .build();
    }
}