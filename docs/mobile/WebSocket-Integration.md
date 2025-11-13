# WebSocket Integration Guide

Complete guide to implementing real-time features using WebSocket with STOMP protocol.

## Table of Contents

1. [Overview](#overview)
2. [Connection Setup](#connection-setup)
3. [Topics and Subscriptions](#topics-and-subscriptions)
4. [Message Formats](#message-formats)
5. [Platform Implementation](#platform-implementation)
6. [Connection Management](#connection-management)
7. [Error Handling](#error-handling)
8. [Best Practices](#best-practices)
9. [Code Examples](#code-examples)

---

## Overview

The Restaurant Admin System uses **WebSocket** with **STOMP** (Simple Text Oriented Messaging Protocol) for real-time bidirectional communication.

### WebSocket Details

- **Protocol**: STOMP over SockJS
- **Endpoint**: `ws://localhost:8080/ws` (development)
- **Production**: `wss://your-domain.com/ws`
- **Fallback**: SockJS provides fallback options if WebSocket is unavailable
- **Authentication**: Optional (JWT can be passed via query parameter or headers)

### Use Cases

**Real-time Order Updates**:
- New orders created → Kitchen staff notified
- Order status changes → All subscribers notified
- Order ready for pickup/delivery → Relevant staff notified

**Delivery Tracking**:
- Delivery assigned → Driver notified
- Status updates → Manager dashboard updates
- Driver location updates (if implemented)

**System Notifications**:
- Broadcast alerts to all users
- Role-specific notifications
- User-specific messages

---

## Connection Setup

### Connection Endpoints

**Primary WebSocket Endpoint**:
```
ws://localhost:8080/ws
```

**SockJS Endpoint** (with fallback):
```
http://localhost:8080/ws
```

**Production** (HTTPS/WSS):
```
wss://your-domain.com/ws
```

### Connection Flow

```
┌────────────────────────────────────────────────────────────┐
│                    App Starts / User Logs In               │
└─────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
                ┌──────────────────────┐
                │  Initialize WebSocket│
                │  Client              │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │  Connect to          │
                │  ws://host:port/ws   │
                └──────────┬───────────┘
                           │
              ┌────────────┴────────────┐
              │                         │
          Success                    Failure
              │                         │
              ▼                         ▼
     ┌─────────────────┐      ┌─────────────────┐
     │ STOMP CONNECTED │      │  Connection     │
     │                 │      │  Failed         │
     └────────┬────────┘      └────────┬────────┘
              │                        │
              ▼                        ▼
     ┌─────────────────┐      ┌─────────────────┐
     │  Subscribe to   │      │  Retry with     │
     │  Topics         │      │  Exponential    │
     │  - /topic/orders│      │  Backoff        │
     │  - /topic/...   │      └─────────────────┘
     └────────┬────────┘
              │
              ▼
     ┌─────────────────┐
     │  Listen for     │
     │  Messages       │
     └────────┬────────┘
              │
              ▼
     ┌─────────────────┐
     │  Handle         │
     │  Incoming       │
     │  Messages       │
     └─────────────────┘
```

### Initial Connection

**Steps**:
1. Create WebSocket client instance
2. Configure connection URL
3. Set up connection callbacks (connected, error, disconnected)
4. Connect to server
5. Wait for CONNECTED frame
6. Subscribe to relevant topics
7. Start listening for messages

---

## Topics and Subscriptions

### Public Topics (Subscribe)

All authenticated users can subscribe to these topics:

#### `/topic/orders`
**Purpose**: Order-related notifications
**Subscribers**: All roles
**Messages**: Order created, updated, status changed

**Use Cases**:
- Kitchen display: New orders appear
- Manager dashboard: Order status updates
- Delivery staff: Orders ready for delivery

#### `/topic/deliveries`
**Purpose**: Delivery-related notifications
**Subscribers**: All roles
**Messages**: Delivery assigned, status updated, completed

**Use Cases**:
- Manager dashboard: Delivery tracking
- Kitchen staff: Delivery dispatched confirmation

#### `/topic/kitchen`
**Purpose**: Kitchen-specific notifications
**Subscribers**: KITCHEN_STAFF, ADMIN, MANAGER
**Messages**: Orders needing preparation, urgent requests

**Use Cases**:
- Kitchen display system
- Order preparation queue updates

#### `/topic/delivery-staff`
**Purpose**: Delivery driver notifications
**Subscribers**: DELIVERY_STAFF, ADMIN, MANAGER
**Messages**: New delivery assignments, route updates

**Use Cases**:
- Driver mobile app
- Delivery coordinator dashboard

#### `/topic/system`
**Purpose**: System-wide alerts and announcements
**Subscribers**: All roles
**Messages**: System maintenance, important announcements

**Use Cases**:
- Broadcast critical information
- System status updates

#### `/topic/notifications`
**Purpose**: Global notification feed
**Subscribers**: All roles
**Messages**: General notifications

#### `/user/{username}/queue/notifications`
**Purpose**: User-specific private messages
**Subscribers**: Individual user
**Messages**: Personal notifications, mentions

---

### Application Destinations (Send)

Use these endpoints to send messages to the server:

#### `/app/message`
**Purpose**: Send general message
**Broadcasts to**: `/topic/messages`
**Payload**: `WebSocketMessageDTO`

#### `/app/order`
**Purpose**: Send order-related message
**Broadcasts to**: `/topic/orders`
**Payload**: Order message object

#### `/app/delivery`
**Purpose**: Send delivery-related message
**Broadcasts to**: `/topic/deliveries`
**Payload**: Delivery message object

#### `/app/private`
**Purpose**: Send private message to specific user
**Sends to**: `/user/{username}/queue/notifications`
**Payload**: Private message with recipient username

---

## Message Formats

### WebSocketMessageDTO

**Structure**:
```json
{
  "type": "MESSAGE_TYPE",
  "content": "Message content or JSON string",
  "sender": "username",
  "timestamp": "2025-11-13T10:30:00"
}
```

**Message Types**:
- `ORDER_CREATED`
- `ORDER_UPDATED`
- `ORDER_STATUS_CHANGED`
- `DELIVERY_ASSIGNED`
- `DELIVERY_STATUS_UPDATED`
- `SYSTEM_ALERT`
- `USER_NOTIFICATION`

---

### Order Created Message

**Topic**: `/topic/orders`
**Type**: `ORDER_CREATED`

```json
{
  "type": "ORDER_CREATED",
  "content": "{\"orderId\":101,\"customerName\":\"John Doe\",\"orderType\":\"DELIVERY\",\"totalPrice\":45.97,\"items\":5}",
  "sender": "system",
  "timestamp": "2025-11-13T10:30:00"
}
```

**Parsed Content**:
```json
{
  "orderId": 101,
  "customerName": "John Doe",
  "orderType": "DELIVERY",
  "totalPrice": 45.97,
  "items": 5
}
```

---

### Order Status Changed Message

**Topic**: `/topic/orders`
**Type**: `ORDER_STATUS_CHANGED`

```json
{
  "type": "ORDER_STATUS_CHANGED",
  "content": "{\"orderId\":101,\"oldStatus\":\"CONFIRMED\",\"newStatus\":\"PREPARING\",\"updatedBy\":\"kitchen_user\"}",
  "sender": "kitchen_user",
  "timestamp": "2025-11-13T10:35:00"
}
```

---

### Delivery Assigned Message

**Topic**: `/topic/deliveries` and `/topic/delivery-staff`
**Type**: `DELIVERY_ASSIGNED`

```json
{
  "type": "DELIVERY_ASSIGNED",
  "content": "{\"deliveryId\":50,\"orderId\":101,\"driverId\":5,\"driverName\":\"Mike Driver\",\"address\":\"123 Main St\",\"notes\":\"Ring doorbell\"}",
  "sender": "manager",
  "timestamp": "2025-11-13T10:40:00"
}
```

**Parsed Content**:
```json
{
  "deliveryId": 50,
  "orderId": 101,
  "driverId": 5,
  "driverName": "Mike Driver",
  "address": "123 Main St",
  "notes": "Ring doorbell"
}
```

---

### Delivery Status Updated Message

**Topic**: `/topic/deliveries`
**Type**: `DELIVERY_STATUS_UPDATED`

```json
{
  "type": "DELIVERY_STATUS_UPDATED",
  "content": "{\"deliveryId\":50,\"orderId\":101,\"status\":\"OUT_FOR_DELIVERY\",\"driverId\":5}",
  "sender": "driver1",
  "timestamp": "2025-11-13T10:45:00"
}
```

---

### System Alert Message

**Topic**: `/topic/system`
**Type**: `SYSTEM_ALERT`

```json
{
  "type": "SYSTEM_ALERT",
  "content": "System maintenance scheduled for tonight at 2 AM. Estimated downtime: 30 minutes.",
  "sender": "admin",
  "timestamp": "2025-11-13T10:00:00"
}
```

---

### User Notification Message

**Topic**: `/user/{username}/queue/notifications`
**Type**: `USER_NOTIFICATION`

```json
{
  "type": "USER_NOTIFICATION",
  "content": "You have been assigned a new delivery for Order #101",
  "sender": "system",
  "timestamp": "2025-11-13T10:40:00"
}
```

---

## Platform Implementation

### iOS (Swift) - Starscream

**Library**: Starscream

**Installation** (Package.swift):
```swift
dependencies: [
    .package(url: "https://github.com/daltoniam/Starscream.git", from: "4.0.0")
]
```

**Basic Implementation**:
```swift
import Starscream

class WebSocketManager: WebSocketDelegate {
    var socket: WebSocket?
    var isConnected = false

    func connect() {
        var request = URLRequest(url: URL(string: "ws://localhost:8080/ws")!)
        request.timeoutInterval = 5
        socket = WebSocket(request: request)
        socket?.delegate = self
        socket?.connect()
    }

    func disconnect() {
        socket?.disconnect()
    }

    // Subscribe to topic
    func subscribe(to topic: String) {
        guard isConnected else { return }

        let subscribeFrame = """
        SUBSCRIBE
        id:sub-\(UUID().uuidString)
        destination:\(topic)

        \0
        """
        socket?.write(string: subscribeFrame)
    }

    // Send message
    func send(to destination: String, message: String) {
        guard isConnected else { return }

        let sendFrame = """
        SEND
        destination:\(destination)
        content-type:application/json

        \(message)\0
        """
        socket?.write(string: sendFrame)
    }

    // WebSocketDelegate methods
    func didReceive(event: WebSocketEvent, client: WebSocket) {
        switch event {
        case .connected(_):
            print("WebSocket connected")
            isConnected = true
            // Subscribe to topics after connection
            subscribeToTopics()

        case .disconnected(let reason, let code):
            print("WebSocket disconnected: \(reason) with code: \(code)")
            isConnected = false

        case .text(let string):
            print("Received text: \(string)")
            handleMessage(string)

        case .binary(let data):
            print("Received data: \(data.count)")

        case .ping(_):
            break

        case .pong(_):
            break

        case .viabilityChanged(_):
            break

        case .reconnectSuggested(_):
            connect()

        case .cancelled:
            isConnected = false

        case .error(let error):
            print("WebSocket error: \(error?.localizedDescription ?? "Unknown")")
            isConnected = false
        }
    }

    private func subscribeToTopics() {
        subscribe(to: "/topic/orders")
        subscribe(to: "/topic/notifications")
        // Subscribe to role-specific topics
    }

    private func handleMessage(_ message: String) {
        // Parse STOMP frame
        if message.contains("MESSAGE") {
            // Extract message body and process
        }
    }
}
```

---

### Android (Kotlin) - STOMP + OkHttp

**Library**: STOMP Protocol Android

**Gradle**:
```gradle
implementation 'com.github.NaikSoftware:StompProtocolAndroid:1.6.6'
implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
```

**Basic Implementation**:
```kotlin
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class WebSocketManager {
    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()

    fun connect() {
        val url = "ws://10.0.2.2:8080/ws"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

        // Lifecycle events
        compositeDisposable.add(
            stompClient!!.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {
                            Log.d("WebSocket", "Connected")
                            subscribeToTopics()
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            Log.d("WebSocket", "Disconnected")
                        }
                        LifecycleEvent.Type.ERROR -> {
                            Log.e("WebSocket", "Error: ${lifecycleEvent.exception}")
                        }
                        else -> {}
                    }
                }
        )

        stompClient!!.connect()
    }

    fun disconnect() {
        stompClient?.disconnect()
        compositeDisposable.clear()
    }

    private fun subscribeToTopics() {
        // Subscribe to orders topic
        compositeDisposable.add(
            stompClient!!.topic("/topic/orders")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stompMessage ->
                    handleOrderMessage(stompMessage.payload)
                }, { throwable ->
                    Log.e("WebSocket", "Error on subscribe topic: $throwable")
                })
        )

        // Subscribe to notifications
        compositeDisposable.add(
            stompClient!!.topic("/topic/notifications")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stompMessage ->
                    handleNotificationMessage(stompMessage.payload)
                }, { throwable ->
                    Log.e("WebSocket", "Error on subscribe topic: $throwable")
                })
        )
    }

    fun sendMessage(destination: String, message: String) {
        compositeDisposable.add(
            stompClient!!.send(destination, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("WebSocket", "Message sent successfully")
                }, { throwable ->
                    Log.e("WebSocket", "Error sending message: $throwable")
                })
        )
    }

    private fun handleOrderMessage(payload: String) {
        try {
            val message = Gson().fromJson(payload, WebSocketMessage::class.java)
            // Process order message
            Log.d("WebSocket", "Order message: ${message.type}")
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing message: $e")
        }
    }

    private fun handleNotificationMessage(payload: String) {
        try {
            val message = Gson().fromJson(payload, WebSocketMessage::class.java)
            // Process notification
            Log.d("WebSocket", "Notification: ${message.content}")
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing message: $e")
        }
    }
}

data class WebSocketMessage(
    val type: String,
    val content: String,
    val sender: String,
    val timestamp: String
)
```

---

### Flutter - web_socket_channel + stomp_dart_client

**Dependencies** (pubspec.yaml):
```yaml
dependencies:
  stomp_dart_client: ^1.0.0
```

**Basic Implementation**:
```dart
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

class WebSocketManager {
  StompClient? _stompClient;
  bool _isConnected = false;

  void connect() {
    _stompClient = StompClient(
      config: StompConfig(
        url: 'ws://localhost:8080/ws',
        onConnect: onConnect,
        onWebSocketError: (dynamic error) => print('WebSocket error: $error'),
        onStompError: (StompFrame frame) => print('Stomp error: ${frame.body}'),
        onDisconnect: (StompFrame frame) {
          print('Disconnected');
          _isConnected = false;
        },
      ),
    );

    _stompClient!.activate();
  }

  void onConnect(StompFrame frame) {
    print('Connected to WebSocket');
    _isConnected = true;

    // Subscribe to topics
    _stompClient!.subscribe(
      destination: '/topic/orders',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          handleOrderMessage(frame.body!);
        }
      },
    );

    _stompClient!.subscribe(
      destination: '/topic/notifications',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          handleNotificationMessage(frame.body!);
        }
      },
    );
  }

  void disconnect() {
    _stompClient?.deactivate();
    _isConnected = false;
  }

  void sendMessage(String destination, Map<String, dynamic> message) {
    if (_isConnected) {
      _stompClient?.send(
        destination: destination,
        body: jsonEncode(message),
      );
    }
  }

  void handleOrderMessage(String payload) {
    try {
      final message = jsonDecode(payload);
      print('Order message: ${message['type']}');
      // Process order message
    } catch (e) {
      print('Error parsing message: $e');
    }
  }

  void handleNotificationMessage(String payload) {
    try {
      final message = jsonDecode(payload);
      print('Notification: ${message['content']}');
      // Process notification
    } catch (e) {
      print('Error parsing message: $e');
    }
  }

  bool get isConnected => _isConnected;
}
```

---

### React Native - Socket.IO Client

**Installation**:
```bash
npm install socket.io-client
npm install @stomp/stompjs
npm install sockjs-client
```

**Basic Implementation**:
```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketManager {
  constructor() {
    this.client = null;
    this.isConnected = false;
  }

  connect() {
    this.client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {},
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    // Fallback to SockJS if WebSocket not available
    if (typeof WebSocket !== 'function') {
      this.client.webSocketFactory = () => {
        return new SockJS('http://localhost:8080/ws');
      };
    }

    this.client.onConnect = (frame) => {
      console.log('Connected: ' + frame);
      this.isConnected = true;
      this.subscribeToTopics();
    };

    this.client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.client.onWebSocketError = (error) => {
      console.error('WebSocket error:', error);
    };

    this.client.onDisconnect = () => {
      console.log('Disconnected');
      this.isConnected = false;
    };

    this.client.activate();
  }

  subscribeToTopics() {
    // Subscribe to orders
    this.client.subscribe('/topic/orders', (message) => {
      this.handleOrderMessage(JSON.parse(message.body));
    });

    // Subscribe to notifications
    this.client.subscribe('/topic/notifications', (message) => {
      this.handleNotificationMessage(JSON.parse(message.body));
    });

    // Subscribe to deliveries (for delivery staff)
    this.client.subscribe('/topic/deliveries', (message) => {
      this.handleDeliveryMessage(JSON.parse(message.body));
    });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
    this.isConnected = false;
  }

  sendMessage(destination, message) {
    if (this.isConnected && this.client) {
      this.client.publish({
        destination: destination,
        body: JSON.stringify(message),
      });
    }
  }

  handleOrderMessage(message) {
    console.log('Order message:', message.type);
    // Process order message
    // Example: Update Redux store, show notification, etc.
  }

  handleNotificationMessage(message) {
    console.log('Notification:', message.content);
    // Show in-app notification
  }

  handleDeliveryMessage(message) {
    console.log('Delivery message:', message.type);
    // Process delivery message
  }
}

export default new WebSocketManager();
```

---

## Connection Management

### Connection Lifecycle

**Best Practices**:

1. **Connect** when user logs in successfully
2. **Disconnect** when user logs out
3. **Reconnect** on network change (WiFi ↔ Cellular)
4. **Handle** background/foreground transitions

### Reconnection Strategy

**Exponential Backoff**:
```
Attempt 1: Wait 1 second
Attempt 2: Wait 2 seconds
Attempt 3: Wait 4 seconds
Attempt 4: Wait 8 seconds
Attempt 5: Wait 16 seconds
Max: 60 seconds
```

**Implementation Example**:
```javascript
let reconnectDelay = 1000;
const maxDelay = 60000;
let reconnectAttempts = 0;

function reconnect() {
  if (reconnectAttempts < 10) {
    setTimeout(() => {
      console.log(`Reconnecting... Attempt ${reconnectAttempts + 1}`);
      connect();
      reconnectAttempts++;
      reconnectDelay = Math.min(reconnectDelay * 2, maxDelay);
    }, reconnectDelay);
  }
}

function onDisconnect() {
  console.log('Disconnected. Will attempt to reconnect...');
  reconnect();
}

function onConnect() {
  console.log('Connected successfully');
  reconnectAttempts = 0;
  reconnectDelay = 1000;
}
```

### Background/Foreground Handling

**iOS**:
```swift
NotificationCenter.default.addObserver(
    self,
    selector: #selector(appDidEnterBackground),
    name: UIApplication.didEnterBackgroundNotification,
    object: nil
)

NotificationCenter.default.addObserver(
    self,
    selector: #selector(appWillEnterForeground),
    name: UIApplication.willEnterForegroundNotification,
    object: nil
)

@objc func appDidEnterBackground() {
    // Keep connection alive or disconnect
    // iOS allows 30 seconds of background execution
}

@objc func appWillEnterForeground() {
    // Reconnect if disconnected
    if !webSocketManager.isConnected {
        webSocketManager.connect()
    }
}
```

**Android**:
```kotlin
override fun onPause() {
    super.onPause()
    // Optionally disconnect to save battery
    // webSocketManager.disconnect()
}

override fun onResume() {
    super.onResume()
    // Reconnect if needed
    if (!webSocketManager.isConnected) {
        webSocketManager.connect()
    }
}
```

---

## Error Handling

### Connection Errors

**Error Types**:
1. **Network Error**: No internet connection
2. **Timeout**: Server not responding
3. **Server Error**: Server rejected connection
4. **Protocol Error**: STOMP protocol error

**Handling**:
```
1. Log error for debugging
2. Show user-friendly message
3. Implement retry with backoff
4. Update connection status UI
5. Notify user when reconnected
```

### Message Parsing Errors

**Best Practices**:
- Always use try-catch when parsing JSON
- Validate message structure
- Log malformed messages
- Continue processing other messages
- Don't crash on parse errors

**Example**:
```javascript
function handleMessage(payload) {
  try {
    const message = JSON.parse(payload);

    if (!message.type || !message.content) {
      console.error('Invalid message format:', payload);
      return;
    }

    // Process valid message
    processMessage(message);
  } catch (error) {
    console.error('Error parsing message:', error);
    // Continue operation, don't crash
  }
}
```

---

## Best Practices

### Connection

✅ **DO**:
- Connect after successful login
- Disconnect on logout
- Implement reconnection logic
- Handle network changes
- Show connection status in UI
- Use connection heartbeat/ping

❌ **DON'T**:
- Connect before authentication
- Keep connection open after logout
- Ignore connection state
- Silently fail connection errors
- Block UI during connection

### Subscriptions

✅ **DO**:
- Subscribe after connection established
- Unsubscribe when leaving screens
- Subscribe to role-appropriate topics only
- Keep track of active subscriptions

❌ **DON'T**:
- Subscribe before connection
- Forget to unsubscribe
- Subscribe to all topics unnecessarily
- Re-subscribe on every render

### Message Handling

✅ **DO**:
- Parse messages safely with try-catch
- Validate message format
- Update UI on main thread
- Show notifications for important messages
- Log messages for debugging (dev only)

❌ **DON'T**:
- Assume message format is always correct
- Block UI thread with processing
- Show notification for every message
- Log sensitive data in production

### Performance

✅ **DO**:
- Batch UI updates if many messages
- Debounce rapid message handling
- Use background threads for processing
- Implement message queue if needed

❌ **DON'T**:
- Update UI for every message immediately
- Process messages synchronously
- Keep all messages in memory
- Create memory leaks with subscriptions

### Security

✅ **DO**:
- Use WSS (WebSocket Secure) in production
- Validate message source
- Sanitize message content before display
- Implement rate limiting for sending

❌ **DON'T**:
- Use WS (unencrypted) in production
- Trust message content without validation
- Execute code from messages
- Send sensitive data via WebSocket

---

## Code Examples

### Role-Based Topic Subscription

```javascript
class RoleBasedWebSocketManager extends WebSocketManager {
  subscribeBasedOnRole(userRole) {
    // All roles
    this.subscribe('/topic/notifications');
    this.subscribe('/topic/system');

    // Role-specific subscriptions
    switch (userRole) {
      case 'ADMIN':
      case 'MANAGER':
        this.subscribe('/topic/orders');
        this.subscribe('/topic/deliveries');
        this.subscribe('/topic/kitchen');
        this.subscribe('/topic/delivery-staff');
        break;

      case 'KITCHEN_STAFF':
        this.subscribe('/topic/orders');
        this.subscribe('/topic/kitchen');
        break;

      case 'DELIVERY_STAFF':
        this.subscribe('/topic/deliveries');
        this.subscribe('/topic/delivery-staff');
        // Subscribe to personal queue
        this.subscribe(`/user/${username}/queue/notifications`);
        break;
    }
  }
}
```

### Complete WebSocket Service with State Management

```typescript
// websocket.service.ts
import { BehaviorSubject, Observable } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface WebSocketMessage {
  type: string;
  content: string;
  sender: string;
  timestamp: string;
}

export enum ConnectionStatus {
  CONNECTING = 'CONNECTING',
  CONNECTED = 'CONNECTED',
  DISCONNECTED = 'DISCONNECTED',
  ERROR = 'ERROR',
}

class WebSocketService {
  private client: Client | null = null;
  private connectionStatus$ = new BehaviorSubject<ConnectionStatus>(
    ConnectionStatus.DISCONNECTED
  );
  private messages$ = new BehaviorSubject<WebSocketMessage | null>(null);

  constructor() {
    this.initializeClient();
  }

  private initializeClient() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        if (process.env.NODE_ENV === 'development') {
          console.log('STOMP:', str);
        }
      },
      onConnect: () => {
        console.log('WebSocket connected');
        this.connectionStatus$.next(ConnectionStatus.CONNECTED);
        this.subscribeToTopics();
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
        this.connectionStatus$.next(ConnectionStatus.DISCONNECTED);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.connectionStatus$.next(ConnectionStatus.ERROR);
      },
      onWebSocketError: (error) => {
        console.error('WebSocket error:', error);
        this.connectionStatus$.next(ConnectionStatus.ERROR);
      },
    });
  }

  connect() {
    if (this.client && !this.client.active) {
      this.connectionStatus$.next(ConnectionStatus.CONNECTING);
      this.client.activate();
    }
  }

  disconnect() {
    if (this.client && this.client.active) {
      this.client.deactivate();
    }
  }

  private subscribeToTopics() {
    if (!this.client) return;

    // Subscribe to orders
    this.client.subscribe('/topic/orders', (message) => {
      this.handleMessage(JSON.parse(message.body));
    });

    // Subscribe to deliveries
    this.client.subscribe('/topic/deliveries', (message) => {
      this.handleMessage(JSON.parse(message.body));
    });

    // Subscribe to notifications
    this.client.subscribe('/topic/notifications', (message) => {
      this.handleMessage(JSON.parse(message.body));
    });
  }

  private handleMessage(message: WebSocketMessage) {
    console.log('Received message:', message.type);
    this.messages$.next(message);
  }

  sendMessage(destination: string, message: any) {
    if (this.client && this.client.active) {
      this.client.publish({
        destination,
        body: JSON.stringify(message),
      });
    }
  }

  getConnectionStatus(): Observable<ConnectionStatus> {
    return this.connectionStatus$.asObservable();
  }

  getMessages(): Observable<WebSocketMessage | null> {
    return this.messages$.asObservable();
  }
}

export default new WebSocketService();
```

---

**Last Updated**: 2025-11-13
**Version**: 1.0.1
