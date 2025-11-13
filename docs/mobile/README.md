# Mobile Integration Guide - Restaurant Admin System

Welcome to the Restaurant Admin System mobile integration documentation. This guide will help you integrate your mobile application (iOS, Android, Flutter, React Native) with the ResAdmin backend API.

## Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Documentation Structure](#documentation-structure)
4. [Base Configuration](#base-configuration)
5. [Authentication Flow](#authentication-flow)
6. [Common Use Cases](#common-use-cases)
7. [Support](#support)

---

## Overview

**Restaurant Admin System (ResAdmin)** is a comprehensive REST API built with Spring Boot that provides complete restaurant management capabilities including:

- **User Management** with role-based access control
- **Product & Category Management** for menu items
- **Order Management** with real-time kitchen updates
- **Delivery Management** with driver assignment and tracking
- **Real-time WebSocket** notifications for orders and deliveries
- **JWT Authentication** with 24-hour token expiration

### System Information

- **Base URL**: `http://localhost:8080/api` (development)
- **Production URL**: `https://your-domain.com/api` (configure as needed)
- **API Version**: 1.0.1
- **Authentication**: JWT Bearer Token
- **Real-time**: WebSocket with STOMP protocol
- **Documentation**: Swagger UI available at `/swagger-ui.html`

### Technology Stack

- **Backend**: Spring Boot 3.5.5 with Java 17
- **Database**: PostgreSQL 12+
- **Security**: Spring Security with JWT (HS256)
- **WebSocket**: STOMP over SockJS
- **API Docs**: OpenAPI 3.0 (Swagger)

---

## Quick Start

### 1. Prerequisites

Before integrating with the mobile app, ensure:

- [ ] Backend server is running (default: `http://localhost:8080`)
- [ ] Database is properly configured and migrated
- [ ] You have test credentials (default admin: `admin` / `password123`)
- [ ] Network connectivity between mobile device/emulator and backend

### 2. Test API Connection

**Request**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN",
      "enabled": true
    }
  }
}
```

### 3. Make Authenticated Request

**Request**:
```bash
curl -X GET http://localhost:8080/api/auth/info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Test WebSocket Connection

Connect to: `ws://localhost:8080/ws` and subscribe to `/topic/notifications`

---

## Documentation Structure

This documentation is organized into the following sections:

### 📚 [API Reference](./API-Reference.md)
Complete API endpoint documentation with:
- All available endpoints
- Request/response formats
- Query parameters
- Required permissions
- Example requests

### 🔐 [Authentication Guide](./Authentication.md)
Security and authentication details:
- JWT implementation
- Login/registration flow
- Token management
- Role-based access control
- Error handling

### 🔌 [WebSocket Integration](./WebSocket-Integration.md)
Real-time features documentation:
- Connection setup
- Topic subscriptions
- Message formats
- Platform-specific implementation
- Reconnection strategies

### 📦 [Data Models](./Data-Models.md)
Complete data structure reference:
- Entity models
- DTOs and request/response formats
- Validation rules
- Relationships

### 📱 [Platform Guides](./Platform-Guides.md)
Platform-specific implementation guides:
- iOS (Swift/SwiftUI)
- Android (Kotlin/Java)
- Flutter
- React Native

### 🎯 [Use Case Examples](./Use-Case-Examples.md)
Step-by-step implementation examples:
- Complete authentication flow
- Order creation and tracking
- Real-time kitchen updates
- Delivery driver app workflow
- Manager dashboard

---

## Base Configuration

### Environment Setup

Create a configuration file for your mobile app:

**iOS (Config.swift)**:
```swift
struct APIConfig {
    static let baseURL = "http://localhost:8080/api"
    static let wsURL = "ws://localhost:8080/ws"
    static let timeout: TimeInterval = 30
}
```

**Android (ApiConfig.kt)**:
```kotlin
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/api"  // Use 10.0.2.2 for Android emulator
    const val WS_URL = "ws://10.0.2.2:8080/ws"
    const val TIMEOUT = 30L
}
```

**Flutter (config.dart)**:
```dart
class ApiConfig {
  static const String baseUrl = 'http://localhost:8080/api';
  static const String wsUrl = 'ws://localhost:8080/ws';
  static const Duration timeout = Duration(seconds: 30);
}
```

**React Native (config.js)**:
```javascript
export const API_CONFIG = {
  baseURL: 'http://localhost:8080/api',
  wsURL: 'ws://localhost:8080/ws',
  timeout: 30000,
};
```

### Network Configuration Notes

**Android Emulator**:
- Use `10.0.2.2` instead of `localhost` to access host machine
- Ensure internet permission in `AndroidManifest.xml`

**iOS Simulator**:
- Can use `localhost` directly
- For HTTPS, configure App Transport Security in `Info.plist`

**Physical Devices**:
- Use your computer's local IP address (e.g., `192.168.1.100`)
- Ensure devices are on the same network
- Configure firewall to allow connections on port 8080

---

## Authentication Flow

### Basic Flow Diagram

```
┌─────────────┐
│ User Opens  │
│    App      │
└──────┬──────┘
       │
       ▼
┌─────────────────┐      ┌──────────────────┐
│ Check Token     │─────▶│ Token Valid?     │
│ in Storage      │      │ Not Expired?     │
└─────────────────┘      └────────┬─────────┘
                                  │
                    ┌─────────────┼──────────────┐
                    │ No          │              │ Yes
                    ▼             ▼              ▼
              ┌──────────┐  ┌──────────┐  ┌──────────┐
              │  Show    │  │  Verify  │  │   Go to  │
              │  Login   │  │  Token   │  │   Main   │
              │  Screen  │  │  on API  │  │  Screen  │
              └────┬─────┘  └────┬─────┘  └──────────┘
                   │             │
                   ▼             ▼
              ┌──────────────────────┐
              │  POST /auth/login    │
              │  username, password  │
              └──────────┬───────────┘
                         │
           ┌─────────────┼──────────────┐
           │ Success     │              │ Error
           ▼             ▼              ▼
     ┌──────────┐  ┌──────────┐  ┌──────────┐
     │  Store   │  │  Set as  │  │   Show   │
     │  Token   │  │  Header  │  │  Error   │
     │ Securely │  │  Bearer  │  │ Message  │
     └────┬─────┘  └────┬─────┘  └──────────┘
          │             │
          └──────┬──────┘
                 │
                 ▼
          ┌──────────┐
          │   Main   │
          │  Screen  │
          └──────────┘
```

### Implementation Steps

1. **Check for stored token** on app launch
2. **Validate token** by calling `/api/auth/info`
3. **Show login** if no valid token exists
4. **POST credentials** to `/api/auth/login`
5. **Store token securely**:
   - iOS: Keychain Services
   - Android: EncryptedSharedPreferences
   - Flutter: flutter_secure_storage
   - React Native: react-native-keychain
6. **Include token** in all requests: `Authorization: Bearer {token}`
7. **Handle 401 errors** by clearing token and showing login

---

## Common Use Cases

### 1. Manager Creating an Order

```
1. Fetch available products: GET /api/products/available
2. User selects products and quantities
3. Calculate total locally
4. Create order: POST /api/orders
5. Receive confirmation + WebSocket notification
6. Display order details
```

### 2. Kitchen Staff Workflow

```
1. Connect to WebSocket: /topic/kitchen
2. Fetch pending orders: GET /api/orders/kitchen
3. Display orders in queue
4. Receive real-time new order notifications
5. Update order status: PATCH /api/orders/{id}/status
6. Broadcast status change via WebSocket
```

### 3. Delivery Driver Workflow

```
1. Login as DELIVERY_STAFF user
2. Fetch assigned deliveries: GET /api/deliveries/my
3. Subscribe to: /topic/delivery-staff
4. Receive new delivery assignment notification
5. Accept and start delivery
6. Update status: PATCH /api/deliveries/{id}/status
7. Mark as delivered when complete
```

### 4. Admin Dashboard

```
1. Fetch today's statistics: GET /api/orders/stats/today
2. List all active orders: GET /api/orders?status=CONFIRMED,PREPARING
3. Monitor delivery stats: GET /api/deliveries/stats
4. Manage users: GET /api/users
5. Subscribe to /topic/system for alerts
```

---

## User Roles and Permissions

### ADMIN
- **Full Access**: All operations across all modules
- **Use Cases**: System administration, user management, reports

### MANAGER
- **Management Access**: Product, category, order, delivery management
- **Use Cases**: Restaurant operations, order processing, staff coordination

### KITCHEN_STAFF
- **Kitchen Operations**: View orders, update order status, toggle product availability
- **Use Cases**: Kitchen display system, order preparation tracking

### DELIVERY_STAFF
- **Delivery Operations**: View assigned deliveries, update delivery status
- **Use Cases**: Driver mobile app, delivery tracking

---

## Error Handling

All API responses follow a consistent format:

**Success Response**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ },
  "timestamp": "2025-11-13T10:30:00"
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error message",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

