package com.resadmin.res.controller;

import com.resadmin.res.dto.websocket.WebSocketMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle messages sent to /app/message
     * Broadcast to all subscribers of /topic/messages
     */
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public WebSocketMessageDTO handleMessage(@Payload WebSocketMessageDTO message, Principal principal) {
        message.setTimestamp(LocalDateTime.now());
        if (principal != null) {
            message.setUserId(principal.getName());
        }
        return message;
    }

    /**
     * Handle private messages sent to /app/private
     * Send to specific user's queue
     */
    @MessageMapping("/private")
    public void handlePrivateMessage(@Payload WebSocketMessageDTO message, Principal principal) {
        message.setTimestamp(LocalDateTime.now());
        if (principal != null) {
            message.setUserId(principal.getName());
        }
        
        // Send to specific user's private queue
        messagingTemplate.convertAndSendToUser(
            message.getUserId(), 
            "/queue/private", 
            message
        );
    }

    /**
     * Handle order updates sent to /app/order
     * Broadcast to all subscribers of /topic/orders
     */
    @MessageMapping("/order")
    @SendTo("/topic/orders")
    public WebSocketMessageDTO handleOrderUpdate(@Payload WebSocketMessageDTO message, Principal principal) {
        message.setTimestamp(LocalDateTime.now());
        if (principal != null) {
            message.setUserId(principal.getName());
        }
        return message;
    }

    /**
     * Handle delivery updates sent to /app/delivery
     * Broadcast to all subscribers of /topic/deliveries
     */
    @MessageMapping("/delivery")
    @SendTo("/topic/deliveries")
    public WebSocketMessageDTO handleDeliveryUpdate(@Payload WebSocketMessageDTO message, Principal principal) {
        message.setTimestamp(LocalDateTime.now());
        if (principal != null) {
            message.setUserId(principal.getName());
        }
        return message;
    }

    /**
     * Send notification to all connected clients
     */
    public void sendGlobalNotification(String type, String message, Object data) {
        WebSocketMessageDTO notification = WebSocketMessageDTO.builder()
                .type(type)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Send notification to specific user
     */
    public void sendUserNotification(String userId, String type, String message, Object data) {
        WebSocketMessageDTO notification = WebSocketMessageDTO.builder()
                .type(type)
                .message(message)
                .data(data)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * Broadcast order status change to all clients
     */
    public void broadcastOrderStatusChange(Long orderId, String status, Object orderData) {
        WebSocketMessageDTO message = WebSocketMessageDTO.builder()
                .type(WebSocketMessageDTO.MessageType.ORDER_STATUS_CHANGED.name())
                .message("Order #" + orderId + " status changed to " + status)
                .data(orderData)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/orders", message);
    }

    /**
     * Broadcast delivery status change to all clients
     */
    public void broadcastDeliveryStatusChange(Long deliveryId, String status, Object deliveryData) {
        WebSocketMessageDTO message = WebSocketMessageDTO.builder()
                .type(WebSocketMessageDTO.MessageType.DELIVERY_STATUS_UPDATED.name())
                .message("Delivery #" + deliveryId + " status changed to " + status)
                .data(deliveryData)
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/deliveries", message);
    }
}