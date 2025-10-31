# WebSocket Real-time Communication Guide

Complete guide to WebSocket/STOMP integration in the Restaurant Administration System.

## Table of Contents

1. [Overview](#overview)
2. [WebSocket Configuration](#websocket-configuration)
3. [Connection Setup](#connection-setup)
4. [Topics and Destinations](#topics-and-destinations)
5. [Message Format](#message-format)
6. [Client Implementation](#client-implementation)
7. [Server Implementation](#server-implementation)
8. [Use Cases](#use-cases)
9. [Testing](#testing)
10. [Troubleshooting](#troubleshooting)

## Overview

The Restaurant Administration System uses WebSocket with STOMP (Simple Text Oriented Messaging Protocol) for real-time bidirectional communication between server and clients.

### Why WebSocket?

**Traditional HTTP** (Request-Response):
```
Client ──── Request ────▶ Server
Client ◀─── Response ─── Server
```

**WebSocket** (Full-Duplex):
```
Client ◀──── Messages ────▶ Server
       (bidirectional, persistent connection)
```

### Benefits

- **Real-time Updates**: Instant notifications without polling
- **Low Latency**: Persistent connection eliminates handshake overhead
- **Bidirectional**: Server can push updates to clients
- **Efficient**: Less bandwidth than HTTP polling
- **STOMP Protocol**: Structured message format with routing

### Use Cases in Our System

1. **Order Notifications**: Kitchen staff receive new order alerts
2. **Status Updates**: Real-time order status changes
3. **Delivery Assignments**: Notify drivers of new deliveries
4. **Kitchen Displays**: Live order queue updates
5. **System Alerts**: Broadcast important messages

## WebSocket Configuration

### Server Configuration

**WebSocketConfig.java**:

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker
        config.enableSimpleBroker("/topic", "/queue");

        // Application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");

        // User destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Configure for production
                .withSockJS();  // SockJS fallback for older browsers
    }
}
```

### Configuration Breakdown

| Setting | Value | Purpose |
|---------|-------|---------|
| **Endpoint** | `/ws` | WebSocket connection URL |
| **Broker Prefixes** | `/topic`, `/queue` | Message destination prefixes |
| **App Prefix** | `/app` | Client-to-server message prefix |
| **User Prefix** | `/user` | User-specific message prefix |
| **SockJS** | Enabled | Fallback for older browsers |

## Connection Setup

### Connection URL

```
ws://localhost:8080/ws
```

For SockJS:
```
http://localhost:8080/ws
```

### Connection Flow

```
1. Client initiates WebSocket handshake
   GET /ws HTTP/1.1
   Upgrade: websocket
   Connection: Upgrade

2. Server accepts and upgrades connection
   HTTP/1.1 101 Switching Protocols
   Upgrade: websocket
   Connection: Upgrade

3. STOMP CONNECT frame
   CONNECT
   accept-version:1.1,1.2
   heart-beat:10000,10000

4. Server CONNECTED frame
   CONNECTED
   version:1.2
   heart-beat:10000,10000

5. Client SUBSCRIBE to topics
   SUBSCRIBE
   id:sub-0
   destination:/topic/orders

6. Server sends MESSAGESUBSCRIBE frames
   MESSAGE
   destination:/topic/orders
   content-type:application/json
   {message payload}
```

## Topics and Destinations

### Broadcast Topics

**1. /topic/notifications**
- **Purpose**: Global notifications to all connected clients
- **Subscribers**: All roles
- **Messages**: All notification types

**2. /topic/orders**
- **Purpose**: Order-related updates
- **Subscribers**: ADMIN, MANAGER, KITCHEN_STAFF
- **Messages**: ORDER_CREATED, ORDER_UPDATED, ORDER_STATUS_CHANGED

**3. /topic/deliveries**
- **Purpose**: Delivery-related updates
- **Subscribers**: ADMIN, MANAGER, DELIVERY_STAFF
- **Messages**: DELIVERY_ASSIGNED, DELIVERY_STATUS_UPDATED

**4. /topic/kitchen**
- **Purpose**: Kitchen-specific notifications
- **Subscribers**: KITCHEN_STAFF, ADMIN, MANAGER
- **Messages**: KITCHEN_NEW_ORDER

**5. /topic/delivery-staff**
- **Purpose**: Delivery staff notifications
- **Subscribers**: DELIVERY_STAFF, ADMIN, MANAGER
- **Messages**: DELIVERY_READY_ORDER, DELIVERY_STAFF_NEW_ASSIGNMENT

**6. /topic/system**
- **Purpose**: System-wide alerts and announcements
- **Subscribers**: All roles
- **Messages**: SYSTEM_ALERT

### User-Specific Queues

**Pattern**: `/user/{username}/queue/notifications`

**Purpose**: Send private notifications to specific user

**Example**:
```
/user/driver1/queue/notifications
/user/admin/queue/notifications
```

### Application Destinations (Client to Server)

**Pattern**: `/app/{destination}`

| Destination | Purpose | Handler |
|-------------|---------|---------|
| `/app/message` | Send general message | → `/topic/messages` |
| `/app/order` | Send order-related message | → `/topic/orders` |
| `/app/delivery` | Send delivery message | → `/topic/deliveries` |
| `/app/private` | Send private message | → `/user/{userId}/queue/private` |

## Message Format

### WebSocketMessageDTO

All WebSocket messages follow this structure:

```java
public class WebSocketMessageDTO {
    private MessageType type;
    private String message;
    private Object data;
    private String userId;
    private LocalDateTime timestamp;
}
```

### Message Types

```java
public enum MessageType {
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_STATUS_CHANGED,
    DELIVERY_ASSIGNED,
    DELIVERY_STATUS_UPDATED,
    KITCHEN_NEW_ORDER,
    DELIVERY_READY_ORDER,
    DELIVERY_STAFF_NEW_ASSIGNMENT,
    SYSTEM_ALERT,
    USER_NOTIFICATION
}
```

### Example Messages

**Order Created**:
```json
{
  "type": "ORDER_CREATED",
  "message": "New order #123 received",
  "data": {
    "id": 123,
    "customerDetails": {
      "name": "John Doe",
      "phone": "+1234567890"
    },
    "orderType": "DELIVERY",
    "status": "PENDING",
    "totalPrice": 45.99,
    "items": [...]
  },
  "userId": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

**Order Status Changed**:
```json
{
  "type": "ORDER_STATUS_CHANGED",
  "message": "Order #123 is now PREPARING",
  "data": {
    "orderId": 123,
    "oldStatus": "CONFIRMED",
    "newStatus": "PREPARING"
  },
  "userId": null,
  "timestamp": "2024-01-15T10:35:00"
}
```

**Delivery Assigned**:
```json
{
  "type": "DELIVERY_ASSIGNED",
  "message": "You have been assigned delivery #45",
  "data": {
    "id": 45,
    "orderId": 123,
    "deliveryAddress": "123 Main St",
    "status": "ASSIGNED"
  },
  "userId": "driver1",
  "timestamp": "2024-01-15T10:40:00"
}
```

**System Alert**:
```json
{
  "type": "SYSTEM_ALERT",
  "message": "System will be under maintenance in 30 minutes",
  "data": {
    "severity": "WARNING",
    "scheduledTime": "2024-01-15T23:00:00"
  },
  "userId": null,
  "timestamp": "2024-01-15T22:30:00"
}
```

## Client Implementation

### JavaScript/TypeScript (SockJS + STOMP)

**Install Dependencies**:
```bash
npm install sockjs-client @stomp/stompjs
```

**Connection Setup**:
```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
  }

  connect() {
    // Create SockJS connection
    const socket = new SockJS('http://localhost:8080/ws');

    // Create STOMP client
    this.client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('STOMP:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: (frame) => {
        console.log('Connected:', frame);
        this.connected = true;
        this.subscribeToTopics();
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },

      onDisconnect: () => {
        console.log('Disconnected');
        this.connected = false;
      }
    });

    // Activate connection
    this.client.activate();
  }

  subscribeToTopics() {
    // Subscribe to order notifications
    this.client.subscribe('/topic/orders', (message) => {
      const data = JSON.parse(message.body);
      console.log('Order notification:', data);
      this.handleOrderNotification(data);
    });

    // Subscribe to delivery notifications
    this.client.subscribe('/topic/deliveries', (message) => {
      const data = JSON.parse(message.body);
      console.log('Delivery notification:', data);
      this.handleDeliveryNotification(data);
    });

    // Subscribe to kitchen notifications (if kitchen staff)
    this.client.subscribe('/topic/kitchen', (message) => {
      const data = JSON.parse(message.body);
      console.log('Kitchen notification:', data);
      this.handleKitchenNotification(data);
    });

    // Subscribe to user-specific notifications
    const username = this.getCurrentUsername();
    this.client.subscribe(`/user/${username}/queue/notifications`, (message) => {
      const data = JSON.parse(message.body);
      console.log('Private notification:', data);
      this.handlePrivateNotification(data);
    });
  }

  sendMessage(destination, message) {
    if (this.connected) {
      this.client.publish({
        destination: `/app/${destination}`,
        body: JSON.stringify(message)
      });
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }

  handleOrderNotification(data) {
    switch (data.type) {
      case 'ORDER_CREATED':
        // Show notification
        this.showNotification('New Order', data.message);
        // Update UI
        this.updateOrderList(data.data);
        break;
      case 'ORDER_STATUS_CHANGED':
        // Update specific order in UI
        this.updateOrderStatus(data.data);
        break;
    }
  }

  handleDeliveryNotification(data) {
    // Handle delivery notifications
  }

  handleKitchenNotification(data) {
    // Handle kitchen notifications
  }

  handlePrivateNotification(data) {
    // Handle private notifications
  }

  showNotification(title, message) {
    // Browser notification API
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(title, { body: message });
    }
  }

  getCurrentUsername() {
    // Get from authentication context
    return localStorage.getItem('username');
  }
}

// Usage
const wsService = new WebSocketService();
wsService.connect();

// Send message
wsService.sendMessage('order', {
  type: 'ORDER_REQUEST',
  orderId: 123
});

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
  wsService.disconnect();
});
```

### React Hook Example

```javascript
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useWebSocket = (topics = []) => {
  const [client, setClient] = useState(null);
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        setConnected(true);

        // Subscribe to topics
        topics.forEach(topic => {
          stompClient.subscribe(topic, (message) => {
            const data = JSON.parse(message.body);
            setMessages(prev => [...prev, data]);
          });
        });
      },
      onDisconnect: () => setConnected(false)
    });

    stompClient.activate();
    setClient(stompClient);

    return () => {
      stompClient.deactivate();
    };
  }, []);

  const sendMessage = (destination, message) => {
    if (connected && client) {
      client.publish({
        destination: `/app/${destination}`,
        body: JSON.stringify(message)
      });
    }
  };

  return { connected, messages, sendMessage };
};

// Usage in component
function OrderDashboard() {
  const { connected, messages, sendMessage } = useWebSocket([
    '/topic/orders',
    '/topic/kitchen'
  ]);

  return (
    <div>
      <p>Status: {connected ? 'Connected' : 'Disconnected'}</p>
      <ul>
        {messages.map((msg, i) => (
          <li key={i}>{msg.message}</li>
        ))}
      </ul>
    </div>
  );
}
```

### Angular Example

```typescript
import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client;
  private connected$ = new BehaviorSubject<boolean>(false);
  private messages$ = new BehaviorSubject<any>(null);

  constructor() {
    this.initializeWebSocketConnection();
  }

  initializeWebSocketConnection() {
    const socket = new SockJS('http://localhost:8080/ws');

    this.client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        this.connected$.next(true);
        this.subscribeToTopics();
      },
      onDisconnect: () => this.connected$.next(false)
    });

    this.client.activate();
  }

  subscribeToTopics() {
    this.client.subscribe('/topic/orders', (message) => {
      this.messages$.next(JSON.parse(message.body));
    });
  }

  sendMessage(destination: string, message: any) {
    this.client.publish({
      destination: `/app/${destination}`,
      body: JSON.stringify(message)
    });
  }

  getMessages(): Observable<any> {
    return this.messages$.asObservable();
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }
}
```

## Server Implementation

### WebSocketService.java

```java
@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Send to specific topic
    public void sendOrderNotification(Order order) {
        WebSocketMessageDTO message = WebSocketMessageDTO.builder()
            .type(MessageType.ORDER_CREATED)
            .message("New order #" + order.getId() + " received")
            .data(EntityMapper.toOrderDTO(order))
            .timestamp(LocalDateTime.now())
            .build();

        messagingTemplate.convertAndSend("/topic/orders", message);
    }

    // Send to kitchen
    public void sendKitchenNotification(Order order) {
        WebSocketMessageDTO message = WebSocketMessageDTO.builder()
            .type(MessageType.KITCHEN_NEW_ORDER)
            .message("New order for kitchen: #" + order.getId())
            .data(EntityMapper.toOrderDTO(order))
            .timestamp(LocalDateTime.now())
            .build();

        messagingTemplate.convertAndSend("/topic/kitchen", message);
    }

    // Send to specific user
    public void sendUserNotification(String username, String notificationMessage) {
        WebSocketMessageDTO message = WebSocketMessageDTO.builder()
            .type(MessageType.USER_NOTIFICATION)
            .message(notificationMessage)
            .userId(username)
            .timestamp(LocalDateTime.now())
            .build();

        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            message
        );
    }

    // Broadcast system alert
    public void broadcastSystemAlert(String alertMessage) {
        WebSocketMessageDTO message = WebSocketMessageDTO.builder()
            .type(MessageType.SYSTEM_ALERT)
            .message(alertMessage)
            .timestamp(LocalDateTime.now())
            .build();

        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}
```

### WebSocketController.java

```java
@Controller
public class WebSocketController {

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public WebSocketMessageDTO handleMessage(WebSocketMessageDTO message) {
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    @MessageMapping("/order")
    @SendTo("/topic/orders")
    public WebSocketMessageDTO handleOrderMessage(WebSocketMessageDTO message) {
        // Process order message
        return message;
    }

    @MessageMapping("/private")
    public void handlePrivateMessage(
            WebSocketMessageDTO message,
            Principal principal) {
        // Send to specific user
        messagingTemplate.convertAndSendToUser(
            message.getUserId(),
            "/queue/private",
            message
        );
    }
}
```

### Integration in Services

**OrderService.java**:
```java
@Service
public class OrderService {

    @Autowired
    private WebSocketService webSocketService;

    @Transactional
    public ApiResponseDTO<OrderDTO> createOrder(CreateOrderRequestDTO request) {
        Order order = new Order();
        // ... create order logic
        Order savedOrder = orderRepository.save(order);

        // Send WebSocket notification
        webSocketService.sendOrderNotification(savedOrder);
        webSocketService.sendKitchenNotification(savedOrder);

        return ApiResponseDTO.success(EntityMapper.toOrderDTO(savedOrder));
    }

    @Transactional
    public ApiResponseDTO<OrderDTO> updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // Send status change notification
        webSocketService.sendOrderStatusChanged(updatedOrder, oldStatus, newStatus);

        return ApiResponseDTO.success(EntityMapper.toOrderDTO(updatedOrder));
    }
}
```

## Use Cases

### 1. Kitchen Display System

**Scenario**: Kitchen staff see new orders in real-time

**Flow**:
```
1. Customer places order → POST /api/orders
2. OrderService creates order in database
3. OrderService calls webSocketService.sendKitchenNotification()
4. WebSocket broadcasts to /topic/kitchen
5. Kitchen display receives notification
6. Kitchen display updates order queue
```

### 2. Delivery Driver Assignment

**Scenario**: Driver receives notification when assigned delivery

**Flow**:
```
1. Manager assigns delivery → POST /api/deliveries/assign
2. DeliveryService creates delivery record
3. DeliveryService sends two notifications:
   - Broadcast to /topic/deliveries (all managers/admin)
   - User-specific to /user/driver1/queue/notifications
4. Driver's app receives notification
5. Driver sees new delivery in their queue
```

### 3. Order Status Tracking

**Scenario**: Customer app shows live order status

**Flow**:
```
1. Customer subscribes to /topic/orders (filtered by their order)
2. Kitchen updates status → PATCH /api/orders/{id}/status
3. WebSocket broadcasts ORDER_STATUS_CHANGED
4. Customer app receives notification
5. Customer app updates progress bar
```

## Testing

### Manual Testing with Browser Console

```javascript
// Open browser console on http://localhost:8080

// 1. Load libraries (if not already loaded)
const script1 = document.createElement('script');
script1.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
document.head.appendChild(script1);

const script2 = document.createElement('script');
script2.src = 'https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js';
document.head.appendChild(script2);

// 2. Wait for scripts to load, then connect
setTimeout(() => {
  const socket = new SockJS('http://localhost:8080/ws');
  const client = Stomp.over(socket);

  client.connect({}, (frame) => {
    console.log('Connected:', frame);

    // Subscribe to orders
    client.subscribe('/topic/orders', (message) => {
      console.log('Received:', JSON.parse(message.body));
    });
  });

  window.stompClient = client;
}, 2000);

// 3. Send test message
stompClient.send('/app/order', {}, JSON.stringify({
  type: 'TEST',
  message: 'Test message'
}));
```

### Testing Tools

**Postman**:
1. Create new WebSocket request
2. URL: `ws://localhost:8080/ws`
3. Connect
4. Send STOMP frames

**wscat** (command line):
```bash
npm install -g wscat
wscat -c ws://localhost:8080/ws
```

## Troubleshooting

### Connection Issues

**Problem**: Cannot connect to WebSocket

**Solutions**:
1. Check if server is running
2. Verify endpoint URL: `ws://localhost:8080/ws`
3. Check CORS configuration
4. Try SockJS fallback: `http://localhost:8080/ws`
5. Check browser console for errors

### Message Not Received

**Problem**: Subscribed but not receiving messages

**Solutions**:
1. Verify subscription destination matches server send destination
2. Check if message is actually being sent from server
3. Enable STOMP debug logging
4. Check server logs for WebSocket errors

### Disconnection Issues

**Problem**: Frequent disconnections

**Solutions**:
1. Implement heartbeat mechanism
2. Add reconnection logic
3. Check network stability
4. Increase timeout values

### Production Considerations

**1. Load Balancing**:
- Use sticky sessions
- Or use Redis for message broker (instead of in-memory)

**2. Scalability**:
```java
// Replace in-memory broker with RabbitMQ or Redis
config.enableStompBrokerRelay("/topic", "/queue")
    .setRelayHost("localhost")
    .setRelayPort(61613);
```

**3. Security**:
- Authenticate WebSocket connections
- Authorize topic subscriptions
- Validate message content

**4. Monitoring**:
- Track active connections
- Monitor message rates
- Log errors and reconnections

---

## Additional Resources

- [STOMP Protocol Specification](https://stomp.github.io/)
- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [SockJS Documentation](https://github.com/sockjs/sockjs-client)
- [@stomp/stompjs Documentation](https://stomp-js.github.io/stomp-js/)
