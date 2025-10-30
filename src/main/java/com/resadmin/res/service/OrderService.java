package com.resadmin.res.service;

import com.resadmin.res.dto.request.CreateOrderRequestDTO;
import com.resadmin.res.dto.request.CreateOrderItemRequestDTO;
import com.resadmin.res.entity.Order;
import com.resadmin.res.entity.OrderItem;
import com.resadmin.res.entity.Product;
import com.resadmin.res.exception.ResourceNotFoundException;
import com.resadmin.res.mapper.EntityMapper;
import com.resadmin.res.repository.OrderItemRepository;
import com.resadmin.res.repository.OrderRepository;
import com.resadmin.res.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private WebSocketService webSocketService;
    
    public List<Order> getAllOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc();
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public List<Order> getKitchenOrders() {
        return orderRepository.findKitchenOrders();
    }
    
    public List<Order> getOrdersReadyForDelivery() {
        return orderRepository.findOrdersReadyForDelivery();
    }
    
    public List<Order> getTodaysOrders() {
        return orderRepository.findTodaysOrders();
    }
    
    public Order createOrder(Order order, List<OrderItem> orderItems) {
        // Validate and calculate total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.getProduct().getId()));
            
            if (!product.getIsAvailable()) {
                throw new RuntimeException("Product '" + product.getName() + "' is not available");
            }
            
            item.setProduct(product);
            item.setPrice(product.getPrice());
            totalPrice = totalPrice.add(item.getTotalPrice());
        }
        
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.OrderStatus.PENDING);
        
        // Save order first
        Order savedOrder = orderRepository.save(order);
        
        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }
        
        // Send WebSocket notification for new order
        webSocketService.notifyOrderCreated(savedOrder);
        webSocketService.notifyKitchenNewOrder(savedOrder);
        
        return savedOrder;
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        // Send WebSocket notification for status change
        webSocketService.notifyOrderStatusChanged(updatedOrder);
        
        // Send specific notifications based on status
        if (newStatus == Order.OrderStatus.READY_FOR_DELIVERY) {
            webSocketService.notifyDeliveryReadyOrder(updatedOrder);
        }
        
        return updatedOrder;
    }
    
    public Order updateOrder(Long id, Order orderDetails) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setCustomerDetails(orderDetails.getCustomerDetails());
        order.setOrderType(orderDetails.getOrderType());
        
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        // Only allow deletion of pending or cancelled orders
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot delete order with status: " + order.getStatus());
        }
        
        orderRepository.delete(order);
    }
    
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }
    
    public Long getTodaysOrderCount() {
        return orderRepository.countTodaysOrders();
    }
    
    public BigDecimal getTodaysRevenue() {
        BigDecimal revenue = orderRepository.getTodaysRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != Order.OrderStatus.CONFIRMED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != Order.OrderStatus.PREPARING && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from CONFIRMED to " + newStatus);
                }
                break;
            case PREPARING:
                if (newStatus != Order.OrderStatus.READY_FOR_PICKUP && 
                    newStatus != Order.OrderStatus.READY_FOR_DELIVERY && 
                    newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from PREPARING to " + newStatus);
                }
                break;
            case READY_FOR_PICKUP:
                if (newStatus != Order.OrderStatus.COMPLETED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from READY_FOR_PICKUP to " + newStatus);
                }
                break;
            case READY_FOR_DELIVERY:
                if (newStatus != Order.OrderStatus.OUT_FOR_DELIVERY && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from READY_FOR_DELIVERY to " + newStatus);
                }
                break;
            case OUT_FOR_DELIVERY:
                if (newStatus != Order.OrderStatus.COMPLETED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from OUT_FOR_DELIVERY to " + newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new RuntimeException("Cannot change status from " + currentStatus);
            default:
                throw new RuntimeException("Unknown status: " + currentStatus);
        }
    }
    
    // New paginated methods
    public Page<Order> getAllOrdersPaginated(Pageable pageable, Order.OrderStatus status, Order.OrderType orderType, 
                                           LocalDateTime from, LocalDateTime to) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        
        if (status != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("status"), status));
        }
        
        if (orderType != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("orderType"), orderType));
        }
        
        if (from != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        
        if (to != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        
        return orderRepository.findAll(spec, pageable);
    }
    
    public Page<Order> getOrdersByStatusPaginated(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    public Page<Order> getOrdersByDateRangePaginated(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }
    
    // DTO-based methods
    public Order createOrderFromDTO(CreateOrderRequestDTO createOrderRequest) {
        Order order = EntityMapper.toOrderEntity(createOrderRequest);
        
        // Validate and calculate total price
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CreateOrderItemRequestDTO itemDTO : createOrderRequest.getOrderItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDTO.getProductId()));
            
            if (!product.getIsAvailable()) {
                throw new RuntimeException("Product '" + product.getName() + "' is not available");
            }
            
            OrderItem orderItem = EntityMapper.toOrderItemEntity(itemDTO, order, product);
            orderItem.setPrice(product.getPrice());
            totalPrice = totalPrice.add(orderItem.getTotalPrice());
            orderItems.add(orderItem);
        }
        
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        // Save order first
        Order savedOrder = orderRepository.save(order);
        
        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }
        
        return savedOrder;
     }
     
     public Order updateOrderFromDTO(Long id, CreateOrderRequestDTO orderDetails) {
         Order order = orderRepository.findById(id)
             .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
         
         order.setCustomerDetails(orderDetails.getCustomerDetails());
         order.setOrderType(orderDetails.getOrderType());
         
         Order updatedOrder = orderRepository.save(order);
         
         // Send WebSocket notification for order update
         webSocketService.notifyOrderUpdated(updatedOrder);
         
         return updatedOrder;
     }
}