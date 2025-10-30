package com.resadmin.res.controller;

import com.resadmin.res.dto.*;
import com.resadmin.res.dto.request.*;
import com.resadmin.res.dto.response.*;
import com.resadmin.res.dto.response.StatsResponseDTO;
import com.resadmin.res.entity.Order;
import com.resadmin.res.entity.OrderItem;
import com.resadmin.res.exception.ResourceNotFoundException;
import com.resadmin.res.mapper.EntityMapper;
import com.resadmin.res.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Orders", description = "Order management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('KITCHEN_STAFF') or hasRole('DELIVERY_STAFF')")
    @Operation(summary = "Get all orders", description = "Retrieve a paginated list of orders with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<OrderDTO>>> getAllOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) Order.OrderStatus status,
            @Parameter(description = "Filter by order type") @RequestParam(required = false) Order.OrderType orderType,
            @Parameter(description = "Filter from date (ISO format: yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Filter to date (ISO format: yyyy-MM-ddTHH:mm:ss)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orderPage = orderService.getAllOrdersPaginated(pageable, status, orderType, from, to);
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orderPage.getContent());
        PagedResponseDTO<OrderDTO> pagedResponse = EntityMapper.toPagedResponseDTO(orderPage, orderDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Orders retrieved successfully", pagedResponse));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('KITCHEN_STAFF') or hasRole('DELIVERY_STAFF')")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<OrderDTO>> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order", "id", id);
        }
        
        OrderDTO orderDTO = EntityMapper.toOrderDTO(order.get());
        return ResponseEntity.ok(ApiResponseDTO.success("Order retrieved successfully", orderDTO));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('KITCHEN_STAFF') or hasRole('DELIVERY_STAFF')")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<OrderDTO>>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orderPage = orderService.getOrdersByStatusPaginated(status, pageable);
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orderPage.getContent());
        PagedResponseDTO<OrderDTO> pagedResponse = EntityMapper.toPagedResponseDTO(orderPage, orderDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Orders retrieved successfully", pagedResponse));
    }
    
    @GetMapping("/kitchen")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('KITCHEN_STAFF')")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getKitchenOrders() {
        List<Order> orders = orderService.getKitchenOrders();
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orders);
        return ResponseEntity.ok(ApiResponseDTO.success("Kitchen orders retrieved successfully", orderDTOs));
    }
    
    @GetMapping("/delivery")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DELIVERY_STAFF')")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getDeliveryOrders() {
        List<Order> orders = orderService.getOrdersReadyForDelivery();
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orders);
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery orders retrieved successfully", orderDTOs));
    }
    
    @GetMapping("/ready-for-delivery")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DELIVERY_STAFF')")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getOrdersReadyForDelivery() {
        List<Order> orders = orderService.getOrdersReadyForDelivery();
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orders);
        return ResponseEntity.ok(ApiResponseDTO.success("Orders ready for delivery retrieved successfully", orderDTOs));
    }
    
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getTodaysOrders() {
        List<Order> orders = orderService.getTodaysOrders();
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orders);
        return ResponseEntity.ok(ApiResponseDTO.success("Today's orders retrieved successfully", orderDTOs));
    }
    
    @PostMapping
    @Operation(summary = "Create new order", description = "Create a new order with order items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<OrderDTO>> createOrder(
            @Parameter(description = "Order creation request") @Valid @RequestBody CreateOrderRequestDTO createOrderRequest) {
        Order savedOrder = orderService.createOrderFromDTO(createOrderRequest);
        OrderDTO orderDTO = EntityMapper.toOrderDTO(savedOrder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Order created successfully", orderDTO));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> updateOrder(@PathVariable Long id, @Valid @RequestBody CreateOrderRequestDTO orderDetails) {
        Order updatedOrder = orderService.updateOrderFromDTO(id, orderDetails);
        OrderDTO orderDTO = EntityMapper.toOrderDTO(updatedOrder);
        return ResponseEntity.ok(ApiResponseDTO.success("Order updated successfully", orderDTO));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('KITCHEN_STAFF') or hasRole('DELIVERY_STAFF')")
    @Operation(summary = "Update order status", description = "Update the status of an existing order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<OrderDTO>> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id, 
            @Parameter(description = "Status update request") @Valid @RequestBody UpdateOrderStatusRequestDTO updateStatusRequest) {
        Order updatedOrder = orderService.updateOrderStatus(id, updateStatusRequest.getStatus());
        OrderDTO orderDTO = EntityMapper.toOrderDTO(updatedOrder);
        return ResponseEntity.ok(ApiResponseDTO.success("Order status updated successfully", orderDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Order deleted successfully", null));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<OrderDTO>>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orderPage = orderService.getOrdersByDateRangePaginated(startDate, endDate, pageable);
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orderPage.getContent());
        PagedResponseDTO<OrderDTO> pagedResponse = EntityMapper.toPagedResponseDTO(orderPage, orderDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Orders retrieved successfully", pagedResponse));
    }
    
    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponseDTO<List<OrderItemDTO>>> getOrderItems(@PathVariable Long id) {
        List<OrderItem> orderItems = orderService.getOrderItems(id);
        List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(EntityMapper::toOrderItemDTO)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponseDTO.success("Order items retrieved successfully", orderItemDTOs));
    }
    
    @GetMapping("/stats/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<StatsResponseDTO>> getTodaysStats() {
        BigDecimal revenue = orderService.getTodaysRevenue();
        
        StatsResponseDTO stats = StatsResponseDTO.orderStats(
            orderService.getTodaysOrderCount(),
            revenue
        );
        return ResponseEntity.ok(ApiResponseDTO.success("Today's statistics retrieved successfully", stats));
    }
    
    @PatchMapping("/{id}/payment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Update payment status", description = "Update the payment status and method of an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status updated successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<OrderDTO>> updatePaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Payment update request") @Valid @RequestBody com.resadmin.res.dto.request.UpdatePaymentRequestDTO updatePaymentRequest) {
        Order updatedOrder = orderService.updatePaymentStatus(id, updatePaymentRequest.getIsPaid(), updatePaymentRequest.getPaymentMethod());
        OrderDTO orderDTO = EntityMapper.toOrderDTO(updatedOrder);
        return ResponseEntity.ok(ApiResponseDTO.success("Payment status updated successfully", orderDTO));
    }
    
    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Mark order as paid", description = "Mark an order as paid with payment method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order marked as paid successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<OrderDTO>> markOrderAsPaid(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Payment method") @RequestParam(required = false) Order.PaymentMethod paymentMethod) {
        Order updatedOrder = orderService.markOrderAsPaid(id, paymentMethod);
        OrderDTO orderDTO = EntityMapper.toOrderDTO(updatedOrder);
        return ResponseEntity.ok(ApiResponseDTO.success("Order marked as paid successfully", orderDTO));
    }
    
    @PostMapping("/{id}/mark-unpaid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Mark order as unpaid", description = "Mark an order as unpaid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order marked as unpaid successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<OrderDTO>> markOrderAsUnpaid(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        Order updatedOrder = orderService.markOrderAsUnpaid(id);
        OrderDTO orderDTO = EntityMapper.toOrderDTO(updatedOrder);
        return ResponseEntity.ok(ApiResponseDTO.success("Order marked as unpaid successfully", orderDTO));
    }
    
    @GetMapping("/paid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get paid orders", description = "Retrieve all paid orders")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getPaidOrders() {
        List<Order> orders = orderService.getPaidOrders();
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orders);
        return ResponseEntity.ok(ApiResponseDTO.success("Paid orders retrieved successfully", orderDTOs));
    }
    
    @GetMapping("/unpaid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get unpaid orders", description = "Retrieve all unpaid orders")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getUnpaidOrders() {
        List<Order> orders = orderService.getUnpaidOrders();
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orders);
        return ResponseEntity.ok(ApiResponseDTO.success("Unpaid orders retrieved successfully", orderDTOs));
    }
    
    @GetMapping("/payment-method/{paymentMethod}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get orders by payment method", description = "Retrieve all orders with a specific payment method")
    public ResponseEntity<ApiResponseDTO<List<OrderDTO>>> getOrdersByPaymentMethod(
            @Parameter(description = "Payment method") @PathVariable Order.PaymentMethod paymentMethod) {
        List<Order> orders = orderService.getOrdersByPaymentMethod(paymentMethod);
        List<OrderDTO> orderDTOs = EntityMapper.toOrderDTOList(orders);
        return ResponseEntity.ok(ApiResponseDTO.success("Orders retrieved successfully", orderDTOs));
    }
}