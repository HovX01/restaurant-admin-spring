# System Architecture

This document describes the architecture and design patterns used in the Restaurant Administration System.

## Table of Contents

1. [Overview](#overview)
2. [Architectural Layers](#architectural-layers)
3. [Design Patterns](#design-patterns)
4. [Component Architecture](#component-architecture)
5. [Data Flow](#data-flow)
6. [Security Architecture](#security-architecture)
7. [WebSocket Architecture](#websocket-architecture)

## Overview

The Restaurant Administration System follows a layered architecture pattern with clear separation of concerns. The application is built on Spring Boot 3.5.5 and uses industry-standard design patterns for maintainability and scalability.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                         │
│  (Web/Mobile Apps, WebSocket Clients)                   │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ HTTP/HTTPS/WebSocket
                 │
┌────────────────▼────────────────────────────────────────┐
│              Spring Security Filter Chain                │
│  (JWT Authentication, CORS, Authorization)               │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│                   Controller Layer                       │
│  (REST API Endpoints, WebSocket Message Handlers)        │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│                    Service Layer                         │
│  (Business Logic, Transaction Management)                │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│                  Repository Layer                        │
│  (Data Access, JPA Repositories)                         │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│                   Database Layer                         │
│  (PostgreSQL Database)                                   │
└─────────────────────────────────────────────────────────┘
```

## Architectural Layers

### 1. Controller Layer

**Purpose**: Handle HTTP requests, validate input, and format responses.

**Responsibilities**:
- Receive HTTP requests
- Validate request parameters and bodies
- Delegate business logic to service layer
- Format and return HTTP responses
- Handle WebSocket message routing

**Key Components**:
- `AuthController` - Authentication endpoints
- `UserController` - User management
- `ProductController` - Product operations
- `CategoryController` - Category operations
- `OrderController` - Order management
- `DeliveryController` - Delivery operations
- `WebSocketController` - WebSocket message handlers

**Example**:
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }
}
```

### 2. Service Layer

**Purpose**: Implement business logic and coordinate between controllers and repositories.

**Responsibilities**:
- Execute business logic
- Manage transactions
- Coordinate multiple repository operations
- Trigger events and notifications
- Validate business rules
- Transform between entities and DTOs

**Key Components**:
- `AuthService` - Authentication and user management
- `OrderService` - Order processing and management
- `ProductService` - Product operations
- `CategoryService` - Category operations
- `DeliveryService` - Delivery management
- `WebSocketService` - Real-time notification service

**Example**:
```java
@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WebSocketService webSocketService;

    public ApiResponseDTO<OrderDTO> createOrder(CreateOrderRequestDTO request) {
        // Business logic
        Order order = new Order();
        // ... set properties
        Order savedOrder = orderRepository.save(order);

        // Send notification
        webSocketService.sendOrderNotification(savedOrder);

        return ApiResponseDTO.success(EntityMapper.toOrderDTO(savedOrder));
    }
}
```

### 3. Repository Layer

**Purpose**: Data access and persistence operations.

**Responsibilities**:
- CRUD operations
- Custom queries
- Database transactions
- Query optimization

**Key Components**:
- `UserRepository` - User data access
- `OrderRepository` - Order data access
- `ProductRepository` - Product data access
- `CategoryRepository` - Category data access
- `DeliveryRepository` - Delivery data access

**Features**:
- Extends `JpaRepository` for standard CRUD
- Extends `JpaSpecificationExecutor` for dynamic queries
- Custom query methods with `@Query` annotations
- Method naming conventions for derived queries

**Example**:
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
                                          JpaSpecificationExecutor<Order> {
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 Pageable pageable);
}
```

### 4. Entity Layer

**Purpose**: Define database schema and object-relational mapping.

**Responsibilities**:
- Map to database tables
- Define relationships
- Specify constraints and validation
- Audit information (timestamps)

**Key Components**:
- `User` - User entity (implements UserDetails for Spring Security)
- `Order` - Order entity
- `OrderItem` - Order line items
- `Product` - Product entity
- `Category` - Category entity
- `Delivery` - Delivery entity

### 5. DTO Layer

**Purpose**: Data transfer between layers and external systems.

**Responsibilities**:
- Transfer data between client and server
- Prevent over-fetching and under-fetching
- Validation rules
- API contract definition

**Structure**:
```
dto/
├── UserDTO.java
├── OrderDTO.java
├── ProductDTO.java
├── request/              # Request DTOs
│   ├── LoginRequestDTO
│   ├── CreateOrderRequestDTO
│   └── UpdateOrderStatusRequestDTO
└── response/             # Response DTOs
    ├── ApiResponseDTO
    ├── TokenResponse
    └── PagedResponseDTO
```

## Design Patterns

### 1. Layered Architecture Pattern

Separates application into distinct layers with clear responsibilities.

**Benefits**:
- Separation of concerns
- Testability
- Maintainability
- Flexibility to change implementations

### 2. Repository Pattern

Abstracts data access logic from business logic.

**Implementation**: Spring Data JPA repositories

**Benefits**:
- Decouples business logic from data access
- Easier testing with repository mocks
- Consistent data access interface

### 3. DTO Pattern

Separates internal domain models from external representations.

**Benefits**:
- Control data exposure
- Version API independently from domain
- Prevent circular references in JSON serialization

### 4. Service Facade Pattern

Provides simplified interface to complex subsystems.

**Example**: `WebSocketService` simplifies WebSocket operations

**Benefits**:
- Simplifies complex operations
- Centralizes logic
- Easier to maintain and test

### 5. Dependency Injection Pattern

Spring's core pattern for managing dependencies.

**Implementation**: `@Autowired`, constructor injection

**Benefits**:
- Loose coupling
- Easier testing
- Flexible configuration

### 6. Filter Chain Pattern

Processes requests through a chain of filters.

**Implementation**: Spring Security Filter Chain, `JwtAuthenticationFilter`

**Benefits**:
- Modular request processing
- Cross-cutting concerns (authentication, logging)
- Easy to add/remove filters

### 7. Strategy Pattern

Defines family of algorithms and makes them interchangeable.

**Example**: Different order status transitions based on order type

**Benefits**:
- Flexible behavior
- Open/closed principle
- Easier to add new strategies

### 8. Observer Pattern

Notifies multiple subscribers of state changes.

**Implementation**: WebSocket notifications

**Benefits**:
- Decoupled communication
- Real-time updates
- Multiple subscribers

### 9. Specification Pattern

Encapsulates business rules in reusable objects.

**Implementation**: `JpaSpecificationExecutor` for dynamic queries

**Benefits**:
- Reusable business rules
- Composable conditions
- Type-safe queries

### 10. Transaction Script Pattern

Organizes business logic as procedures/scripts.

**Implementation**: Service layer methods with `@Transactional`

**Benefits**:
- Simple to understand
- Good for CRUD operations
- Transaction management

## Component Architecture

### Authentication Flow

```
┌──────────┐       ┌─────────────────┐       ┌─────────────┐
│  Client  │──────▶│ AuthController  │──────▶│ AuthService │
└──────────┘       └─────────────────┘       └─────────────┘
     │                                              │
     │                                              ▼
     │                                        ┌──────────────┐
     │                                        │UserRepository│
     │                                        └──────────────┘
     │                                              │
     │                                              ▼
     │                                        ┌──────────────┐
     │                                        │   JwtUtil    │
     │                                        └──────────────┘
     │                                              │
     │◀─────────────── JWT Token ───────────────────┘
     │
     │       ┌─────────────────────────────────────┐
     └──────▶│ Authorization: Bearer <token>       │
             └─────────────────────────────────────┘
                            │
                            ▼
             ┌─────────────────────────────────────┐
             │   JwtAuthenticationFilter           │
             └─────────────────────────────────────┘
                            │
                            ▼
             ┌─────────────────────────────────────┐
             │   Protected Resource                │
             └─────────────────────────────────────┘
```

### Order Processing Flow

```
┌────────────┐
│   Client   │
└─────┬──────┘
      │ POST /api/orders
      ▼
┌─────────────────┐
│OrderController  │
└─────┬───────────┘
      │
      ▼
┌─────────────────┐
│  OrderService   │──────┐
└─────┬───────────┘      │
      │                  │
      ▼                  │
┌─────────────────┐      │
│OrderRepository  │      │
└─────┬───────────┘      │
      │                  │
      ▼                  │
┌─────────────────┐      │
│   Database      │      │
└─────┬───────────┘      │
      │                  │
      │ Order Created    │
      └──────────────────┤
                         │
                         ▼
              ┌────────────────────┐
              │ WebSocketService   │
              └─────┬──────────────┘
                    │
                    ▼
          ┌─────────────────────┐
          │ Broadcast to Topics │
          │ - /topic/orders     │
          │ - /topic/kitchen    │
          └─────────────────────┘
```

### Delivery Assignment Flow

```
┌────────────┐
│  Manager   │
└─────┬──────┘
      │ POST /api/deliveries/assign
      ▼
┌─────────────────────┐
│DeliveryController   │
└─────┬───────────────┘
      │
      ▼
┌─────────────────────┐
│  DeliveryService    │
└─────┬───────────────┘
      │
      ├──▶ Validate Order Status
      ├──▶ Validate Driver Role
      │
      ▼
┌─────────────────────┐
│DeliveryRepository   │
└─────┬───────────────┘
      │
      ▼
┌─────────────────────┐
│   Database          │
└─────┬───────────────┘
      │
      ▼
┌─────────────────────┐
│ WebSocketService    │
└─────┬───────────────┘
      │
      ├──▶ /topic/deliveries (All)
      └──▶ /user/{driverId}/queue/notifications (Driver)
```

## Data Flow

### Request Flow

```
HTTP Request
    │
    ▼
┌─────────────────────────┐
│  CORS Filter            │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│  JWT Filter             │
│  (validate token)       │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│  Authorization Filter   │
│  (check roles)          │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│  Controller             │
│  (validate input)       │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│  Service                │
│  (business logic)       │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│  Repository             │
│  (data access)          │
└─────────┬───────────────┘
          │
          ▼
┌─────────────────────────┐
│  Database               │
└─────────┬───────────────┘
          │
          ▼
      Response
```

### Exception Flow

```
Exception Thrown
    │
    ▼
┌─────────────────────────────┐
│  GlobalExceptionHandler     │
│  (@RestControllerAdvice)    │
└─────────┬───────────────────┘
          │
          ├─▶ ResourceNotFoundException → 404
          ├─▶ BadCredentialsException → 401
          ├─▶ AccessDeniedException → 403
          ├─▶ ValidationException → 400
          ├─▶ IllegalStateException → 409
          └─▶ Exception → 500
              │
              ▼
┌─────────────────────────────┐
│  ApiResponseDTO             │
│  {success: false,           │
│   message: "...",           │
│   error: "...",             │
│   timestamp: "..."}         │
└─────────────────────────────┘
```

## Security Architecture

### Authentication Architecture

```
┌─────────────────────────────────────────────────┐
│              Security Filter Chain              │
│                                                 │
│  ┌───────────────────────────────────────┐     │
│  │  1. CORS Filter                       │     │
│  └───────────────┬───────────────────────┘     │
│                  │                             │
│  ┌───────────────▼───────────────────────┐     │
│  │  2. JWT Authentication Filter         │     │
│  │     - Extract token from header       │     │
│  │     - Validate token signature        │     │
│  │     - Check expiration                │     │
│  │     - Load user details               │     │
│  │     - Set authentication in context   │     │
│  └───────────────┬───────────────────────┘     │
│                  │                             │
│  ┌───────────────▼───────────────────────┐     │
│  │  3. Authorization Filter              │     │
│  │     - Check @PreAuthorize             │     │
│  │     - Verify user roles               │     │
│  └───────────────┬───────────────────────┘     │
│                  │                             │
└──────────────────┼─────────────────────────────┘
                   │
                   ▼
         ┌──────────────────┐
         │   Controller     │
         └──────────────────┘
```

### Authorization Model

```
┌──────────────────────────────────────────────┐
│              User Roles                       │
├──────────────────────────────────────────────┤
│  ADMIN                                        │
│   └─ Full system access                      │
│   └─ User management                         │
│   └─ All CRUD operations                     │
│                                              │
│  MANAGER                                     │
│   └─ Product/Category management             │
│   └─ Order creation                          │
│   └─ User management                         │
│   └─ Delivery assignment                     │
│                                              │
│  KITCHEN_STAFF                               │
│   └─ View orders                             │
│   └─ Update order status                     │
│   └─ Kitchen operations                      │
│                                              │
│  DELIVERY_STAFF                              │
│   └─ View assigned deliveries                │
│   └─ Update delivery status                  │
└──────────────────────────────────────────────┘
```

## WebSocket Architecture

### WebSocket Connection Flow

```
┌─────────┐                              ┌──────────┐
│ Client  │                              │  Server  │
└────┬────┘                              └────┬─────┘
     │                                        │
     │  1. HTTP Handshake Upgrade             │
     │────────────────────────────────────▶   │
     │                                        │
     │  2. Switching Protocols (101)          │
     │  ◀────────────────────────────────────│
     │                                        │
     │  3. STOMP CONNECT                      │
     │────────────────────────────────────▶   │
     │                                        │
     │  4. STOMP CONNECTED                    │
     │  ◀────────────────────────────────────│
     │                                        │
     │  5. SUBSCRIBE /topic/orders            │
     │────────────────────────────────────▶   │
     │                                        │
     │  6. MESSAGE (order notification)       │
     │  ◀────────────────────────────────────│
     │                                        │
```

### Topic Architecture

```
┌──────────────────────────────────────────────┐
│           WebSocket Topics                    │
├──────────────────────────────────────────────┤
│                                              │
│  /topic/notifications (Broadcast)            │
│  └─ All system notifications                 │
│                                              │
│  /topic/orders (Broadcast)                   │
│  └─ Order create/update events               │
│                                              │
│  /topic/deliveries (Broadcast)               │
│  └─ Delivery assignment/update events        │
│                                              │
│  /topic/kitchen (Kitchen Staff)              │
│  └─ New order notifications                  │
│                                              │
│  /topic/delivery-staff (Delivery Staff)      │
│  └─ Ready for delivery notifications         │
│                                              │
│  /user/{userId}/queue/notifications (Private)│
│  └─ User-specific notifications              │
│                                              │
└──────────────────────────────────────────────┘
```

## Best Practices

### 1. Separation of Concerns
- Each layer has a single responsibility
- Controllers don't contain business logic
- Services don't directly access the database

### 2. Dependency Injection
- Use constructor injection for required dependencies
- Use setter injection for optional dependencies
- Avoid field injection in production code

### 3. Transaction Management
- Use `@Transactional` at service layer
- Keep transactions short
- Avoid long-running operations in transactions

### 4. Error Handling
- Use global exception handler
- Return meaningful error messages
- Log exceptions appropriately

### 5. Security
- Never store plain-text passwords
- Validate all input
- Use parameterized queries
- Implement proper authorization checks

### 6. API Design
- Use proper HTTP methods and status codes
- Version your API
- Document all endpoints
- Use pagination for list endpoints

### 7. Database Access
- Use indexes for frequently queried columns
- Avoid N+1 query problems
- Use proper fetch strategies
- Implement soft deletes for audit trail

### 8. Testing
- Unit test business logic
- Integration test repositories
- Mock external dependencies
- Test security configurations

## Scalability Considerations

### Horizontal Scaling
- Stateless application design (JWT tokens)
- Session-less architecture
- Database connection pooling

### Caching
- Consider Redis for session storage
- Cache frequently accessed data
- Implement cache invalidation strategies

### Database Optimization
- Proper indexing strategy
- Query optimization
- Connection pooling
- Read replicas for read-heavy operations

### WebSocket Scalability
- Consider Redis pub/sub for WebSocket in clustered environment
- Implement WebSocket heartbeat/keepalive
- Handle reconnection logic

## Monitoring and Observability

### Logging
- Use appropriate log levels
- Include correlation IDs
- Log business events
- Avoid logging sensitive data

### Metrics
- Track API response times
- Monitor database query performance
- Track WebSocket connections
- Monitor error rates

### Health Checks
- Database connectivity
- External service availability
- Disk space
- Memory usage
