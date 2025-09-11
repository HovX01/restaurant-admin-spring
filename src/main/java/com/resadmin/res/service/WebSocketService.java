package com.resadmin.res.service;

import com.resadmin.res.controller.WebSocketController;
import com.resadmin.res.dto.DeliveryDTO;
import com.resadmin.res.dto.OrderDTO;
import com.resadmin.res.dto.websocket.WebSocketMessageDTO;
import com.resadmin.res.entity.Delivery;
import com.resadmin.res.entity.Order;
import com.resadmin.res.mapper.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private WebSocketController webSocketController;

    /**
     * Notify all clients about a new order creation
     */
    public void notifyOrderCreated(Order order) {
        OrderDTO orderDTO = EntityMapper.toOrderDTO(order);
        webSocketController.sendGlobalNotification(
            WebSocketMessageDTO.MessageType.ORDER_CREATED.name(),
            "New order #" + order.getId() + " has been created",
            orderDTO
        );
    }

    /**
     * Notify all clients about order status change
     */
    public void notifyOrderStatusChanged(Order order) {
        OrderDTO orderDTO = EntityMapper.toOrderDTO(order);
        webSocketController.broadcastOrderStatusChange(
            order.getId(),
            order.getStatus().name(),
            orderDTO
        );
    }

    /**
     * Notify all clients about order updates
     */
    public void notifyOrderUpdated(Order order) {
        OrderDTO orderDTO = EntityMapper.toOrderDTO(order);
        webSocketController.sendGlobalNotification(
            WebSocketMessageDTO.MessageType.ORDER_UPDATED.name(),
            "Order #" + order.getId() + " has been updated",
            orderDTO
        );
    }

    /**
     * Notify all clients about delivery assignment
     */
    public void notifyDeliveryAssigned(Delivery delivery) {
        DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(delivery);
        webSocketController.sendGlobalNotification(
            WebSocketMessageDTO.MessageType.DELIVERY_ASSIGNED.name(),
            "Delivery #" + delivery.getId() + " has been assigned to driver",
            deliveryDTO
        );
    }

    /**
     * Notify all clients about delivery status change
     */
    public void notifyDeliveryStatusChanged(Delivery delivery) {
        DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(delivery);
        webSocketController.broadcastDeliveryStatusChange(
            delivery.getId(),
            delivery.getStatus().name(),
            deliveryDTO
        );
    }

    /**
     * Send notification to specific user
     */
    public void notifyUser(String userId, String message, Object data) {
        webSocketController.sendUserNotification(
            userId,
            WebSocketMessageDTO.MessageType.USER_NOTIFICATION.name(),
            message,
            data
        );
    }

    /**
     * Send system alert to all clients
     */
    public void sendSystemAlert(String message, Object data) {
        webSocketController.sendGlobalNotification(
            WebSocketMessageDTO.MessageType.SYSTEM_ALERT.name(),
            message,
            data
        );
    }

    /**
     * Notify kitchen staff about new orders
     */
    public void notifyKitchenNewOrder(Order order) {
        OrderDTO orderDTO = EntityMapper.toOrderDTO(order);
        webSocketController.sendGlobalNotification(
            "KITCHEN_NEW_ORDER",
            "New order #" + order.getId() + " for kitchen preparation",
            orderDTO
        );
    }

    /**
     * Notify delivery staff about orders ready for delivery
     */
    public void notifyDeliveryReadyOrder(Order order) {
        OrderDTO orderDTO = EntityMapper.toOrderDTO(order);
        webSocketController.sendGlobalNotification(
            "DELIVERY_READY_ORDER",
            "Order #" + order.getId() + " is ready for delivery",
            orderDTO
        );
    }

    /**
     * Notify delivery staff about new assignment
     */
    public void notifyDeliveryStaffNewAssignment(Delivery delivery) {
        DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(delivery);
        webSocketController.sendGlobalNotification(
            "DELIVERY_STAFF_NEW_ASSIGNMENT",
            "New delivery assignment #" + delivery.getId(),
            deliveryDTO
        );
    }
}