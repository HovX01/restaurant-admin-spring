# Restaurant Admin System - Application Information

## Technology Stack

### Backend Framework
- **Spring Boot 3.5.5** - Main application framework
- **Java 17** - Programming language
- **Gradle** - Build automation tool

### Database & ORM
- **PostgreSQL** - Primary database
- **Spring Data JPA** - ORM framework with Hibernate
- **Hibernate** - ORM implementation

### Security
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Token)** - Token-based authentication
- **BCrypt** - Password hashing

### API & Documentation
- **Spring Web** - REST API framework
- **OpenAPI/Swagger** - API documentation (springdoc-openapi)
- **Jakarta Validation** - Input validation

### Additional Features
- **Spring WebSocket** - Real-time communication
- **Lombok** - Boilerplate code reduction
- **Spring Boot Starter Test** - Testing framework

## Application Logic

### Core Business Domain
Restaurant Management System with the following main entities:

#### User Management
- **User Entity**: Staff and admin users with role-based access control
- **Authentication**: JWT-based authentication system
- **Authorization**: Role-based permissions (ADMIN, STAFF, etc.)
- **Features**: Login, registration, password change, user management

#### Product Management
- **Product Entity**: Menu items with pricing, availability, and categorization
- **Category Entity**: Product categorization system
- **Features**: CRUD operations for products and categories, inventory management

#### Order Management
- **Order Entity**: Customer orders with status tracking
- **OrderItem Entity**: Individual items within orders
- **Order Status**: PENDING, PREPARING, READY, COMPLETED, CANCELLED
- **Order Types**: DINE_IN, TAKEAWAY, DELIVERY
- **Features**: Order creation, status updates, order history

#### Delivery Management
- **Delivery Entity**: Delivery tracking and management
- **Features**: Delivery status updates, delivery person assignment

### Architecture Pattern
- **Layered Architecture**: Clean separation of concerns
  - Controller Layer: REST API endpoints
  - Service Layer: Business logic implementation
  - Repository Layer: Data access abstraction
  - Entity Layer: Database models

### Key Features
1. **Authentication System**: JWT-based secure authentication
2. **Role-Based Access Control**: Different permission levels for users
3. **Real-time Updates**: WebSocket support for live order updates
4. **API Documentation**: Swagger/OpenAPI documentation
5. **Data Validation**: Comprehensive input validation
6. **Audit Trail**: Timestamp fields for tracking creation and updates
7. **Error Handling**: Centralized exception handling

### Security Features
- JWT token-based authentication
- Password encryption using BCrypt
- Role-based authorization
- CORS configuration
- SQL injection prevention through JPA
- Input validation at multiple layers

## File Structure

```
ResAdmin/
├── build.gradle                 # Gradle build configuration
├── gradlew*                     # Gradle wrapper script
├── gradlew.bat                  # Gradle wrapper script (Windows)
├── settings.gradle              # Gradle settings
├── HELP.md                      # Spring Boot help documentation
├── LICENSE                      # Project license
├── README.md                    # Project documentation
├── .gitignore                   # Git ignore rules
├── .gradle/                     # Gradle cache directory
├── build/                       # Build output directory
├── docs/                        # Documentation directory
│   └── app-info.md             # This file
├── gradle/                      # Gradle wrapper files
└── src/                         # Source code directory
    ├── main/                    # Main source code
    │   ├── java/com/resadmin/res/  # Java packages
    │   │   ├── ResApplication.java  # Main application class
    │   │   ├── config/              # Configuration classes
    │   │   │   ├── SecurityConfig.java    # Spring Security configuration
    │   │   │   ├── SwaggerConfig.java     # Swagger documentation config
    │   │   │   ├── WebConfig.java         # Web configuration
    │   │   │   └── WebSocketConfig.java   # WebSocket configuration
    │   │   ├── controller/          # REST API controllers
    │   │   │   ├── AuthController.java      # Authentication endpoints
    │   │   │   ├── CategoryController.java  # Category management
    │   │   │   ├── DeliveryController.java  # Delivery management
    │   │   │   ├── OrderController.java      # Order management
    │   │   │   ├── ProductController.java   # Product management
    │   │   │   └── UserController.java       # User management
    │   │   ├── dto/                 # Data Transfer Objects
    │   │   │   ├── request/         # Request DTOs
    │   │   │   ├── response/        # Response DTOs
    │   │   │   └── websocket/       # WebSocket DTOs
    │   │   ├── entity/              # JPA entity classes
    │   │   │   ├── User.java        # User entity
    │   │   │   ├── Product.java     # Product entity
    │   │   │   ├── Category.java    # Category entity
    │   │   │   ├── Order.java       # Order entity
    │   │   │   ├── OrderItem.java  # Order item entity
    │   │   │   └── Delivery.java   # Delivery entity
    │   │   ├── exception/           # Exception handling
    │   │   ├── mapper/             # Object mappers
    │   │   ├── repository/         # Data access layer
    │   │   ├── security/           # Security components
    │   │   ├── service/            # Business logic layer
    │   │   └── util/               # Utility classes
    │   └── resources/              # Application resources
    │       ├── application.properties  # Application configuration
    │       ├── schema.sql          # Database schema
    │       └── data.sql            # Sample data
    └── test/                       # Test source code
        └── java/com/resadmin/res/  # Test packages
```

## Database Schema

### Core Tables
- **users** - User accounts and authentication
- **categories** - Product categories
- **products** - Menu items
- **orders** - Customer orders
- **order_items** - Items within orders
- **deliveries** - Delivery information

### Key Relationships
- Users → Orders (One-to-Many)
- Categories → Products (One-to-Many)
- Orders → OrderItems (One-to-Many)
- Orders → Deliveries (One-to-One)
- Products → OrderItems (One-to-Many)

## Configuration

### Application Properties
- **Database**: PostgreSQL connection configuration
- **JWT**: Token secret and expiration settings
- **Server**: Port 8080
- **Logging**: Debug level for development
- **Swagger**: API documentation enabled
- **Timezone**: Asia/Phnom_Penh
- **Async**: Thread pool configuration

### Security Configuration
- JWT-based authentication
- Role-based authorization
- Password encryption
- CORS enabled for development

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/change-password` - Change password
- `GET /api/auth/me` - Get current user info

### Products
- `GET /api/products` - Get all products
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Orders
- `GET /api/orders` - Get all orders
- `POST /api/orders` - Create order
- `PUT /api/orders/{id}` - Update order
- `GET /api/orders/{id}` - Get order by ID

### Categories
- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

## Development Setup

### Prerequisites
- Java 17+
- PostgreSQL
- Gradle

### Running the Application
```bash
./gradlew bootRun
```

### Access Points
- Application: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html
- Database: PostgreSQL on localhost:5432

## Testing
- JUnit 5 for unit testing
- Spring Boot Test for integration testing
- Spring Security Test for security testing