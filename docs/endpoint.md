# API Endpoints Documentation

This document provides comprehensive information about all API endpoints in the ResAdmin system, including request/response structures, access control, and descriptions.

## Table of Contents
- [Authentication Endpoints](#authentication-endpoints)
- [User Management Endpoints](#user-management-endpoints)
- [Category Management Endpoints](#category-management-endpoints)
- [Product Management Endpoints](#product-management-endpoints)
- [Order Management Endpoints](#order-management-endpoints)
- [Delivery Management Endpoints](#delivery-management-endpoints)
- [WebSocket Endpoints](#websocket-endpoints)
- [Common Response Structure](#common-response-structure)
- [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)

---

## Authentication Endpoints

**Base Path**: `/api/auth`

### POST /api/auth/login
**Description**: Authenticate user and receive JWT token  
**Access Control**: Public  
**Request Body**:
```json
{
  "username": "string",
  "password": "string"
}
```
**Response**: `ApiResponseDTO<TokenResponse>`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "jwt_token_string",
    "type": "Bearer",
    "expiresIn": 86400
  }
}
```

### POST /api/auth/register
**Description**: Register a new user account  
**Access Control**: Public  
**Request Body**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "fullName": "string"
}
```
**Response**: `ApiResponseDTO<UserDTO>`

### POST /api/auth/change-password
**Description**: Change user password  
**Access Control**: Authenticated users  
**Security**: Bearer Authentication required  
**Request Body**:
```json
{
  "currentPassword": "string",
  "newPassword": "string"
}
```
**Response**: `ApiResponseDTO<String>`

### GET /api/auth/info
**Description**: Get current user information  
**Access Control**: Authenticated users  
**Security**: Bearer Authentication required  
**Response**: `ApiResponseDTO<UserDTO>`

---

## User Management Endpoints

**Base Path**: `/api/users`  
**Class-level Access Control**: `ADMIN` or `MANAGER` roles

### GET /api/users
**Description**: Get paginated list of users with optional filtering  
**Access Control**: `ADMIN` or `MANAGER`  
**Query Parameters**:
- `page` (int, default: 0): Page number (0-based)
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "username"): Sort field
- `sortDir` (string, default: "asc"): Sort direction (asc/desc)
- `role` (string, optional): Filter by user role
- `enabled` (boolean, optional): Filter by enabled status
- `search` (string, optional): Search by username or full name

**Response**: `ApiResponseDTO<PagedResponseDTO<UserDTO>>`

### GET /api/users/{id}
**Description**: Get user by ID  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - User ID  
**Response**: `ApiResponseDTO<UserDTO>`

### GET /api/users/username/{username}
**Description**: Get user by username  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `username` (String) - Username  
**Response**: `ApiResponseDTO<UserDTO>`

### GET /api/users/role/{role}
**Description**: Get users by role  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `role` (User.Role) - User role  
**Response**: `ApiResponseDTO<List<UserDTO>>`

### GET /api/users/enabled/{enabled}
**Description**: Get users by enabled status  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `enabled` (Boolean) - Enabled status  
**Response**: `ApiResponseDTO<List<UserDTO>>`

### GET /api/users/delivery-staff/available
**Description**: Get available delivery staff  
**Access Control**: `ADMIN` or `MANAGER`  
**Response**: `ApiResponseDTO<List<UserDTO>>`

### PATCH /api/users/{id}/role
**Description**: Update user role  
**Access Control**: `ADMIN` only  
**Path Parameters**: `id` (Long) - User ID  
**Request Body**: `UpdateRoleRequestDTO`
```json
{
  "role": "ADMIN|MANAGER|KITCHEN_STAFF|DELIVERY_STAFF|USER"
}
```
**Response**: `ApiResponseDTO<UserDTO>`

### PATCH /api/users/{id}/toggle-status
**Description**: Toggle user enabled status  
**Access Control**: `ADMIN` only  
**Path Parameters**: `id` (Long) - User ID  
**Response**: `ApiResponseDTO<UserDTO>`

### DELETE /api/users/{id}
**Description**: Delete user  
**Access Control**: `ADMIN` only  
**Path Parameters**: `id` (Long) - User ID  
**Response**: `ApiResponseDTO<Void>`

---

## Category Management Endpoints

**Base Path**: `/api/categories`

### GET /api/categories
**Description**: Get paginated list of categories with optional filtering  
**Access Control**: Public (authenticated users)  
**Query Parameters**:
- `page` (int, default: 0): Page number (0-based)
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "name"): Sort field
- `sortDir` (string, default: "asc"): Sort direction (asc/desc)
- `name` (string, optional): Filter by name (contains)

**Response**: `ApiResponseDTO<PagedResponseDTO<CategoryDTO>>`

### GET /api/categories/{id}
**Description**: Get category by ID  
**Access Control**: Public (authenticated users)  
**Path Parameters**: `id` (Long) - Category ID  
**Response**: `ApiResponseDTO<CategoryDTO>`

### POST /api/categories
**Description**: Create new category  
**Access Control**: `ADMIN` or `MANAGER`  
**Request Body**: `Category`
```json
{
  "name": "string",
  "description": "string"
}
```
**Response**: `ApiResponseDTO<CategoryDTO>`

### PUT /api/categories/{id}
**Description**: Update category  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Category ID  
**Request Body**: `Category`  
**Response**: `ApiResponseDTO<CategoryDTO>`

### DELETE /api/categories/{id}
**Description**: Delete category  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Category ID  
**Response**: `ApiResponseDTO<Void>`

### GET /api/categories/search
**Description**: Search categories by name  
**Access Control**: Public (authenticated users)  
**Query Parameters**: `name` (string) - Search term  
**Response**: `ApiResponseDTO<List<CategoryDTO>>`

### GET /api/categories/exists/{name}
**Description**: Check if category exists by name  
**Access Control**: Public (authenticated users)  
**Path Parameters**: `name` (String) - Category name  
**Response**: `ApiResponseDTO<Boolean>`

---

## Product Management Endpoints

**Base Path**: `/api/products`

### GET /api/products
**Description**: Get paginated list of products with optional filtering  
**Access Control**: Public (authenticated users)  
**Query Parameters**:
- `page` (int, default: 0): Page number (0-based)
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "name"): Sort field
- `sortDir` (string, default: "asc"): Sort direction (asc/desc)
- `category` (string, optional): Filter by category
- `available` (boolean, optional): Filter by availability
- `name` (string, optional): Filter by name (contains)
- `minPrice` (BigDecimal, optional): Filter by minimum price
- `maxPrice` (BigDecimal, optional): Filter by maximum price

**Response**: `ApiResponseDTO<PagedResponseDTO<ProductDTO>>`

### GET /api/products/available
**Description**: Get all available products  
**Access Control**: Public (authenticated users)  
**Response**: `ApiResponseDTO<List<ProductDTO>>`

### GET /api/products/{id}
**Description**: Get product by ID  
**Access Control**: Public (authenticated users)  
**Path Parameters**: `id` (Long) - Product ID  
**Response**: `ApiResponseDTO<ProductDTO>`

### GET /api/products/category/{categoryId}
**Description**: Get products by category  
**Access Control**: Public (authenticated users)  
**Path Parameters**: `categoryId` (Long) - Category ID  
**Response**: `ApiResponseDTO<List<ProductDTO>>`

### GET /api/products/category/{categoryId}/available
**Description**: Get available products by category  
**Access Control**: Public (authenticated users)  
**Path Parameters**: `categoryId` (Long) - Category ID  
**Response**: `ApiResponseDTO<List<ProductDTO>>`

### POST /api/products
**Description**: Create new product  
**Access Control**: `ADMIN` or `MANAGER`  
**Request Body**: `Product`
```json
{
  "name": "string",
  "description": "string",
  "price": "number",
  "isAvailable": "boolean",
  "category": { "id": "number" },
  "imageUrl": "string"
}
```
**Response**: `ApiResponseDTO<ProductDTO>`

### PUT /api/products/{id}
**Description**: Update product  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Product ID  
**Request Body**: `Product`  
**Response**: `ApiResponseDTO<ProductDTO>`

### PATCH /api/products/{id}/toggle-availability
**Description**: Toggle product availability  
**Access Control**: `ADMIN`, `MANAGER`, or `KITCHEN_STAFF`  
**Path Parameters**: `id` (Long) - Product ID  
**Response**: `ApiResponseDTO<ProductDTO>`

### DELETE /api/products/{id}
**Description**: Delete product  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Product ID  
**Response**: `ApiResponseDTO<Void>`

### GET /api/products/search
**Description**: Search products by name  
**Access Control**: Public (authenticated users)  
**Query Parameters**: `name` (string) - Search term  
**Response**: `ApiResponseDTO<List<ProductDTO>>`

### GET /api/products/price-range
**Description**: Get products by price range  
**Access Control**: Public (authenticated users)  
**Query Parameters**:
- `minPrice` (BigDecimal) - Minimum price
- `maxPrice` (BigDecimal) - Maximum price

**Response**: `ApiResponseDTO<List<ProductDTO>>`

### GET /api/products/exists/{name}
**Description**: Check if product exists by name  
**Access Control**: Public (authenticated users)  
**Path Parameters**: `name` (String) - Product name  
**Response**: `ApiResponseDTO<Boolean>`

---

## Order Management Endpoints

**Base Path**: `/api/orders`

### GET /api/orders
**Description**: Get paginated list of orders with optional filtering  
**Access Control**: `ADMIN`, `MANAGER`, `KITCHEN_STAFF`, or `DELIVERY_STAFF`  
**Query Parameters**:
- `page` (int, default: 0): Page number (0-based)
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "createdAt"): Sort field
- `sortDir` (string, default: "desc"): Sort direction (asc/desc)
- `status` (Order.OrderStatus, optional): Filter by order status
- `orderType` (Order.OrderType, optional): Filter by order type
- `from` (LocalDateTime, optional): Filter from date (ISO format)
- `to` (LocalDateTime, optional): Filter to date (ISO format)

**Response**: `ApiResponseDTO<PagedResponseDTO<OrderDTO>>`

### GET /api/orders/{id}
**Description**: Get order by ID  
**Access Control**: `ADMIN`, `MANAGER`, `KITCHEN_STAFF`, or `DELIVERY_STAFF`  
**Path Parameters**: `id` (Long) - Order ID  
**Response**: `ApiResponseDTO<OrderDTO>`

### GET /api/orders/status/{status}
**Description**: Get orders by status with pagination  
**Access Control**: `ADMIN`, `MANAGER`, `KITCHEN_STAFF`, or `DELIVERY_STAFF`  
**Path Parameters**: `status` (Order.OrderStatus) - Order status  
**Query Parameters**: Same pagination parameters as GET /api/orders  
**Response**: `ApiResponseDTO<PagedResponseDTO<OrderDTO>>`

### GET /api/orders/kitchen
**Description**: Get orders for kitchen processing  
**Access Control**: `ADMIN`, `MANAGER`, or `KITCHEN_STAFF`  
**Response**: `ApiResponseDTO<List<OrderDTO>>`

### GET /api/orders/ready-for-delivery
**Description**: Get orders ready for delivery  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Response**: `ApiResponseDTO<List<OrderDTO>>`

### GET /api/orders/today
**Description**: Get today's orders  
**Access Control**: `ADMIN` or `MANAGER`  
**Response**: `ApiResponseDTO<List<OrderDTO>>`

### POST /api/orders
**Description**: Create new order  
**Access Control**: Public (authenticated users)  
**Request Body**: `CreateOrderRequestDTO`
```json
{
  "customerDetails": "string",
  "totalPrice": "number",
  "orderType": "DINE_IN|TAKEAWAY|DELIVERY",
  "orderItems": [
    {
      "productId": "number",
      "quantity": "number"
    }
  ]
}
```
**Response**: `ApiResponseDTO<OrderDTO>` (HTTP 201)

### PUT /api/orders/{id}
**Description**: Update order  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Order ID  
**Request Body**: `CreateOrderRequestDTO`  
**Response**: `ApiResponseDTO<OrderDTO>`

### PATCH /api/orders/{id}/status
**Description**: Update order status  
**Access Control**: `ADMIN`, `MANAGER`, `KITCHEN_STAFF`, or `DELIVERY_STAFF`  
**Path Parameters**: `id` (Long) - Order ID  
**Request Body**: `UpdateOrderStatusRequestDTO`
```json
{
  "status": "PENDING|CONFIRMED|PREPARING|READY|OUT_FOR_DELIVERY|DELIVERED|CANCELLED"
}
```
**Response**: `ApiResponseDTO<OrderDTO>`

### DELETE /api/orders/{id}
**Description**: Delete order  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Order ID  
**Response**: `ApiResponseDTO<Void>`

### GET /api/orders/date-range
**Description**: Get orders by date range with pagination  
**Access Control**: `ADMIN` or `MANAGER`  
**Query Parameters**:
- `startDate` (LocalDateTime) - Start date (ISO format)
- `endDate` (LocalDateTime) - End date (ISO format)
- Standard pagination parameters

**Response**: `ApiResponseDTO<PagedResponseDTO<OrderDTO>>`

### GET /api/orders/{id}/items
**Description**: Get order items for specific order  
**Access Control**: Public (authenticated users)  
**Path Parameters**: `id` (Long) - Order ID  
**Response**: `ApiResponseDTO<List<OrderItemDTO>>`

### GET /api/orders/stats/today
**Description**: Get today's order statistics  
**Access Control**: `ADMIN` or `MANAGER`  
**Response**: `ApiResponseDTO<StatsResponseDTO>`
```json
{
  "success": true,
  "message": "Today's statistics retrieved successfully",
  "data": {
    "orderCount": "number",
    "revenue": "number"
  }
}
```

---

## Delivery Management Endpoints

**Base Path**: `/api/deliveries`  
**Class-level Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`

### GET /api/deliveries
**Description**: Get all deliveries  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Response**: `List<Delivery>`

### GET /api/deliveries/{id}
**Description**: Get delivery by ID  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Path Parameters**: `id` (Long) - Delivery ID  
**Response**: `ApiResponseDTO<DeliveryDTO>`

### GET /api/deliveries/order/{orderId}
**Description**: Get delivery by order ID  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Path Parameters**: `orderId` (Long) - Order ID  
**Response**: `ApiResponseDTO<DeliveryDTO>`

### GET /api/deliveries/driver/{driverId}
**Description**: Get deliveries by driver  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Path Parameters**: `driverId` (Long) - Driver ID  
**Response**: `List<Delivery>`

### GET /api/deliveries/status/{status}
**Description**: Get deliveries by status  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Path Parameters**: `status` (Delivery.DeliveryStatus) - Delivery status  
**Response**: `List<Delivery>`

### GET /api/deliveries/pending
**Description**: Get pending deliveries  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Response**: `List<Delivery>`

### GET /api/deliveries/active
**Description**: Get active deliveries  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Response**: `List<Delivery>`

### GET /api/deliveries/today
**Description**: Get today's deliveries  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Response**: `List<Delivery>`

### POST /api/deliveries/assign
**Description**: Assign delivery to driver  
**Access Control**: `ADMIN` or `MANAGER`  
**Request Body**: `AssignDeliveryRequestDTO`
```json
{
  "orderId": "number",
  "driverId": "number",
  "deliveryAddress": "string",
  "deliveryNotes": "string"
}
```
**Response**: `ApiResponseDTO<DeliveryDTO>`

### PATCH /api/deliveries/{id}/status
**Description**: Update delivery status  
**Access Control**: `ADMIN`, `MANAGER`, or `DELIVERY_STAFF`  
**Path Parameters**: `id` (Long) - Delivery ID  
**Request Body**: `UpdateDeliveryStatusRequestDTO`
```json
{
  "status": "PENDING|ASSIGNED|PICKED_UP|OUT_FOR_DELIVERY|DELIVERED|CANCELLED"
}
```
**Response**: `ApiResponseDTO<DeliveryDTO>`

### PUT /api/deliveries/{id}
**Description**: Update delivery details  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Delivery ID  
**Request Body**: `UpdateDeliveryRequestDTO`
```json
{
  "deliveryAddress": "string",
  "deliveryNotes": "string"
}
```
**Response**: `ApiResponseDTO<DeliveryDTO>`

### PATCH /api/deliveries/{id}/reassign
**Description**: Reassign delivery to different driver  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Delivery ID  
**Request Body**: `ReassignDriverRequestDTO`
```json
{
  "newDriverId": "number"
}
```
**Response**: `ApiResponseDTO<DeliveryDTO>`

### DELETE /api/deliveries/{id}
**Description**: Cancel delivery  
**Access Control**: `ADMIN` or `MANAGER`  
**Path Parameters**: `id` (Long) - Delivery ID  
**Response**: `ApiResponseDTO<String>`

### GET /api/deliveries/date-range
**Description**: Get deliveries by date range  
**Access Control**: `ADMIN` or `MANAGER`  
**Query Parameters**:
- `startDate` (LocalDateTime) - Start date (ISO format)
- `endDate` (LocalDateTime) - End date (ISO format)

**Response**: `List<Delivery>`

### GET /api/deliveries/drivers/available
**Description**: Get available drivers  
**Access Control**: `ADMIN` or `MANAGER`  
**Response**: `List<User>`

### GET /api/deliveries/stats
**Description**: Get delivery statistics  
**Access Control**: `ADMIN` or `MANAGER`  
**Response**: `ApiResponseDTO<StatsResponseDTO>`
```json
{
  "success": true,
  "message": "Delivery statistics retrieved successfully",
  "data": {
    "completedCount": "number",
    "activeCount": "number"
  }
}
```

---

## WebSocket Endpoints

**Base Path**: WebSocket connection to `/ws`

### Message Mappings

#### /app/message → /topic/messages
**Description**: Handle general messages and broadcast to all subscribers  
**Access Control**: Authenticated users  
**Input**: `WebSocketMessageDTO`  
**Output**: Broadcast to `/topic/messages`

#### /app/private
**Description**: Handle private messages and send to specific user  
**Access Control**: Authenticated users  
**Input**: `WebSocketMessageDTO`  
**Output**: Send to user's `/queue/private`

#### /app/order → /topic/orders
**Description**: Handle order updates and broadcast to all subscribers  
**Access Control**: Authenticated users  
**Input**: `WebSocketMessageDTO`  
**Output**: Broadcast to `/topic/orders`

#### /app/delivery → /topic/deliveries
**Description**: Handle delivery updates and broadcast to all subscribers  
**Access Control**: Authenticated users  
**Input**: `WebSocketMessageDTO`  
**Output**: Broadcast to `/topic/deliveries`

### Subscription Topics

- `/topic/messages` - General messages
- `/topic/orders` - Order status updates
- `/topic/deliveries` - Delivery status updates
- `/topic/notifications` - Global notifications
- `/queue/private` - Private messages (user-specific)
- `/queue/notifications` - User-specific notifications

---

## Common Response Structure

All REST API endpoints return responses wrapped in `ApiResponseDTO<T>`:

```json
{
  "success": "boolean",
  "message": "string",
  "data": "T (generic type)",
  "timestamp": "LocalDateTime (optional)"
}
```

### Paginated Responses

Endpoints returning paginated data use `PagedResponseDTO<T>`:

```json
{
  "content": "List<T>",
  "page": "number",
  "size": "number",
  "totalElements": "number",
  "totalPages": "number",
  "first": "boolean",
  "last": "boolean",
  "hasNext": "boolean",
  "hasPrevious": "boolean"
}
```

---

## Data Transfer Objects (DTOs)

### UserDTO
```json
{
  "id": "number",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "role": "ADMIN|MANAGER|KITCHEN_STAFF|DELIVERY_STAFF|USER",
  "enabled": "boolean",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

### CategoryDTO
```json
{
  "id": "number",
  "name": "string",
  "description": "string",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

### ProductDTO
```json
{
  "id": "number",
  "name": "string",
  "description": "string",
  "price": "BigDecimal",
  "available": "boolean",
  "category": "CategoryDTO",
  "imageUrl": "string",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

### OrderDTO
```json
{
  "id": "number",
  "customerDetails": "string",
  "status": "PENDING|CONFIRMED|PREPARING|READY|OUT_FOR_DELIVERY|DELIVERED|CANCELLED",
  "totalPrice": "BigDecimal",
  "orderType": "DINE_IN|TAKEAWAY|DELIVERY",
  "createdAt": "LocalDateTime",
  "orderItems": "List<OrderItemDTO>",
  "delivery": "DeliveryDTO (optional)"
}
```

### OrderItemDTO
```json
{
  "id": "number",
  "orderId": "number",
  "product": "ProductDTO",
  "quantity": "number",
  "price": "BigDecimal"
}
```

### DeliveryDTO
```json
{
  "id": "number",
  "orderId": "number",
  "driver": "UserDTO",
  "status": "PENDING|ASSIGNED|PICKED_UP|OUT_FOR_DELIVERY|DELIVERED|CANCELLED",
  "dispatchedAt": "LocalDateTime",
  "deliveredAt": "LocalDateTime",
  "deliveryAddress": "string",
  "deliveryNotes": "string"
}
```

### StatsResponseDTO
```json
{
  "orderCount": "number (optional)",
  "revenue": "BigDecimal (optional)",
  "completedCount": "number (optional)",
  "activeCount": "number (optional)"
}
```

### WebSocketMessageDTO
```json
{
  "type": "string",
  "message": "string",
  "data": "object (optional)",
  "userId": "string (optional)",
  "timestamp": "LocalDateTime"
}
```

---

## Security Notes

1. **Authentication**: Most endpoints require JWT Bearer token authentication
2. **Authorization**: Role-based access control using `@PreAuthorize` annotations
3. **CORS**: All controllers allow cross-origin requests with `@CrossOrigin(origins = "*")`
4. **Validation**: Request bodies are validated using Jakarta Bean Validation annotations
5. **Error Handling**: Consistent error responses using `ResourceNotFoundException` and global exception handling

## HTTP Status Codes

- **200 OK**: Successful GET, PUT, PATCH, DELETE operations
- **201 Created**: Successful POST operations (resource creation)
- **400 Bad Request**: Invalid request data or validation errors
- **401 Unauthorized**: Missing or invalid authentication token
- **403 Forbidden**: Insufficient permissions for the requested operation
- **404 Not Found**: Requested resource not found
- **500 Internal Server Error**: Server-side errors

---

*This documentation is generated based on the current API implementation and should be updated when endpoints are modified or added.*