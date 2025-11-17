# Data Models Reference

Complete reference for all data structures, DTOs, and validation rules.

## Table of Contents

1. [Entity Models](#entity-models)
2. [Request DTOs](#request-dtos)
3. [Response DTOs](#response-dtos)
4. [Validation Rules](#validation-rules)
5. [Enumerations](#enumerations)
6. [Relationships](#relationships)

---

## Entity Models

### User

**Description**: System user with role-based access

**Properties**:
```json
{
  "id": 1,
  "username": "admin",
  "email": "[email protected]",
  "fullName": "System Administrator",
  "role": "ADMIN",
  "enabled": true,
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-01T00:00:00"
}
```

**Field Details**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| id | Long | Yes (auto) | Primary key | Unique identifier |
| username | String | Yes | 3-50 chars, unique | Login username |
| password | String | Yes | Min 6 chars | BCrypt encrypted |
| email | String | No | Max 100 chars | Email address |
| fullName | String | No | Max 100 chars | Full display name |
| role | Enum | Yes | See UserRole | User role |
| enabled | Boolean | Yes | Default: true | Account status |
| createdAt | LocalDateTime | Yes (auto) | Auto-generated | Creation timestamp |
| updatedAt | LocalDateTime | Yes (auto) | Auto-updated | Last update timestamp |

**Note**: Password field is never returned in API responses

---

### Category

**Description**: Product category for menu organization

**Properties**:
```json
{
  "id": 1,
  "name": "Appetizers",
  "description": "Starters and small bites",
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-01T00:00:00"
}
```

**Field Details**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| id | Long | Yes (auto) | Primary key | Unique identifier |
| name | String | Yes | Max 100 chars, unique | Category name |
| description | String | No | Max 500 chars | Category description |
| createdAt | LocalDateTime | Yes (auto) | Auto-generated | Creation timestamp |
| updatedAt | LocalDateTime | Yes (auto) | Auto-updated | Last update timestamp |

---

### Product

**Description**: Menu item/product

**Properties**:
```json
{
  "id": 10,
  "name": "Grilled Salmon",
  "description": "Fresh Atlantic salmon with herbs and lemon",
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
```

**Field Details**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| id | Long | Yes (auto) | Primary key | Unique identifier |
| name | String | Yes | Max 100 chars | Product name |
| description | String | No | Max 1000 chars | Product description |
| price | BigDecimal | Yes | Positive, scale 2 | Product price |
| imageUrl | String | No | Valid URL | Product image URL |
| isAvailable | Boolean | Yes | Default: true | Availability status |
| category | Category | Yes | Must exist | Associated category |
| createdAt | LocalDateTime | Yes (auto) | Auto-generated | Creation timestamp |
| updatedAt | LocalDateTime | Yes (auto) | Auto-updated | Last update timestamp |

**Important**: Price uses BigDecimal with precision 10, scale 2 for accurate financial calculations

---

### Order

**Description**: Customer order

**Properties**:
```json
{
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
}
```

**Field Details**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| id | Long | Yes (auto) | Primary key | Unique identifier |
| customerDetails | String | Yes | Max 500 chars | Customer info (name, phone, address) |
| status | Enum | Yes | See OrderStatus | Order status |
| totalPrice | BigDecimal | Yes | Positive, scale 2 | Total order price |
| orderType | Enum | Yes | See OrderType | Type of order |
| createdAt | LocalDateTime | Yes (auto) | Auto-generated | Order creation time |
| orderItems | List<OrderItem> | Yes | Min 1 item | Order line items |
| delivery | Delivery | No | For DELIVERY type | Associated delivery |

---

### OrderItem

**Description**: Line item in an order

**Properties**:
```json
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
}
```

**Field Details**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| id | Long | Yes (auto) | Primary key | Unique identifier |
| product | Product | Yes | Must exist | Referenced product |
| quantity | Integer | Yes | Min 1 | Quantity ordered |
| price | BigDecimal | Yes | Positive, scale 2 | Unit price at order time |
| totalPrice | BigDecimal | Computed | quantity × price | Total line item price |

**Note**: Price is captured at order time to preserve historical pricing

---

### Delivery

**Description**: Delivery assignment for an order

**Properties**:
```json
{
  "id": 50,
  "order": {
    "id": 101,
    "customerDetails": "John Doe, +1234567890",
    "totalPrice": 45.97
  },
  "driver": {
    "id": 5,
    "username": "driver1",
    "fullName": "Mike Driver",
    "email": "[email protected]"
  },
  "status": "OUT_FOR_DELIVERY",
  "dispatchedAt": "2025-11-13T09:10:00",
  "deliveredAt": null,
  "deliveryAddress": "123 Main St, City, State 12345",
  "deliveryNotes": "Ring doorbell. Gate code: 1234. Leave at front door if no answer."
}
```

**Field Details**:
| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| id | Long | Yes (auto) | Primary key | Unique identifier |
| order | Order | Yes | Must exist | Associated order |
| driver | User | No | Role: DELIVERY_STAFF | Assigned driver |
| status | Enum | Yes | See DeliveryStatus | Delivery status |
| dispatchedAt | LocalDateTime | Yes (auto) | Auto-generated | Dispatch timestamp |
| deliveredAt | LocalDateTime | No | Set on completion | Delivery completion time |
| deliveryAddress | String | Yes | Max 500 chars | Delivery address |
| deliveryNotes | String | No | Max 1000 chars | Special instructions |

---

## Request DTOs

### LoginRequestDTO

**Endpoint**: `POST /api/auth/login`

```json
{
  "username": "admin",
  "password": "password123"
}
```

**Validation**:
- `username`: Required, not blank
- `password`: Required, not blank, min 6 characters

---

### RegisterRequestDTO

**Endpoint**: `POST /api/auth/register`

```json
{
  "username": "newuser",
  "password": "securePassword123",
  "email": "[email protected]",
  "fullName": "John Doe",
  "role": "DELIVERY_STAFF"
}
```

**Validation**:
- `username`: Required, 3-50 characters, unique
- `password`: Required, min 6 characters
- `email`: Optional, valid email format, max 100 characters
- `fullName`: Optional, max 100 characters
- `role`: Required, valid UserRole enum value

---

### ChangePasswordRequestDTO

**Endpoint**: `POST /api/auth/change-password`

```json
{
  "oldPassword": "currentPassword",
  "newPassword": "newSecurePassword123"
}
```

**Validation**:
- `oldPassword`: Required, not blank
- `newPassword`: Required, min 6 characters, different from old password

---

### CreateCategoryRequestDTO

**Endpoint**: `POST /api/categories`

```json
{
  "name": "Desserts",
  "description": "Sweet treats and after-dinner delights"
}
```

**Validation**:
- `name`: Required, max 100 characters, unique
- `description`: Optional, max 500 characters

---

### CreateProductRequestDTO

**Endpoint**: `POST /api/products`

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

**Validation**:
- `name`: Required, max 100 characters
- `description`: Optional, max 1000 characters
- `price`: Required, positive number, max 2 decimal places
- `imageUrl`: Optional, valid URL format
- `isAvailable`: Optional, boolean, default true
- `categoryId`: Required, must reference existing category

---

### CreateOrderRequestDTO

**Endpoint**: `POST /api/orders`

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

**Validation**:
- `customerName`: Required, max 100 characters
- `customerPhone`: Required, max 20 characters
- `customerAddress`: Required for DELIVERY type, max 500 characters
- `notes`: Optional, max 1000 characters
- `totalAmount`: Required, positive
- `orderType`: Required, valid OrderType enum
- `items`: Required, min 1 item
- `items[].productId`: Required, must exist
- `items[].quantity`: Required, min 1
- `items[].price`: Required, must match current product price

---

### UpdateOrderStatusRequestDTO

**Endpoint**: `PATCH /api/orders/{id}/status`

```json
{
  "status": "PREPARING"
}
```

**Validation**:
- `status`: Required, valid OrderStatus enum value

---

### AssignDeliveryRequestDTO

**Endpoint**: `POST /api/deliveries/assign`

```json
{
  "orderId": 101,
  "driverId": 5,
  "deliveryAddress": "123 Main St, City, State 12345",
  "deliveryNotes": "Ring doorbell, gate code: 1234"
}
```

**Validation**:
- `orderId`: Required, must exist, must be DELIVERY type
- `driverId`: Required, must exist, must be DELIVERY_STAFF role, must be enabled
- `deliveryAddress`: Required, max 500 characters
- `deliveryNotes`: Optional, max 1000 characters

---

### UpdateDeliveryStatusRequestDTO

**Endpoint**: `PATCH /api/deliveries/{id}/status`

```json
{
  "status": "OUT_FOR_DELIVERY"
}
```

**Validation**:
- `status`: Required, valid DeliveryStatus enum value

---

## Response DTOs

### ApiResponseDTO<T>

**Description**: Standard wrapper for all API responses

**Success Response**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* T - actual data */ },
  "timestamp": "2025-11-13T10:30:00"
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Error summary",
  "error": "Detailed error message",
  "data": null,
  "timestamp": "2025-11-13T10:30:00"
}
```

**Fields**:
- `success`: Boolean indicating success/failure
- `message`: Human-readable message
- `data`: Response payload (generic type T)
- `error`: Error details (only on failure)
- `timestamp`: Response timestamp (ISO 8601)

---

### PagedResponseDTO<T>

**Description**: Wrapper for paginated list responses

```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": {
    "content": [ /* array of T */ ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  },
  "timestamp": "2025-11-13T10:30:00"
}
```

**Pagination Fields**:
- `content`: Array of items (type T)
- `page`: Current page number (0-indexed)
- `size`: Items per page
- `totalElements`: Total number of items across all pages
- `totalPages`: Total number of pages
- `last`: Boolean indicating if this is the last page

---

### TokenResponse

**Description**: JWT token response after login

```json
{
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
}
```

**Fields**:
- `token`: JWT authentication token (valid for 24 hours)
- `user`: User object with profile information

---

### StatsResponseDTO

**Description**: Statistics response for dashboard

```json
{
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
}
```

---

## Validation Rules

### Common Validation

**String Fields**:
- Trimmed automatically
- Null vs Empty: Nullable fields can be null, but if provided must not be blank
- Length constraints enforced server-side

**Numeric Fields**:
- Must be positive for prices and quantities
- BigDecimal used for monetary values (precision 10, scale 2)
- Integer for counts and IDs

**Enums**:
- Must match exactly (case-sensitive)
- Invalid values result in 400 Bad Request

**Timestamps**:
- ISO 8601 format: `YYYY-MM-DDThh:mm:ss`
- Timezone: Asia/Phnom_Penh
- Auto-generated fields cannot be set by client

---

### Field-Specific Rules

**Username**:
- Length: 3-50 characters
- Must be unique
- Cannot be changed after registration
- Case-sensitive

**Password**:
- Minimum: 6 characters (consider 8+ in production)
- Encrypted with BCrypt
- Never returned in responses
- Should include mix of characters (client-side recommendation)

**Email**:
- Must be valid email format
- Maximum 100 characters
- Not required to be unique
- Optional field

**Phone Numbers**:
- Maximum 20 characters
- Format not enforced (international support)
- Should include country code

**Prices**:
- Must be positive
- Maximum 2 decimal places
- Stored as BigDecimal for precision
- Example: 12.99, 100.00, 5.50

**Quantities**:
- Must be at least 1
- Integer only
- Maximum not enforced

---

## Enumerations

### UserRole

**Values**:
- `ADMIN` - System administrator with full access
- `MANAGER` - Restaurant manager with management access
- `KITCHEN_STAFF` - Kitchen operations staff
- `DELIVERY_STAFF` - Delivery drivers

**Usage**:
```json
{
  "role": "DELIVERY_STAFF"
}
```

**Permissions Matrix**:
| Operation | ADMIN | MANAGER | KITCHEN_STAFF | DELIVERY_STAFF |
|-----------|-------|---------|---------------|----------------|
| User Management | Full | View, Update | - | - |
| Product Management | Full | Full | View, Toggle | - |
| Order Management | Full | Full | View, Update Status | View Assigned |
| Delivery Management | Full | Full | - | View, Update Own |

---

### OrderStatus

**Values**:
- `PENDING` - Order received, awaiting confirmation
- `CONFIRMED` - Order confirmed, ready for kitchen
- `PREPARING` - Kitchen preparing order
- `READY_FOR_PICKUP` - Ready for customer pickup
- `READY_FOR_DELIVERY` - Ready for delivery assignment
- `OUT_FOR_DELIVERY` - Driver en route to customer
- `COMPLETED` - Order delivered/completed
- `CANCELLED` - Order cancelled

**Status Flow**:
```
PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP / READY_FOR_DELIVERY
                                              ↓
                                    OUT_FOR_DELIVERY (delivery only)
                                              ↓
                                         COMPLETED

Any status can transition to → CANCELLED
```

---

### OrderType

**Values**:
- `DINE_IN` - Customer dining in restaurant
- `TAKEOUT` - Customer pickup at counter
- `DELIVERY` - Home/office delivery
- `PICKUP` - Curbside pickup

**Usage**:
```json
{
  "orderType": "DELIVERY"
}
```

**Validation**:
- DELIVERY type requires customer address
- OUT_FOR_DELIVERY status only valid for DELIVERY type

---

### DeliveryStatus

**Values**:
- `PENDING` - Awaiting driver assignment
- `ASSIGNED` - Driver assigned, not started
- `OUT_FOR_DELIVERY` - Driver en route
- `DELIVERED` - Successfully delivered
- `CANCELLED` - Delivery cancelled

**Status Flow**:
```
PENDING → ASSIGNED → OUT_FOR_DELIVERY → DELIVERED

Any status can transition to → CANCELLED
```

---

## Relationships

### Entity Relationship Diagram

```
┌─────────────┐
│    User     │
│  (Roles)    │
└──────┬──────┘
       │ 1:M (driver)
       │
       ├──────────────────────────────┐
       │                              │
       ▼                              │
┌─────────────┐                       │
│  Delivery   │                       │
└──────┬──────┘                       │
       │ 1:1                          │
       │                              │
       ▼                              │
┌─────────────┐                       │
│    Order    │                       │
└──────┬──────┘                       │
       │ 1:M                          │
       │                              │
       ▼                              │
┌─────────────┐                       │
│  OrderItem  │                       │
└──────┬──────┘                       │
       │ M:1                          │
       │                              │
       ▼                              │
┌─────────────┐                       │
│   Product   │                       │
└──────┬──────┘                       │
       │ M:1                          │
       │                              │
       ▼                              │
┌─────────────┐                       │
│  Category   │                       │
└─────────────┘                       │
```

### Relationship Details

**User → Delivery**:
- Type: One-to-Many
- Relationship: A delivery driver (User with DELIVERY_STAFF role) can have multiple deliveries
- Foreign Key: `delivery.driver_id` → `user.id`
- Nullable: Yes (delivery can be unassigned)

**Order → Delivery**:
- Type: One-to-One
- Relationship: A delivery order has one delivery record
- Foreign Key: `delivery.order_id` → `order.id`
- Cascade: Delete delivery when order deleted
- Nullable: No (delivery must have order)

**Order → OrderItem**:
- Type: One-to-Many
- Relationship: An order contains multiple order items
- Foreign Key: `order_item.order_id` → `order.id`
- Cascade: Delete order items when order deleted
- Nullable: No (order item must belong to order)

**Product → OrderItem**:
- Type: One-to-Many
- Relationship: A product can appear in multiple order items
- Foreign Key: `order_item.product_id` → `product.id`
- Cascade: No cascade (preserve historical orders)
- Nullable: No (order item must reference product)

**Category → Product**:
- Type: One-to-Many
- Relationship: A category contains multiple products
- Foreign Key: `product.category_id` → `category.id`
- Cascade: No (prevent deletion of category with products)
- Nullable: No (product must have category)

---

### Cascade Behavior

**Delete Order**:
- Deletes all associated OrderItems
- Deletes associated Delivery
- Does NOT delete referenced Products
- Does NOT delete driver User

**Delete Product**:
- Fails if product is referenced by any OrderItem
- Must remove from all orders first (or use soft delete)

**Delete Category**:
- Fails if category has any products
- Must reassign or delete products first

**Delete User (Driver)**:
- Fails if user has active deliveries
- Must complete or reassign deliveries first

---

## Data Type Mappings

### Java → JSON → Mobile

| Java Type | JSON Type | iOS (Swift) | Android (Kotlin) | Flutter | React Native |
|-----------|-----------|-------------|------------------|---------|--------------|
| Long | number | Int64 | Long | int | number |
| String | string | String | String | String | string |
| Boolean | boolean | Bool | Boolean | bool | boolean |
| BigDecimal | number | Decimal/Double | BigDecimal | double | number |
| LocalDateTime | string | Date | LocalDateTime | DateTime | Date |
| Enum | string | enum | enum | enum | string |
| List<T> | array | [T] | List<T> | List<T> | T[] |

### Important Notes

**BigDecimal Handling**:
- Transmitted as JSON number (e.g., `24.99`)
- Use appropriate decimal types in mobile to avoid floating point errors
- iOS: Use `Decimal` or `NSDecimalNumber`
- Android: Use `BigDecimal`
- Flutter: Use `double` with rounding
- React Native: Use decimal libraries or integer cents

**DateTime Handling**:
- Format: ISO 8601 (`2025-11-13T10:30:00`)
- Timezone: Asia/Phnom_Penh (UTC+7)
- Parse with appropriate date formatter/parser
- Display in user's local timezone

**Enum Handling**:
- Transmitted as string
- Case-sensitive matching required
- Validate before sending to API
- Use native enum types in mobile code

---

## Example Model Classes

### iOS (Swift)

```swift
struct User: Codable {
    let id: Int
    let username: String
    let email: String?
    let fullName: String?
    let role: UserRole
    let enabled: Bool
    let createdAt: Date
    let updatedAt: Date
}

enum UserRole: String, Codable {
    case admin = "ADMIN"
    case manager = "MANAGER"
    case kitchenStaff = "KITCHEN_STAFF"
    case deliveryStaff = "DELIVERY_STAFF"
}

struct Product: Codable {
    let id: Int
    let name: String
    let description: String?
    let price: Decimal
    let imageUrl: String?
    let isAvailable: Bool
    let category: Category
    let createdAt: Date
    let updatedAt: Date
}

struct Order: Codable {
    let id: Int
    let customerDetails: String
    let status: OrderStatus
    let totalPrice: Decimal
    let orderType: OrderType
    let createdAt: Date
    let orderItems: [OrderItem]
    let delivery: Delivery?
}
```

### Android (Kotlin)

```kotlin
data class User(
    val id: Long,
    val username: String,
    val email: String?,
    val fullName: String?,
    val role: UserRole,
    val enabled: Boolean,
    val createdAt: String,
    val updatedAt: String
)

enum class UserRole {
    ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF
}

data class Product(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val imageUrl: String?,
    val isAvailable: Boolean,
    val category: Category,
    val createdAt: String,
    val updatedAt: String
)

data class Order(
    val id: Long,
    val customerDetails: String,
    val status: OrderStatus,
    val totalPrice: BigDecimal,
    val orderType: OrderType,
    val createdAt: String,
    val orderItems: List<OrderItem>,
    val delivery: Delivery?
)
```

---

**Last Updated**: 2025-11-13
**Version**: 1.0.1
