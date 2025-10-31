# Restaurant Administration System

A comprehensive Spring Boot REST API backend for restaurant management with real-time WebSocket communication, JWT authentication, and role-based access control.

## Overview

The Restaurant Administration System is a complete backend solution designed to manage all aspects of a restaurant's operations, including order management, product catalog, delivery tracking, user management, and real-time notifications.

## Key Features

- **JWT Authentication**: Secure token-based authentication with 24-hour expiration
- **Role-Based Access Control**: Four user roles (ADMIN, MANAGER, KITCHEN_STAFF, DELIVERY_STAFF)
- **Real-time Notifications**: WebSocket/STOMP integration for live updates
- **Order Management**: Complete order lifecycle tracking from creation to delivery
- **Product Catalog**: Manage products with categories, pricing, and availability
- **Delivery Tracking**: Assign and track deliveries with status updates
- **Comprehensive API**: RESTful endpoints with pagination and filtering
- **API Documentation**: Interactive Swagger/OpenAPI documentation
- **Error Handling**: Global exception handling with standardized responses
- **Financial Precision**: BigDecimal for all monetary calculations

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.5.5 |
| Java | JDK | 17+ |
| Build Tool | Gradle | 7+ |
| Database | PostgreSQL | 12+ |
| ORM | Spring Data JPA | Hibernate |
| Security | Spring Security + JWT | 3.x |
| JWT Library | JJWT | 0.12.3 |
| Real-time | WebSocket/STOMP | - |
| Documentation | Swagger/OpenAPI | 3 |

## Quick Start

### Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Gradle 7 or higher

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd restaurant-admin-spring
   ```

2. **Create PostgreSQL database**
   ```sql
   CREATE DATABASE res_dev01;
   CREATE USER res_dev01 WITH PASSWORD 'res_dev01';
   GRANT ALL PRIVILEGES ON DATABASE res_dev01 TO res_dev01;
   ```

3. **Configure database connection**

   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/res_dev01
   spring.datasource.username=res_dev01
   spring.datasource.password=res_dev01
   ```

4. **Build the project**
   ```bash
   ./gradlew clean build
   ```

5. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

6. **Access the application**
   - API Base URL: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - WebSocket Endpoint: ws://localhost:8080/ws

### Sample Credentials

The application comes with pre-configured users (password: `password123` for all):

| Username | Role | Description |
|----------|------|-------------|
| admin | ADMIN | Full system access |
| manager1 | MANAGER | Product/order management |
| chef1 | KITCHEN_STAFF | Kitchen operations |
| driver1 | DELIVERY_STAFF | Delivery operations |
| driver2 | DELIVERY_STAFF | Delivery operations |

## API Testing

### Using cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# Get products (with token)
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using Swagger UI

1. Navigate to http://localhost:8080/swagger-ui.html
2. Click on `/api/auth/login` endpoint
3. Click "Try it out"
4. Enter credentials and execute
5. Copy the JWT token from response
6. Click "Authorize" button at the top
7. Enter: `Bearer YOUR_JWT_TOKEN`
8. Now you can test all protected endpoints

## Project Structure

```
src/main/java/com/resadmin/res/
├── ResApplication.java          # Main application entry point
├── config/                      # Configuration classes
├── controller/                  # REST API controllers
├── entity/                      # JPA entities
├── dto/                         # Data Transfer Objects
├── service/                     # Business logic layer
├── repository/                  # Data access layer
├── security/                    # Security components
├── exception/                   # Exception handling
├── mapper/                      # Entity-DTO mapping
└── util/                        # Utility classes
```

## Documentation

Detailed documentation is available in the `/docs` directory:

- [Architecture Guide](./ARCHITECTURE.md) - System architecture and design patterns
- [API Documentation](./API_DOCUMENTATION.md) - Detailed API endpoints and examples
- [Database Schema](./DATABASE_SCHEMA.md) - Database structure and relationships
- [Security Guide](./SECURITY.md) - Authentication and authorization
- [WebSocket Guide](./WEBSOCKET.md) - Real-time communication
- [Development Guide](./DEVELOPMENT.md) - Development setup and guidelines

## Core Modules

### Authentication & Authorization
- JWT-based authentication
- BCrypt password encryption
- Role-based access control
- Secure endpoints with method-level security

### Order Management
- Create and track orders
- Status workflow: PENDING → CONFIRMED → PREPARING → READY → DELIVERED/COMPLETED
- Order types: DINE_IN, TAKEOUT, DELIVERY, PICKUP
- Real-time kitchen notifications

### Product & Category Management
- Product CRUD operations
- Category organization
- Availability tracking
- Price management with BigDecimal precision

### Delivery Management
- Assign deliveries to drivers
- Track delivery status
- Delivery address and notes
- Real-time driver notifications

### Real-time Notifications
- WebSocket/STOMP integration
- Order status updates
- Delivery assignments
- Kitchen alerts
- System-wide notifications

## API Overview

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/change-password` - Change password
- `GET /api/auth/info` - Get current user info

### Main Resource Endpoints
- `/api/users` - User management
- `/api/categories` - Category management
- `/api/products` - Product management
- `/api/orders` - Order management
- `/api/deliveries` - Delivery management

For complete API documentation, see [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) or visit the Swagger UI.

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/res_dev01
spring.datasource.username=res_dev01
spring.datasource.password=res_dev01

# JWT
jwt.secret=myVeryLongSecretKeyThatIsAtLeast256BitsLongForJWTHMACAlgorithm
jwt.expiration=86400000  # 24 hours

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Timezone
spring.jackson.time-zone=Asia/Phnom_Penh
```

## Testing

### Run all tests
```bash
./gradlew test
```

### Run specific test
```bash
./gradlew test --tests OrderServiceTest
```

## Development

### Build project
```bash
./gradlew clean build
```

### Run in development mode
```bash
./gradlew bootRun
```

### Generate API documentation
API documentation is automatically generated and available at `/swagger-ui.html` when the application is running.

## Troubleshooting

### Database Connection Issues
- Ensure PostgreSQL is running
- Verify database credentials in `application.properties`
- Check if database `res_dev01` exists

### JWT Token Issues
- Ensure token is included in Authorization header: `Bearer <token>`
- Check if token has expired (24-hour validity)
- Verify JWT secret is configured correctly

### WebSocket Connection Issues
- Check if WebSocket endpoint is accessible: `ws://localhost:8080/ws`
- Ensure proper STOMP client configuration
- Verify CORS settings if connecting from different origin

## License

[Add your license information here]

## Contributing

[Add your contributing guidelines here]

## Contact

[Add your contact information here]
