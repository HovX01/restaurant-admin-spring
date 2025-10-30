package com.resadmin.res.service;

import com.resadmin.res.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void testGetTodaysRevenue_ShouldReturnBigDecimal() {
        // Given
        BigDecimal expectedRevenue = new BigDecimal("123.45");
        when(orderRepository.getTodaysRevenue()).thenReturn(expectedRevenue);

        // When
        BigDecimal actualRevenue = orderService.getTodaysRevenue();

        // Then
        assertEquals(expectedRevenue, actualRevenue);
    }

    @Test
    public void testGetTodaysRevenue_ShouldReturnZeroWhenNull() {
        // Given
        when(orderRepository.getTodaysRevenue()).thenReturn(null);

        // When
        BigDecimal actualRevenue = orderService.getTodaysRevenue();

        // Then
        assertEquals(BigDecimal.ZERO, actualRevenue);
    }
}
