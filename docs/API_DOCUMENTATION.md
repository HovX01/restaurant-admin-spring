# API Documentation

Complete REST API reference for the Restaurant Administration System.

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Standard Response Format](#standard-response-format)
4. [Error Handling](#error-handling)
5. [Pagination](#pagination)
6. [Authentication Endpoints](#authentication-endpoints)
7. [User Management](#user-management)
8. [Category Management](#category-management)
9. [Product Management](#product-management)
10. [Order Management](#order-management)
11. [Delivery Management](#delivery-management)

## Overview

**Base URL**: `http://localhost:8080/api`

**Authentication**: JWT Bearer Token (except auth endpoints)

**Content-Type**: `application/json`

**Date Format**: ISO 8601 (`yyyy-MM-dd'T'HH:mm:ss`)

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Obtaining a Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@restaurant.com",
      "fullName": "System Administrator",
      "role": "ADMIN",
      "enabled": true
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## Standard Response Format

All API endpoints return a standardized response format:

### Success Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error description",
  "error": "Error details",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Paginated Response

```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": {
    "content": [/* array of items */],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## Error Handling

### HTTP Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid input, validation errors |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource, illegal state |
| 500 | Internal Server Error | Server error |

### Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "username": "Username is required",
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  },
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

## Pagination

List endpoints support pagination using query parameters:

**Parameters**:
- `page` - Page number (0-indexed, default: 0)
- `size` - Items per page (default: 20)
- `sort` - Sort field and direction (e.g., `createdAt,desc`)

**Example**:
```
GET /api/products?page=0&size=20&sort=createdAt,desc
```

---

## Authentication Endpoints

### Login

Authenticate user and receive JWT token.

**Endpoint**: `POST /api/auth/login`

**Authorization**: None

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
    "type": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@restaurant.com",
      "fullName": "System Administrator",
      "role": "ADMIN",
      "enabled": true
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `401 Unauthorized` - Invalid credentials

---

### Register

Register a new user account.

**Endpoint**: `POST /api/auth/register`

**Authorization**: None (or ADMIN/MANAGER for role assignment)

**Request Body**:
```json
{
  "username": "newuser",
  "password": "securePassword123",
  "email": "user@restaurant.com",
  "fullName": "John Doe",
  "role": "KITCHEN_STAFF"
}
```

**Response**: `201 Created`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 5,
    "username": "newuser",
    "email": "user@restaurant.com",
    "fullName": "John Doe",
    "role": "KITCHEN_STAFF",
    "enabled": true
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `400 Bad Request` - Validation errors
- `409 Conflict` - Username or email already exists

---

### Change Password

Change the authenticated user's password.

**Endpoint**: `POST /api/auth/change-password`

**Authorization**: Required (Bearer Token)

**Request Body**:
```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newSecurePassword123"
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `400 Bad Request` - Current password incorrect
- `401 Unauthorized` - Invalid or missing token

---

### Get Current User Info

Get information about the authenticated user.

**Endpoint**: `GET /api/auth/info`

**Authorization**: Required (Bearer Token)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "User info retrieved",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@restaurant.com",
    "fullName": "System Administrator",
    "role": "ADMIN",
    "enabled": true
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## User Management

### List Users

Get paginated list of users with filtering.

**Endpoint**: `GET /api/users`

**Authorization**: ADMIN, MANAGER

**Query Parameters**:
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)
- `username` - Filter by username (partial match)
- `role` - Filter by role (ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF)
- `enabled` - Filter by enabled status (true/false)

**Example**: `GET /api/users?role=DELIVERY_STAFF&enabled=true&page=0&size=10`

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "driver1",
        "email": "driver1@restaurant.com",
        "fullName": "Driver One",
        "role": "DELIVERY_STAFF",
        "enabled": true,
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Get User by ID

Get a specific user by ID.

**Endpoint**: `GET /api/users/{id}`

**Authorization**: ADMIN, MANAGER

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "User found",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@restaurant.com",
    "fullName": "System Administrator",
    "role": "ADMIN",
    "enabled": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `404 Not Found` - User not found

---

### Create User

Create a new user.

**Endpoint**: `POST /api/users`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "username": "newstaff",
  "password": "securePass123",
  "email": "staff@restaurant.com",
  "fullName": "Jane Smith",
  "role": "KITCHEN_STAFF",
  "enabled": true
}
```

**Response**: `201 Created`
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 6,
    "username": "newstaff",
    "email": "staff@restaurant.com",
    "fullName": "Jane Smith",
    "role": "KITCHEN_STAFF",
    "enabled": true
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Update User

Update an existing user.

**Endpoint**: `PUT /api/users/{id}`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "email": "updated@restaurant.com",
  "fullName": "Jane Smith Updated",
  "role": "MANAGER",
  "enabled": false
}
```

**Response**: `200 OK`

---

### Delete User

Delete a user.

**Endpoint**: `DELETE /api/users/{id}`

**Authorization**: ADMIN

**Response**: `204 No Content`

**Error Responses**:
- `404 Not Found` - User not found
- `409 Conflict` - Cannot delete user (e.g., has associated records)

---

## Category Management

### List Categories

Get all categories.

**Endpoint**: `GET /api/categories`

**Authorization**: Optional (public or authenticated)

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Appetizers",
      "description": "Starters and small bites",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    },
    {
      "id": 2,
      "name": "Main Courses",
      "description": "Main dishes",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Get Category by ID

Get a specific category.

**Endpoint**: `GET /api/categories/{id}`

**Authorization**: Optional

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Category found",
  "data": {
    "id": 1,
    "name": "Appetizers",
    "description": "Starters and small bites",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Create Category

Create a new category.

**Endpoint**: `POST /api/categories`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "name": "Desserts",
  "description": "Sweet treats and desserts"
}
```

**Response**: `201 Created`

---

### Update Category

Update an existing category.

**Endpoint**: `PUT /api/categories/{id}`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "name": "Desserts & Sweets",
  "description": "Updated description"
}
```

**Response**: `200 OK`

---

### Delete Category

Delete a category.

**Endpoint**: `DELETE /api/categories/{id}`

**Authorization**: ADMIN

**Response**: `204 No Content`

**Error Responses**:
- `409 Conflict` - Category has associated products

---

## Product Management

### List Products

Get paginated list of products with filtering.

**Endpoint**: `GET /api/products`

**Authorization**: Optional

**Query Parameters**:
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)
- `categoryId` - Filter by category
- `isAvailable` - Filter by availability (true/false)
- `minPrice` - Minimum price filter
- `maxPrice` - Maximum price filter
- `name` - Search by name (partial match)

**Example**: `GET /api/products?categoryId=1&isAvailable=true&page=0&size=20`

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Spring Rolls",
        "description": "Crispy vegetable spring rolls",
        "price": 5.99,
        "imageUrl": "http://example.com/spring-rolls.jpg",
        "isAvailable": true,
        "category": {
          "id": 1,
          "name": "Appetizers"
        },
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 17,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Get Available Products

Get only available products.

**Endpoint**: `GET /api/products/available`

**Authorization**: Optional

**Query Parameters**: Same as List Products

**Response**: `200 OK` (same format as List Products)

---

### Get Product by ID

Get a specific product.

**Endpoint**: `GET /api/products/{id}`

**Authorization**: Optional

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Product found",
  "data": {
    "id": 1,
    "name": "Spring Rolls",
    "description": "Crispy vegetable spring rolls (4 pieces)",
    "price": 5.99,
    "imageUrl": "http://example.com/spring-rolls.jpg",
    "isAvailable": true,
    "category": {
      "id": 1,
      "name": "Appetizers",
      "description": "Starters and small bites"
    },
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Create Product

Create a new product.

**Endpoint**: `POST /api/products`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "name": "Pad Thai",
  "description": "Traditional Thai stir-fried rice noodles",
  "price": 12.99,
  "imageUrl": "http://example.com/pad-thai.jpg",
  "isAvailable": true,
  "categoryId": 2
}
```

**Response**: `201 Created`
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 18,
    "name": "Pad Thai",
    "description": "Traditional Thai stir-fried rice noodles",
    "price": 12.99,
    "imageUrl": "http://example.com/pad-thai.jpg",
    "isAvailable": true,
    "category": {
      "id": 2,
      "name": "Main Courses"
    }
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `400 Bad Request` - Validation errors
- `404 Not Found` - Category not found
- `409 Conflict` - Product name already exists

---

### Update Product

Update an existing product.

**Endpoint**: `PUT /api/products/{id}`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "name": "Pad Thai Special",
  "description": "Traditional Thai stir-fried rice noodles with shrimp",
  "price": 14.99,
  "imageUrl": "http://example.com/pad-thai-special.jpg",
  "isAvailable": true,
  "categoryId": 2
}
```

**Response**: `200 OK`

---

### Delete Product

Delete a product.

**Endpoint**: `DELETE /api/products/{id}`

**Authorization**: ADMIN

**Response**: `204 No Content`

**Error Responses**:
- `409 Conflict` - Product is used in existing orders

---

## Order Management

### List Orders

Get paginated list of orders with filtering.

**Endpoint**: `GET /api/orders`

**Authorization**: ADMIN, MANAGER, KITCHEN_STAFF

**Query Parameters**:
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)
- `status` - Filter by status (PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, READY_FOR_DELIVERY, OUT_FOR_DELIVERY, COMPLETED, CANCELLED)
- `orderType` - Filter by type (DINE_IN, TAKEOUT, DELIVERY, PICKUP)
- `startDate` - Start date filter (ISO format)
- `endDate` - End date filter (ISO format)

**Example**: `GET /api/orders?status=PREPARING&page=0&size=20`

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "customerDetails": {
          "name": "John Customer",
          "phone": "+1234567890",
          "email": "customer@example.com"
        },
        "orderType": "DELIVERY",
        "status": "PREPARING",
        "totalPrice": 45.97,
        "items": [
          {
            "id": 1,
            "productId": 1,
            "productName": "Spring Rolls",
            "quantity": 2,
            "price": 5.99
          }
        ],
        "createdAt": "2024-01-15T09:00:00",
        "updatedAt": "2024-01-15T09:15:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2,
    "last": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Get Order by ID

Get a specific order with all details.

**Endpoint**: `GET /api/orders/{id}`

**Authorization**: ADMIN, MANAGER, KITCHEN_STAFF

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Order found",
  "data": {
    "id": 1,
    "customerDetails": {
      "name": "John Customer",
      "phone": "+1234567890",
      "email": "customer@example.com",
      "address": "123 Main St, City"
    },
    "orderType": "DELIVERY",
    "status": "PREPARING",
    "totalPrice": 45.97,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Spring Rolls",
        "quantity": 2,
        "price": 5.99
      },
      {
        "id": 2,
        "productId": 5,
        "productName": "Green Curry",
        "quantity": 1,
        "price": 13.99
      }
    ],
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-15T09:15:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Create Order

Create a new order.

**Endpoint**: `POST /api/orders`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "customerDetails": {
    "name": "Jane Customer",
    "phone": "+1234567890",
    "email": "jane@example.com",
    "address": "456 Oak Ave, City"
  },
  "orderType": "DELIVERY",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 5,
      "quantity": 1
    }
  ]
}
```

**Response**: `201 Created`
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 26,
    "customerDetails": {
      "name": "Jane Customer",
      "phone": "+1234567890",
      "email": "jane@example.com",
      "address": "456 Oak Ave, City"
    },
    "orderType": "DELIVERY",
    "status": "PENDING",
    "totalPrice": 25.97,
    "items": [
      {
        "id": 50,
        "productId": 1,
        "productName": "Spring Rolls",
        "quantity": 2,
        "price": 5.99
      },
      {
        "id": 51,
        "productId": 5,
        "productName": "Green Curry",
        "quantity": 1,
        "price": 13.99
      }
    ],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `400 Bad Request` - Validation errors, product not available
- `404 Not Found` - Product not found

---

### Update Order Status

Update the status of an order.

**Endpoint**: `PATCH /api/orders/{id}/status`

**Authorization**: ADMIN, MANAGER, KITCHEN_STAFF

**Request Body**:
```json
{
  "status": "PREPARING"
}
```

**Valid Status Transitions**:
- PENDING → CONFIRMED, CANCELLED
- CONFIRMED → PREPARING, CANCELLED
- PREPARING → READY_FOR_PICKUP, READY_FOR_DELIVERY, CANCELLED
- READY_FOR_PICKUP → COMPLETED, CANCELLED
- READY_FOR_DELIVERY → OUT_FOR_DELIVERY, CANCELLED
- OUT_FOR_DELIVERY → COMPLETED, CANCELLED

**Response**: `200 OK`

**Error Responses**:
- `400 Bad Request` - Invalid status transition
- `404 Not Found` - Order not found

---

### Get Kitchen Orders

Get orders that are CONFIRMED or PREPARING (for kitchen staff).

**Endpoint**: `GET /api/orders/kitchen`

**Authorization**: KITCHEN_STAFF, MANAGER, ADMIN

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Kitchen orders retrieved",
  "data": [
    {
      "id": 1,
      "customerDetails": { /* ... */ },
      "orderType": "DINE_IN",
      "status": "PREPARING",
      "totalPrice": 45.97,
      "items": [/* ... */],
      "createdAt": "2024-01-15T09:00:00",
      "updatedAt": "2024-01-15T09:15:00"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Get Today's Orders

Get all orders created today.

**Endpoint**: `GET /api/orders/today`

**Authorization**: ADMIN, MANAGER

**Response**: `200 OK`

---

### Get Orders by Date Range

Get orders within a specific date range.

**Endpoint**: `GET /api/orders/date-range`

**Authorization**: ADMIN, MANAGER

**Query Parameters**:
- `startDate` - Start date (ISO format, required)
- `endDate` - End date (ISO format, required)
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)

**Example**: `GET /api/orders/date-range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59&page=0&size=20`

**Response**: `200 OK`

---

### Get Today's Statistics

Get count and revenue statistics for today's orders.

**Endpoint**: `GET /api/orders/stats/today`

**Authorization**: ADMIN, MANAGER

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Statistics retrieved",
  "data": {
    "orderCount": 15,
    "totalRevenue": 589.85,
    "completedOrders": 12,
    "pendingOrders": 3,
    "date": "2024-01-15"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Delivery Management

### List Deliveries

Get all deliveries.

**Endpoint**: `GET /api/deliveries`

**Authorization**: ADMIN, MANAGER

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Deliveries retrieved successfully",
  "data": [
    {
      "id": 1,
      "orderId": 5,
      "driverId": 4,
      "driverName": "Driver One",
      "deliveryAddress": "123 Main St, City",
      "deliveryNotes": "Ring doorbell",
      "status": "OUT_FOR_DELIVERY",
      "dispatchedAt": "2024-01-15T10:00:00",
      "deliveredAt": null,
      "order": {
        "id": 5,
        "customerDetails": { /* ... */ },
        "totalPrice": 45.99
      }
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Get My Deliveries

Get deliveries assigned to the authenticated delivery staff.

**Endpoint**: `GET /api/deliveries/my`

**Authorization**: DELIVERY_STAFF

**Response**: `200 OK`

---

### Get Delivery by ID

Get a specific delivery.

**Endpoint**: `GET /api/deliveries/{id}`

**Authorization**: ADMIN, MANAGER, DELIVERY_STAFF (own deliveries only)

**Response**: `200 OK`

---

### Assign Delivery

Assign a delivery to a driver.

**Endpoint**: `POST /api/deliveries/assign`

**Authorization**: ADMIN, MANAGER

**Request Body**:
```json
{
  "orderId": 5,
  "driverId": 4,
  "deliveryAddress": "123 Main St, City, State 12345",
  "deliveryNotes": "Call upon arrival"
}
```

**Response**: `201 Created`
```json
{
  "success": true,
  "message": "Delivery assigned successfully",
  "data": {
    "id": 10,
    "orderId": 5,
    "driverId": 4,
    "driverName": "Driver One",
    "deliveryAddress": "123 Main St, City, State 12345",
    "deliveryNotes": "Call upon arrival",
    "status": "ASSIGNED",
    "dispatchedAt": "2024-01-15T10:30:00",
    "deliveredAt": null
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Responses**:
- `400 Bad Request` - Order not ready for delivery, driver not DELIVERY_STAFF role
- `404 Not Found` - Order or driver not found
- `409 Conflict` - Delivery already exists for order

---

### Update Delivery Status

Update the status of a delivery.

**Endpoint**: `PATCH /api/deliveries/{id}/status`

**Authorization**: ADMIN, MANAGER, DELIVERY_STAFF (own deliveries only)

**Request Body**:
```json
{
  "status": "DELIVERED"
}
```

**Valid Statuses**:
- PENDING
- ASSIGNED
- OUT_FOR_DELIVERY
- DELIVERED

**Response**: `200 OK`

---

### Get Deliveries by Status

Get deliveries filtered by status.

**Endpoint**: `GET /api/deliveries/status/{status}`

**Authorization**: ADMIN, MANAGER

**Example**: `GET /api/deliveries/status/OUT_FOR_DELIVERY`

**Response**: `200 OK`

---

## Testing with cURL

### Complete Workflow Example

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' \
  | jq -r '.data.token')

# 2. Get Products
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# 3. Create Order
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerDetails": {
      "name": "John Doe",
      "phone": "+1234567890",
      "email": "john@example.com"
    },
    "orderType": "DELIVERY",
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 5, "quantity": 1}
    ]
  }'

# 4. Update Order Status
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"PREPARING"}'

# 5. Assign Delivery
curl -X POST http://localhost:8080/api/deliveries/assign \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "driverId": 4,
    "deliveryAddress": "123 Main St",
    "deliveryNotes": "Ring doorbell"
  }'
```

## Rate Limiting

Currently no rate limiting is implemented. Consider implementing for production.

## Versioning

Currently API is unversioned. Consider adding version prefix (e.g., `/api/v1/`) for production.

## Additional Resources

- Interactive API documentation: http://localhost:8080/swagger-ui.html
- OpenAPI specification: http://localhost:8080/api-docs
- WebSocket documentation: [WEBSOCKET.md](./WEBSOCKET.md)