**Common HTTP Status Codes**:
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Best Practices

### Security
- ✅ Store JWT tokens securely (Keychain/EncryptedSharedPreferences)
- ✅ Never log tokens in production
- ✅ Implement token refresh before expiration
- ✅ Use HTTPS in production
- ✅ Implement certificate pinning
- ✅ Validate SSL certificates

### Performance
- ✅ Implement request caching for offline support
- ✅ Use pagination for large lists
- ✅ Cache static data (categories, products)
- ✅ Implement pull-to-refresh
- ✅ Show loading states

### User Experience
- ✅ Handle network errors gracefully
- ✅ Show WebSocket connection status
- ✅ Implement retry mechanisms
- ✅ Provide offline mode when possible
- ✅ Use optimistic updates for better UX

### WebSocket
- ✅ Implement reconnection logic
- ✅ Handle background/foreground transitions
- ✅ Subscribe to role-appropriate topics only
- ✅ Unsubscribe when leaving screens
- ✅ Show connection status to users

---

## Development Workflow

### 1. Local Development Setup

```bash
# Start backend server
cd restaurant-admin-spring
./gradlew bootRun

# Backend will be available at:
# API: http://localhost:8080/api
# WebSocket: ws://localhost:8080/ws
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### 2. Testing with Swagger

1. Open `http://localhost:8080/swagger-ui.html`
2. Click "Authorize" button
3. Login to get JWT token
4. Enter token in format: `Bearer {your-token}`
5. Test all endpoints interactively

