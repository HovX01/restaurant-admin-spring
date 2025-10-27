package com.resadmin.res.mapper;

import com.resadmin.res.dto.*;
import com.resadmin.res.dto.request.CreateOrderItemRequestDTO;
import com.resadmin.res.dto.request.CreateOrderRequestDTO;
import com.resadmin.res.entity.*;
import org.springframework.data.domain.Page;
import com.resadmin.res.dto.response.PagedResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class EntityMapper {
    
    // Order mappings
    public static OrderDTO toOrderDTO(Order order) {
        if (order == null) return null;
        
        String customerDetails = order.getCustomerDetails();
        String customerName = extractCustomerField(customerDetails, "Name");
        String customerPhone = extractCustomerField(customerDetails, "Phone");
        String customerAddress = extractCustomerField(customerDetails, "Address");
        String notes = extractCustomerField(customerDetails, "Notes");
        
        return OrderDTO.builder()
                .id(order.getId())
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerAddress(customerAddress)
                .notes(notes)
                .customerDetails(customerDetails)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .orderType(order.getOrderType())
                .createdAt(order.getCreatedAt())
                .orderItems(order.getOrderItems() != null ? 
                    order.getOrderItems().stream().map(EntityMapper::toOrderItemDTO).collect(Collectors.toList()) : null)
                .delivery(order.getDelivery() != null ? toDeliveryDTO(order.getDelivery()) : null)
                .build();
    }
    
    private static String extractCustomerField(String customerDetails, String fieldName) {
        if (customerDetails == null || customerDetails.isEmpty()) {
            return null;
        }
        
        // Check if it's in the new format (with labels like "Name: ")
        String pattern = fieldName + ": ";
        int startIndex = customerDetails.indexOf(pattern);
        if (startIndex == -1) {
            // Legacy format fallback: handle old comma-separated format
            return handleLegacyFormat(customerDetails, fieldName);
        }
        
        startIndex += pattern.length();
        int endIndex = customerDetails.indexOf(" | ", startIndex);
        
        if (endIndex == -1) {
            return customerDetails.substring(startIndex).trim();
        }
        
        return customerDetails.substring(startIndex, endIndex).trim();
    }
    
    private static String handleLegacyFormat(String customerDetails, String fieldName) {
        // Legacy format: "John Doe, 555-1234, john@email.com"
        // Only extract name and phone from this format
        if (!customerDetails.contains(",")) {
            return null;
        }
        
        String[] parts = customerDetails.split(",");
        if (fieldName.equals("Name") && parts.length > 0) {
            return parts[0].trim();
        } else if (fieldName.equals("Phone") && parts.length > 1) {
            return parts[1].trim();
        }
        
        return null;
    }
    
    public static Order toOrderEntity(CreateOrderRequestDTO dto) {
        if (dto == null) return null;
        
        Order order = new Order();
        order.setCustomerDetails(dto.getCustomerDetails());
        order.setTotalPrice(dto.getTotalPrice());
        order.setOrderType(dto.getOrderType());
        return order;
    }
    
    // OrderItem mappings
    public static OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) return null;
        
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .orderId(orderItem.getOrder() != null ? orderItem.getOrder().getId() : null)
                .product(orderItem.getProduct() != null ? toProductDTO(orderItem.getProduct()) : null)
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
    
    public static OrderItem toOrderItemEntity(CreateOrderItemRequestDTO dto, Order order, Product product) {
        if (dto == null) return null;
        
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setPrice(dto.getPrice());
        return orderItem;
    }
    
    // Product mappings
    public static ProductDTO toProductDTO(Product product) {
        if (product == null) return null;
        
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .available(product.getIsAvailable())
                .category(product.getCategory() != null ? toCategoryDTO(product.getCategory()) : null)
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    // Category mappings
    public static CategoryDTO toCategoryDTO(Category category) {
        if (category == null) return null;
        
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
    
    // Delivery mappings
    public static DeliveryDTO toDeliveryDTO(Delivery delivery) {
        if (delivery == null) return null;
        
        return DeliveryDTO.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrder() != null ? delivery.getOrder().getId() : null)
                .driver(delivery.getDriver() != null ? toUserDTO(delivery.getDriver()) : null)
                .status(delivery.getStatus())
                .dispatchedAt(delivery.getDispatchedAt())
                .deliveredAt(delivery.getDeliveredAt())
                .deliveryAddress(delivery.getDeliveryAddress())
                .deliveryNotes(delivery.getDeliveryNotes())
                .build();
    }
    
    // User mappings
    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    // List mappings
    public static List<OrderDTO> toOrderDTOList(List<Order> orders) {
        return orders != null ? orders.stream().map(EntityMapper::toOrderDTO).collect(Collectors.toList()) : null;
    }
    
    public static List<ProductDTO> toProductDTOList(List<Product> products) {
        return products != null ? products.stream().map(EntityMapper::toProductDTO).collect(Collectors.toList()) : null;
    }
    
    public static List<CategoryDTO> toCategoryDTOList(List<Category> categories) {
        return categories != null ? categories.stream().map(EntityMapper::toCategoryDTO).collect(Collectors.toList()) : null;
    }
    
    public static List<DeliveryDTO> toDeliveryDTOList(List<Delivery> deliveries) {
        return deliveries != null ? deliveries.stream().map(EntityMapper::toDeliveryDTO).collect(Collectors.toList()) : null;
    }
    
    public static List<UserDTO> toUserDTOList(List<User> users) {
        return users != null ? users.stream().map(EntityMapper::toUserDTO).collect(Collectors.toList()) : null;
    }
    
    // Paged response mapping
    public static <T, D> PagedResponseDTO<D> toPagedResponseDTO(Page<T> page, List<D> dtoList) {
        return PagedResponseDTO.<D>builder()
                .content(dtoList)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}