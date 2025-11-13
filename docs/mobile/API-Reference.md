# API Reference

Complete REST API endpoint documentation for the Restaurant Admin System.

## Base URL

```
Development: http://localhost:8080/api
Production: https://your-domain.com/api
```

## Authentication

All protected endpoints require JWT authentication via the Authorization header:

```
Authorization: Bearer {your-jwt-token}
```

---

## Table of Contents

1. [Authentication Endpoints](#authentication-endpoints)
2. [User Management](#user-management)
3. [Category Management](#category-management)
4. [Product Management](#product-management)
5. [Order Management](#order-management)
6. [Delivery Management](#delivery-management)
7. [Delivery Driver Endpoints](#delivery-driver-endpoints)
8. [Response Formats](#response-formats)

---

## Authentication Endpoints

**Base Path**: `/api/auth`

### Login

Authenticate a user and receive a JWT token.

**Endpoint**: `POST /api/auth/login`
**Authentication**: None (Public)

**Request Body**:
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "[email protected]",
      "fullName": "System Administrator",
      "role": "ADMIN",
      "enabled": true,
      "createdAt": "2025-01-01T00:00:00",
      "updatedAt": "2025-01-01T00:00:00"
    }
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

**Error Response**: `401 Unauthorized`
```json
{
  "success": false,
  "message": "Invalid credentials",
  "error": "Bad credentials",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Register

Register a new user account.

**Endpoint**: `POST /api/auth/register`
**Authentication**: None (Public)

**Request Body**:
```json
{
  "username": "newuser",
  "password": "securePassword123",
  "email": "[email protected]",
  "fullName": "John Doe",
  "role": "DELIVERY_STAFF"
}
```

**Validation Rules**:
- `username`: 3-50 characters, unique
- `password`: Minimum 6 characters
- `email`: Valid email format, max 100 characters
- `fullName`: Max 100 characters
- `role`: One of `ADMIN`, `MANAGER`, `KITCHEN_STAFF`, `DELIVERY_STAFF`

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 15,
    "username": "newuser",
    "email": "[email protected]",
    "fullName": "John Doe",
    "role": "DELIVERY_STAFF",
    "enabled": true,
    "createdAt": "2025-11-13T10:30:00",
    "updatedAt": "2025-11-13T10:30:00"
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Get Current User Info

Retrieve information about the currently authenticated user.

**Endpoint**: `GET /api/auth/info`
**Authentication**: Required
**Roles**: All

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "User info retrieved successfully",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "[email protected]",
    "fullName": "System Administrator",
    "role": "ADMIN",
    "enabled": true,
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-01T00:00:00"
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Change Password

Change the password for the currently authenticated user.

**Endpoint**: `POST /api/auth/change-password`
**Authentication**: Required
**Roles**: All

**Request Body**:
```json
{
  "oldPassword": "currentPassword",
  "newPassword": "newSecurePassword123"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

---

## User Management

**Base Path**: `/api/users`
**Required Roles**: ADMIN, MANAGER (some endpoints ADMIN only)

### List Users (Paginated)

Retrieve a paginated list of users with optional filters.

**Endpoint**: `GET /api/users`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Query Parameters**:
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size
- `sortBy` (optional, default: "id"): Sort field
- `sortDir` (optional, default: "asc"): Sort direction (asc/desc)
- `username` (optional): Filter by username (partial match)
- `role` (optional): Filter by role
- `enabled` (optional): Filter by enabled status (true/false)

**Example Request**:
```
GET /api/users?page=0&size=20&sortBy=username&sortDir=asc&role=DELIVERY_STAFF&enabled=true
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 5,
        "username": "driver1",
        "email": "[email protected]",
        "fullName": "Mike Driver",
        "role": "DELIVERY_STAFF",
        "enabled": true,
        "createdAt": "2025-01-05T10:00:00",
        "updatedAt": "2025-01-05T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Get User by ID

**Endpoint**: `GET /api/users/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: `200 OK` (returns single user object)

---

### Get User by Username

**Endpoint**: `GET /api/users/username/{username}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

---

### Get Users by Role

**Endpoint**: `GET /api/users/role/{role}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Valid Roles**: `ADMIN`, `MANAGER`, `KITCHEN_STAFF`, `DELIVERY_STAFF`

---

### Get Available Delivery Drivers

Get all delivery staff who are available for assignment.

**Endpoint**: `GET /api/users/delivery-staff/available`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: Returns list of users with role `DELIVERY_STAFF` and `enabled=true`

---

### Update User Role

**Endpoint**: `PATCH /api/users/{id}/role`
**Authentication**: Required
**Roles**: ADMIN only

**Request Body**:
```json
{
  "role": "MANAGER"
}
```

---

### Toggle User Status

Enable or disable a user account.

**Endpoint**: `PATCH /api/users/{id}/toggle-status`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: Returns updated user object with toggled `enabled` status

---

### Delete User

**Endpoint**: `DELETE /api/users/{id}`
**Authentication**: Required
**Roles**: ADMIN only

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

---

## Category Management

**Base Path**: `/api/categories`

### List Categories

**Endpoint**: `GET /api/categories`
**Authentication**: Required
**Roles**: All (read), ADMIN/MANAGER (write)

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response**: `200 OK` (paginated response with category list)

---

### Get Category by ID

**Endpoint**: `GET /api/categories/{id}`
**Authentication**: Required
**Roles**: All

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Category retrieved successfully",
  "data": {
    "id": 1,
    "name": "Appetizers",
    "description": "Starters and small bites",
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-01T00:00:00"
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Create Category

**Endpoint**: `POST /api/categories`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**:
```json
{
  "name": "Desserts",
  "description": "Sweet treats and after-dinner delights"
}
```

**Validation Rules**:
- `name`: Required, max 100 characters, must be unique
- `description`: Optional, max 500 characters

---

### Update Category

**Endpoint**: `PUT /api/categories/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**: Same as Create Category

---

### Delete Category

**Endpoint**: `DELETE /api/categories/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Note**: Cannot delete category if it has associated products

---

### Search Categories

**Endpoint**: `GET /api/categories/search?name={name}`
**Authentication**: Required
**Roles**: All

**Response**: Returns categories with names containing the search term

---

### Check Category Exists

**Endpoint**: `GET /api/categories/exists/{name}`
**Authentication**: Required
**Roles**: All

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Category exists check",
  "data": true,
  "timestamp": "2025-11-13T10:30:00"
}
```

---

## Product Management

**Base Path**: `/api/products`

### List Products (with Filters)

**Endpoint**: `GET /api/products`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, KITCHEN_STAFF

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `category` (optional): Filter by category name
- `available` (optional): Filter by availability (true/false)
- `name` (optional): Search by name (partial match)
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter

**Example Request**:
```
GET /api/products?category=Main Courses&available=true&minPrice=10.00&maxPrice=30.00
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": 10,
        "name": "Grilled Salmon",
        "description": "Fresh Atlantic salmon with herbs",
        "price": 24.99,
        "imageUrl": "https://example.com/images/salmon.jpg",
        "isAvailable": true,
        "category": {
          "id": 2,
          "name": "Main Courses",
          "description": "Entrees and main dishes"
        },
        "createdAt": "2025-01-10T12:00:00",
        "updatedAt": "2025-01-10T12:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Get Available Products

Get all products that are currently available (isAvailable=true).

**Endpoint**: `GET /api/products/available`
**Authentication**: Required
**Roles**: All

**Response**: Returns list of available products

---

### Get Product by ID

**Endpoint**: `GET /api/products/{id}`
**Authentication**: Required
**Roles**: All

---

### Get Products by Category

**Endpoint**: `GET /api/products/category/{categoryId}`
**Authentication**: Required
**Roles**: All

---

### Get Available Products in Category

**Endpoint**: `GET /api/products/category/{categoryId}/available`
**Authentication**: Required
**Roles**: All

---

### Create Product

**Endpoint**: `POST /api/products`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**:
```json
{
  "name": "Grilled Salmon",
  "description": "Fresh Atlantic salmon with herbs and lemon",
  "price": 24.99,
  "imageUrl": "https://example.com/images/salmon.jpg",
  "isAvailable": true,
  "categoryId": 2
}
```

**Validation Rules**:
- `name`: Required, max 100 characters
- `description`: Optional, max 1000 characters
- `price`: Required, must be positive (BigDecimal with precision 10, scale 2)
- `imageUrl`: Optional
- `isAvailable`: Optional, default true
- `categoryId`: Required, must exist

---

### Update Product

**Endpoint**: `PUT /api/products/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**: Same as Create Product

---

### Toggle Product Availability

**Endpoint**: `PATCH /api/products/{id}/toggle-availability`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, KITCHEN_STAFF

**Response**: Returns product with toggled `isAvailable` status

---

### Delete Product

**Endpoint**: `DELETE /api/products/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

---

### Search Products

**Endpoint**: `GET /api/products/search?name={name}`
**Authentication**: Required
**Roles**: All

---

### Filter by Price Range

**Endpoint**: `GET /api/products/price-range?minPrice={min}&maxPrice={max}`
**Authentication**: Required
**Roles**: All

---

## Order Management

**Base Path**: `/api/orders`

### Order Status Flow

```
PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP / READY_FOR_DELIVERY →
OUT_FOR_DELIVERY (delivery only) → COMPLETED

Any status can transition to → CANCELLED
```

### Order Types

- `DINE_IN`: Customer dining in restaurant
- `TAKEOUT`: Customer pickup
- `DELIVERY`: Home delivery
- `PICKUP`: Curbside pickup

---

### List Orders (with Filters)

**Endpoint**: `GET /api/orders`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `sortBy` (optional, default: "createdAt")
- `sortDir` (optional, default: "desc")
- `status` (optional): Filter by order status
- `orderType` (optional): Filter by order type
- `from` (optional): Start date (ISO format)
- `to` (optional): End date (ISO format)

**Example Request**:
```
GET /api/orders?status=CONFIRMED&orderType=DELIVERY&from=2025-11-01&to=2025-11-13&page=0&size=20
```

---

### Get Order by ID

**Endpoint**: `GET /api/orders/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Order retrieved successfully",
  "data": {
    "id": 101,
    "customerDetails": "John Doe, +1234567890, 123 Main St",
    "status": "CONFIRMED",
    "totalPrice": 45.97,
    "orderType": "DELIVERY",
    "createdAt": "2025-11-13T09:00:00",
    "orderItems": [
      {
        "id": 201,
        "product": {
          "id": 5,
          "name": "Cheeseburger",
          "price": 12.99
        },
        "quantity": 2,
        "price": 12.99,
        "totalPrice": 25.98
      },
      {
        "id": 202,
        "product": {
          "id": 8,
          "name": "French Fries",
          "price": 4.99
        },
        "quantity": 4,
        "price": 4.99,
        "totalPrice": 19.96
      }
    ],
    "delivery": {
      "id": 50,
      "status": "ASSIGNED",
      "driver": {
        "id": 5,
        "username": "driver1",
        "fullName": "Mike Driver"
      },
      "deliveryAddress": "123 Main St",
      "dispatchedAt": "2025-11-13T09:10:00"
    }
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Create Order

**Endpoint**: `POST /api/orders`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**:
```json
{
  "customerName": "John Doe",
  "customerPhone": "+1234567890",
  "customerAddress": "123 Main St, City, State 12345",
  "notes": "Extra napkins please, ring doorbell",
  "totalAmount": 45.97,
  "orderType": "DELIVERY",
  "items": [
    {
      "productId": 5,
      "quantity": 2,
      "price": 12.99
    },
    {
      "productId": 8,
      "quantity": 4,
      "price": 4.99
    }
  ]
}
```

**Validation Rules**:
- `customerName`: Required
- `customerPhone`: Required
- `customerAddress`: Required for DELIVERY orders
- `items`: Required, must have at least 1 item
- `items[].productId`: Must exist
- `items[].quantity`: Must be > 0
- `items[].price`: Must match current product price

**Response**: `200 OK` (returns created order with generated ID)

**Note**: System automatically calculates total from items and sends WebSocket notification to kitchen

---

### Update Order

**Endpoint**: `PUT /api/orders/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**: Same as Create Order

---

### Update Order Status

**Endpoint**: `PATCH /api/orders/{id}/status`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF

**Request Body**:
```json
{
  "status": "PREPARING"
}
```

**Valid Status Values**:
- `PENDING`
- `CONFIRMED`
- `PREPARING`
- `READY_FOR_PICKUP`
- `READY_FOR_DELIVERY`
- `OUT_FOR_DELIVERY`
- `COMPLETED`
- `CANCELLED`

**Response**: Returns updated order + sends WebSocket notification

---

### Get Kitchen Orders

Get orders relevant to kitchen staff (statuses: CONFIRMED, PREPARING).

**Endpoint**: `GET /api/orders/kitchen`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, KITCHEN_STAFF

---

### Get Delivery Orders

Get orders for delivery (type=DELIVERY, various statuses).

**Endpoint**: `GET /api/orders/delivery`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Get Orders Ready for Delivery

**Endpoint**: `GET /api/orders/ready-for-delivery`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Get Today's Orders

**Endpoint**: `GET /api/orders/today`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

---

### Get Orders by Status

**Endpoint**: `GET /api/orders/status/{status}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF

**Query Parameters**: Supports pagination (page, size, sortBy, sortDir)

---

### Get Orders by Date Range

**Endpoint**: `GET /api/orders/date-range?startDate={date}&endDate={date}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Date Format**: ISO 8601 (e.g., `2025-11-01`)

---

### Get Order Items

**Endpoint**: `GET /api/orders/{id}/items`
**Authentication**: Required
**Roles**: All authenticated users

---

### Get Today's Statistics

**Endpoint**: `GET /api/orders/stats/today`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Today's statistics",
  "data": {
    "totalOrders": 45,
    "totalRevenue": 1234.56,
    "averageOrderValue": 27.43,
    "ordersByStatus": {
      "PENDING": 3,
      "CONFIRMED": 5,
      "PREPARING": 8,
      "READY_FOR_DELIVERY": 2,
      "OUT_FOR_DELIVERY": 4,
      "COMPLETED": 20,
      "CANCELLED": 3
    },
    "ordersByType": {
      "DINE_IN": 15,
      "TAKEOUT": 10,
      "DELIVERY": 18,
      "PICKUP": 2
    }
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

### Delete Order

**Endpoint**: `DELETE /api/orders/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

---

## Delivery Management

**Base Path**: `/api/deliveries`

### Delivery Status Flow

```
PENDING → ASSIGNED → OUT_FOR_DELIVERY → DELIVERED

Any status can transition to → CANCELLED
```

---

### List All Deliveries

**Endpoint**: `GET /api/deliveries`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Get My Deliveries (Driver)

Get deliveries assigned to the currently logged-in delivery driver.

**Endpoint**: `GET /api/deliveries/my`
**Authentication**: Required
**Roles**: DELIVERY_STAFF

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `sortBy` (optional, default: "dispatchedAt")
- `sortDir` (optional, default: "desc")

---

### Get Delivery by ID

**Endpoint**: `GET /api/deliveries/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Get Delivery by Order ID

**Endpoint**: `GET /api/deliveries/order/{orderId}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Get Deliveries by Driver

**Endpoint**: `GET /api/deliveries/driver/{driverId}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

---

### Get Deliveries by Status

**Endpoint**: `GET /api/deliveries/status/{status}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

**Valid Status Values**: `PENDING`, `ASSIGNED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`

---

### Get Pending Deliveries

**Endpoint**: `GET /api/deliveries/pending`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Get Active Deliveries

Get deliveries with status ASSIGNED or OUT_FOR_DELIVERY.

**Endpoint**: `GET /api/deliveries/active`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Get Today's Deliveries

**Endpoint**: `GET /api/deliveries/today`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

---

### Assign Delivery

Assign an order to a delivery driver.

**Endpoint**: `POST /api/deliveries/assign`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**:
```json
{
  "orderId": 101,
  "driverId": 5,
  "deliveryAddress": "123 Main St, City, State 12345",
  "deliveryNotes": "Ring doorbell, gate code: 1234. Leave at front door if no answer."
}
```

**Response**: `200 OK` (returns created delivery + sends WebSocket notification to driver)

---

### Update Delivery Status

**Endpoint**: `PATCH /api/deliveries/{id}/status`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

**Request Body**:
```json
{
  "status": "OUT_FOR_DELIVERY"
}
```

**Response**: Returns updated delivery + sends WebSocket notification

---

### Update Delivery Details

**Endpoint**: `PUT /api/deliveries/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**:
```json
{
  "deliveryAddress": "Updated address",
  "deliveryNotes": "Updated notes"
}
```

---

### Reassign Delivery Driver

**Endpoint**: `PATCH /api/deliveries/{id}/reassign`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Request Body**:
```json
{
  "driverId": 7
}
```

---

### Cancel Delivery

**Endpoint**: `DELETE /api/deliveries/{id}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: Sets delivery status to CANCELLED

---

### Get Deliveries by Date Range

**Endpoint**: `GET /api/deliveries/date-range?startDate={date}&endDate={date}`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

---

### Get Available Drivers

**Endpoint**: `GET /api/deliveries/drivers/available`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: Returns list of delivery staff who are enabled and available

---

### Get Delivery Statistics

**Endpoint**: `GET /api/deliveries/stats`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Delivery statistics",
  "data": {
    "totalDeliveries": 120,
    "activeDeliveries": 8,
    "completedDeliveries": 105,
    "cancelledDeliveries": 7,
    "averageDeliveryTime": 25.5,
    "deliveriesByDriver": [
      {
        "driverId": 5,
        "driverName": "Mike Driver",
        "totalDeliveries": 45,
        "completedToday": 8
      }
    ]
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

---

## Delivery Driver Endpoints

**Base Path**: `/api/delivery-drivers`

### Get All Delivery Drivers

**Endpoint**: `GET /api/delivery-drivers`
**Authentication**: Required
**Roles**: ADMIN, MANAGER, DELIVERY_STAFF

**Response**: Returns list of all users with role `DELIVERY_STAFF`

---

### Get Available Drivers

**Endpoint**: `GET /api/delivery-drivers/available`
**Authentication**: Required
**Roles**: ADMIN, MANAGER

**Response**: Returns delivery drivers who are enabled and not currently on active delivery

---

## Response Formats

### Standard Success Response

```json
{
  "success": true,
  "message": "Operation description",
  "data": { /* response data or null */ },
  "timestamp": "2025-11-13T10:30:00"
}
```

### Standard Error Response

```json
{
  "success": false,
  "message": "Error summary",
  "error": "Detailed error message",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

### Paginated Response

```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": {
    "content": [ /* array of items */ ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data or validation error |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions for this operation |
| 404 | Not Found | Requested resource does not exist |
| 409 | Conflict | Resource already exists or conflict with current state |
| 500 | Internal Server Error | Unexpected server error |

---

## Pagination

All list endpoints support pagination with the following query parameters:

- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default: 20, max: 100)
- `sortBy`: Field to sort by (default varies by endpoint)
- `sortDir`: Sort direction - `asc` or `desc` (default varies by endpoint)

**Example**:
```
GET /api/products?page=2&size=50&sortBy=name&sortDir=asc
```

---

## Filtering and Search

Many endpoints support filtering and search parameters:

**Products**:
- `category`: Filter by category name
- `available`: Filter by availability
- `name`: Search by name (partial match)
- `minPrice`, `maxPrice`: Price range filter

**Orders**:
- `status`: Filter by order status
- `orderType`: Filter by order type
- `from`, `to`: Date range filter

**Users**:
- `username`: Filter by username (partial match)
- `role`: Filter by user role
- `enabled`: Filter by enabled status

---

## Rate Limiting

Currently, there are no rate limits enforced. For production deployment, consider implementing rate limiting based on your requirements.

---

## API Versioning

The current API version is **v1**. The base path includes the implicit version. Future versions may use explicit versioning like `/api/v2/`.

---

## Additional Notes

1. **BigDecimal Precision**: All monetary values (prices, totals) use BigDecimal with precision 10 and scale 2
2. **Timestamps**: All timestamps are in ISO 8601 format and use timezone Asia/Phnom_Penh
3. **Cascade Operations**: Deleting an order automatically deletes associated order items and delivery records
4. **Validation**: All request bodies are validated; validation errors return 400 Bad Request with details
5. **WebSocket Notifications**: Most state-changing operations (create/update orders, deliveries) trigger WebSocket notifications

---

**Last Updated**: 2025-11-13
**API Version**: 1.0.1