### 3. Testing with Postman

1. Import API collection (can be generated from Swagger)
2. Create environment variables:
   - `base_url`: `http://localhost:8080/api`
   - `token`: Your JWT token
3. Set Authorization header: `Bearer {{token}}`

---

## Troubleshooting

### Connection Issues

**Problem**: Cannot connect to API from mobile device
**Solutions**:
- Verify backend is running: `curl http://localhost:8080/api/auth/login`
- For Android emulator, use `10.0.2.2` instead of `localhost`
- For physical devices, use computer's IP address
- Check firewall settings
- Ensure devices are on same network

### Authentication Issues

**Problem**: Receiving 401 Unauthorized errors
**Solutions**:
- Verify token is not expired (24-hour expiration)
- Check Authorization header format: `Bearer {token}`
- Ensure token is stored correctly after login
- Verify user account is enabled
- Check role permissions for the endpoint

### WebSocket Issues

**Problem**: WebSocket not connecting or disconnecting
**Solutions**:
- Verify WebSocket URL: `ws://localhost:8080/ws`
- Implement reconnection logic
- Check network connectivity
- Handle background/foreground app transitions
- Verify STOMP protocol version compatibility

### Data Issues

**Problem**: Invalid data or validation errors
**Solutions**:
- Check required fields in request body
- Verify data types (especially BigDecimal for prices)
- Ensure foreign keys exist (product IDs, category IDs)
- Check maximum length constraints
- Review validation rules in Data Models documentation

---

## Next Steps

1. **Review [API Reference](./API-Reference.md)** for complete endpoint documentation
2. **Implement [Authentication](./Authentication.md)** in your mobile app
3. **Choose your [Platform Guide](./Platform-Guides.md)** for specific implementation
4. **Integrate [WebSocket](./WebSocket-Integration.md)** for real-time features
5. **Review [Use Case Examples](./Use-Case-Examples.md)** for complete workflows

---

## Support

### Resources
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **GitHub Repository**: [Your Repository URL]
- **Issue Tracker**: [Your Issue Tracker URL]

### Default Test Accounts

```
Admin Account:
  username: admin
  password: password123
  role: ADMIN

Manager Account:
  username: manager
  password: password123
  role: MANAGER

Kitchen Staff Account:
  username: kitchen
  password: password123
  role: KITCHEN_STAFF

Delivery Staff Account:
  username: driver
  password: password123
  role: DELIVERY_STAFF
```

### Contact
For questions or issues, please contact your development team or create an issue in the project repository.

---

**Version**: 1.0.1
**Last Updated**: 2025-11-13
**Author**: Restaurant Admin System Team
