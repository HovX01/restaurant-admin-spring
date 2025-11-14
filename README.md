# Restaurant Administration System

A comprehensive REST API for restaurant management built with Spring Boot, featuring JWT authentication, role-based access control, and complete CRUD operations for restaurant operations.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Authentication & Security](#authentication--security)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Setup & Installation](#setup--installation)
- [Usage Examples](#usage-examples)
- [Development](#development)

## Features

### Core Functionality
- **User Management**: Complete user lifecycle with role-based permissions
- **Product Management**: Categories and products with filtering capabilities
- **Order Management**: Order creation, status tracking, payment management, and kitchen workflow
- **Payment Management**: Track payment status and payment methods (Cash on Delivery, Bank, Card)
- **Delivery Management**: Delivery assignment and tracking
- **Authentication**: JWT-based authentication with role-based access control

### Security Features
- JWT token-based authentication
- Role-based authorization (ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF)
- API-only authentication (non-API routes are unrestricted)
- Proper error handling with appropriate HTTP status codes
- CORS configuration for cross-origin requests

### Advanced Features
- Filtering and pagination for all list endpoints
- RESTful API design patterns
- Comprehensive error responses
- Swagger/OpenAPI documentation
- Database relationship management
- **Real-time WebSocket Communication**: Live notifications and updates

### WebSocket Features
- **Real-time Order Notifications**: Instant updates when orders are created or status changes
- **Kitchen Staff Alerts**: Live notifications for new orders requiring preparation
- **Delivery Tracking**: Real-time updates for delivery assignments and status changes
- **Driver Management**: Instant notifications for delivery staff assignments and reassignments
- **System-wide Broadcasts**: Global notifications for important system events
- **User-specific Messages**: Targeted notifications based on user roles and permissions

## Technology Stack

- **Backend**: Spring Boot 3.x
- **Security**: Spring Security with JWT
- **Real-time Communication**: Spring WebSocket with STOMP
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Gradle
- **Java Version**: 17+

## Project Structure

```
src/main/java/com/resadmin/res/
├── config/
│   ├── SecurityConfig.java          # Security configuration
│   └── SwaggerConfig.java           # API documentation config
├── controller/
│   ├── AuthController.java          # Authentication endpoints
│   ├── UserController.java          # User management
│   ├── CategoryController.java      # Category management
│   ├── ProductController.java       # Product management
│   ├── OrderController.java         # Order management
│   └── DeliveryController.java      # Delivery management
├── dto/
│   ├── request/                     # Request DTOs
│   └── response/                    # Response DTOs
├── entity/
│   ├── User.java                    # User entity
│   ├── Category.java                # Category entity
│   ├── Product.java                 # Product entity
│   ├── Order.java                   # Order entity
│   └── Delivery.java                # Delivery entity
├── repository/
│   └── [Entity]Repository.java      # Data access layer
├── security/
│   └── JwtAuthenticationFilter.java # JWT filter
├── service/
│   └── [Entity]Service.java         # Business logic layer
└── util/
    ├── JwtUtil.java                 # JWT utilities
    └── EntityMapper.java            # Entity mapping utilities
```

## Authentication & Security

### JWT Authentication Flow

1. **Login Process**:
   ```
   POST /api/auth/login
   {
     "username": "admin",
     "password": "password123"
   }
   ```
   
   **Success Response (200)**:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "user": {
       "id": 1,
       "username": "admin",
       "role": "ADMIN"
     },
     "message": "Login successful",
     "success": true
   }
   ```
   
   **Error Response (401)**:
   ```json
   {
     "error": "Invalid username or password",
     "message": "Authentication failed",
     "success": false
   }
   ```

2. **Token Usage**:
   - Include JWT token in Authorization header: `Bearer <token>`
   - Token is validated for all `/api/**` routes (except public endpoints)
   - Non-API routes are accessible without authentication

### Security Configuration

- **Public Endpoints**: `/api/auth/**`, `/api/public/**`
- **Non-API Routes**: Unrestricted access (static files, frontend routes)
- **API Routes**: JWT authentication required
- **Role-based Access**: Different endpoints require specific roles

### User Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access, user management, all operations |
| **MANAGER** | Product/category management, order creation, user management |
| **KITCHEN_STAFF** | Order status updates, kitchen operations |
| **DELIVERY_STAFF** | Delivery management, order status updates |

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/register` | User registration | No |
| POST | `/api/auth/change-password` | Change password | No |

### User Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/users` | List users with filtering | ADMIN, MANAGER |
| GET | `/api/users/{id}` | Get user by ID | ADMIN, MANAGER |
| POST | `/api/users` | Create user | ADMIN, MANAGER |
| PUT | `/api/users/{id}` | Update user | ADMIN, MANAGER |
| DELETE | `/api/users/{id}` | Delete user | ADMIN, MANAGER |

### Product & Category Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/categories` | List categories | ADMIN, MANAGER |
| POST | `/api/categories` | Create category | ADMIN, MANAGER |
| PUT | `/api/categories/{id}` | Update category | ADMIN, MANAGER |
| DELETE | `/api/categories/{id}` | Delete category | ADMIN, MANAGER |
| GET | `/api/products` | List products with filtering | ADMIN, MANAGER |
| POST | `/api/products` | Create product | ADMIN, MANAGER |
| PUT | `/api/products/{id}` | Update product | ADMIN, MANAGER |
| DELETE | `/api/products/{id}` | Delete product | ADMIN, MANAGER |

### Order Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/orders` | List orders | ADMIN, MANAGER, KITCHEN_STAFF |
| POST | `/api/orders` | Create order | ADMIN, MANAGER |
| PUT | `/api/orders/{id}/status` | Update order status | ADMIN, MANAGER, KITCHEN_STAFF |
| PATCH | `/api/orders/{id}/payment` | Update payment status | ADMIN, MANAGER |
| POST | `/api/orders/{id}/mark-paid` | Mark order as paid | ADMIN, MANAGER |
| POST | `/api/orders/{id}/mark-unpaid` | Mark order as unpaid | ADMIN, MANAGER |
| GET | `/api/orders/paid` | Get paid orders | ADMIN, MANAGER |
| GET | `/api/orders/unpaid` | Get unpaid orders | ADMIN, MANAGER |
| GET | `/api/orders/payment-method/{method}` | Get orders by payment method | ADMIN, MANAGER |
| GET | `/api/orders/kitchen/**` | Kitchen operations | ADMIN, MANAGER, KITCHEN_STAFF |

### Delivery Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/deliveries` | List deliveries | ADMIN, MANAGER |
| POST | `/api/deliveries/assign` | Assign delivery | ADMIN, MANAGER |
| GET | `/api/deliveries/my/**` | My deliveries | ADMIN, MANAGER, DELIVERY_STAFF |

## Payment Management

### Payment Features

The system supports comprehensive payment tracking for orders:

- **Payment Status**: Track whether an order has been paid or not
- **Payment Methods**: Support for multiple payment methods
  - **CASH_ON_DELIVERY**: Payment upon delivery
  - **BANK**: Bank transfer or online banking
  - **CARD**: Credit/debit card payment

### Payment Operations

#### Creating an Order with Payment Method

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "customerPhone": "555-1234",
    "customerAddress": "123 Main St",
    "paymentMethod": "CARD",
    "totalAmount": 25.99,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 12.99
      }
    ]
  }'
```

#### Marking an Order as Paid

```bash
# Simple mark as paid
curl -X POST http://localhost:8080/api/orders/1/mark-paid?paymentMethod=CASH_ON_DELIVERY \
  -H "Authorization: Bearer <token>"

# Update payment status with details
curl -X PATCH http://localhost:8080/api/orders/1/payment \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "isPaid": true,
    "paymentMethod": "BANK"
  }'
```

#### Retrieving Orders by Payment Status

```bash
# Get all paid orders
curl -X GET http://localhost:8080/api/orders/paid \
  -H "Authorization: Bearer <token>"

# Get all unpaid orders
curl -X GET http://localhost:8080/api/orders/unpaid \
  -H "Authorization: Bearer <token>"

# Get orders by payment method
curl -X GET http://localhost:8080/api/orders/payment-method/CARD \
  -H "Authorization: Bearer <token>"
```

### Payment Status in Order Response

All order responses now include payment information:

```json
{
  "id": 1,
  "customerName": "John Doe",
  "status": "CONFIRMED",
  "totalPrice": 25.99,
  "isPaid": true,
  "paymentMethod": "CARD",
  "orderItems": [...],
  "createdAt": "2024-01-15T10:30:00Z"
}
```

## WebSocket Implementation

### WebSocket Configuration

The system uses Spring WebSocket with STOMP protocol for real-time communication:

- **WebSocket Endpoint**: `/ws`
- **Message Broker**: Simple in-memory broker
- **Application Destination Prefix**: `/app`
- **User Destination Prefix**: `/user`
- **Topic Prefix**: `/topic`

### WebSocket Endpoints

| Destination | Description | Message Type |
|-------------|-------------|-------------|
| `/topic/orders` | Global order notifications | ORDER_CREATED, ORDER_UPDATED |
| `/topic/deliveries` | Global delivery notifications | DELIVERY_ASSIGNED, DELIVERY_STATUS_CHANGED |
| `/topic/kitchen` | Kitchen staff notifications | KITCHEN_NEW_ORDER |
| `/topic/delivery-staff` | Delivery staff notifications | DELIVERY_READY_ORDER, DELIVERY_STAFF_NEW_ASSIGNMENT |
| `/topic/system` | System-wide alerts | SYSTEM_ALERT |
| `/user/{userId}/notifications` | User-specific messages | USER_NOTIFICATION |

### Message Handlers

```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to global order notifications
    stompClient.subscribe('/topic/orders', function(message) {
        const notification = JSON.parse(message.body);
        handleOrderNotification(notification);
    });
    
    // Subscribe to user-specific notifications
    stompClient.subscribe('/user/notifications', function(message) {
        const notification = JSON.parse(message.body);
        handleUserNotification(notification);
    });
});
```

### WebSocket Message Format

```json
{
    "type": "ORDER_CREATED",
    "message": "New order #123 has been created",
    "data": {
        "orderId": 123,
        "status": "PENDING",
        "customerName": "John Doe",
        "totalAmount": 25.99
    },
    "timestamp": "2024-01-15T10:30:00Z"
}
```

### Notification Types

- **ORDER_CREATED**: New order placed
- **ORDER_UPDATED**: Order details modified
- **ORDER_STATUS_CHANGED**: Order status updated
- **DELIVERY_ASSIGNED**: Delivery assigned to driver
- **DELIVERY_STATUS_CHANGED**: Delivery status updated
- **KITCHEN_NEW_ORDER**: New order for kitchen preparation
- **DELIVERY_READY_ORDER**: Order ready for delivery
- **DELIVERY_STAFF_NEW_ASSIGNMENT**: New delivery assignment
- **SYSTEM_ALERT**: System-wide notifications
- **USER_NOTIFICATION**: User-specific messages

### Integration with Business Logic

WebSocket notifications are automatically triggered by:

- **Order Service**: Order creation, updates, and status changes
- **Delivery Service**: Delivery assignments, status updates, and driver reassignments
- **System Events**: Administrative actions and system alerts

### Client Implementation Example

```html
<!DOCTYPE html>
<html>
<head>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <script>
        function connectWebSocket() {
            const socket = new SockJS('/ws');
            const stompClient = Stomp.over(socket);
            
            stompClient.connect({}, function(frame) {
                // Subscribe to relevant topics based on user role
                if (userRole === 'KITCHEN_STAFF') {
                    stompClient.subscribe('/topic/kitchen', handleKitchenNotification);
                }
                
                if (userRole === 'DELIVERY_STAFF') {
                    stompClient.subscribe('/topic/delivery-staff', handleDeliveryNotification);
                }
                
                // All users can receive system alerts
                stompClient.subscribe('/topic/system', handleSystemAlert);
            });
        }
        
        function handleKitchenNotification(message) {
            const data = JSON.parse(message.body);
            showNotification('New Order', data.message);
            updateKitchenDisplay(data.data);
        }
    </script>
</body>
</html>
```

## Database Schema

### Core Entities

- **User**: Authentication and role management
- **Category**: Product categorization
- **Product**: Menu items with category relationships
- **Order**: Customer orders with status tracking, payment status, and payment method
- **Delivery**: Delivery assignments and tracking

### Order Entity Fields

- `id`: Primary key
- `customer_details`: Customer information
- `order_type`: DINE_IN, TAKEOUT, DELIVERY, PICKUP
- `status`: Order status (PENDING, CONFIRMED, PREPARING, etc.)
- `total_price`: Order total amount
- `is_paid`: Payment status (true/false)
- `payment_method`: CASH_ON_DELIVERY, BANK, CARD
- `created_at`: Order creation timestamp

### Key Relationships

- User → Orders (One-to-Many)
- Category → Products (One-to-Many)
- Order → Products (Many-to-Many)
- Delivery → Order (One-to-One)

## Setup & Installation

### Prerequisites

- Java 17 or higher
- PostgreSQL 12+
- Gradle 7+

### Database Setup

1. Create PostgreSQL database:
   ```sql
   CREATE DATABASE res_dev01;
   CREATE USER res_dev01 WITH PASSWORD 'res_dev01';
   GRANT ALL PRIVILEGES ON DATABASE res_dev01 TO res_dev01;
   ```

2. Update `application.properties` if needed:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/res_dev01
   spring.datasource.username=res_dev01
   spring.datasource.password=res_dev01
   ```

### Application Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd ResAdmin
   ```

2. Build the application:
   ```bash
   ./gradlew clean build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

4. Access the application:
   - API Base URL: `http://localhost:8080/api`
   - Swagger Documentation: `http://localhost:8080/swagger-ui.html`

## Usage Examples

### Authentication Flow

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# 2. Use token for authenticated requests
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Product Management

```bash
# Create category
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Beverages","description":"Drinks and beverages"}'

# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Coffee","price":4.99,"categoryId":1}'

# List products with filtering
curl -X GET "http://localhost:8080/api/products?category=Beverages&minPrice=3&maxPrice=10" \
  -H "Authorization: Bearer <token>"
```

## Development

### Running Tests

```bash
./gradlew test
```

### Code Style

- Follow Spring Boot best practices
- Use proper REST conventions
- Implement comprehensive error handling
- Include appropriate logging

### API Documentation

The API is documented using Swagger/OpenAPI 3. Access the interactive documentation at:
`http://localhost:8080/swagger-ui.html`

### Security Considerations

- JWT tokens expire after 24 hours (configurable)
- Passwords are encrypted using BCrypt
- CORS is configured for development (update for production)
- All API endpoints require authentication except public routes
- Non-API routes are unrestricted for frontend integration

### Error Handling

All endpoints return consistent error responses:

```json
{
  "error": "Brief error description",
  "message": "Detailed error message",
  "success": false
}
```

HTTP status codes follow REST conventions:
- 200: Success
- 201: Created
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error

---

**Note**: This system is designed for restaurant administration with comprehensive role-based access control and JWT authentication. The API-only authentication approach allows for flexible frontend integration while maintaining security for backend operations.