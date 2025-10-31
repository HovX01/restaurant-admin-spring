# Development Guide

Complete development guide for the Restaurant Administration System.

## Table of Contents

1. [Development Environment Setup](#development-environment-setup)
2. [Project Structure](#project-structure)
3. [Development Workflow](#development-workflow)
4. [Coding Standards](#coding-standards)
5. [Testing Guidelines](#testing-guidelines)
6. [Debugging](#debugging)
7. [Common Development Tasks](#common-development-tasks)
8. [Performance Optimization](#performance-optimization)
9. [Deployment](#deployment)
10. [Troubleshooting](#troubleshooting)

## Development Environment Setup

### Prerequisites

**Required Software**:

| Software | Version | Download |
|----------|---------|----------|
| Java JDK | 17+ | [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/) |
| PostgreSQL | 12+ | [PostgreSQL](https://www.postgresql.org/download/) |
| Gradle | 7+ | [Gradle](https://gradle.org/install/) (or use wrapper) |
| Git | Latest | [Git](https://git-scm.com/downloads) |

**Recommended IDE**:
- IntelliJ IDEA (recommended)
- Eclipse
- Visual Studio Code with Java extensions

### Initial Setup

**1. Clone Repository**:
```bash
git clone <repository-url>
cd restaurant-admin-spring
```

**2. Database Setup**:
```bash
# Start PostgreSQL service
sudo service postgresql start  # Linux
# or
brew services start postgresql  # macOS
# or
net start postgresql-x64-12    # Windows

# Create database and user
psql -U postgres

CREATE DATABASE res_dev01;
CREATE USER res_dev01 WITH PASSWORD 'res_dev01';
GRANT ALL PRIVILEGES ON DATABASE res_dev01 TO res_dev01;
\q
```

**3. Configure Application**:

Edit `src/main/resources/application.properties`:
```properties
# Database (update if using different credentials)
spring.datasource.url=jdbc:postgresql://localhost:5432/res_dev01
spring.datasource.username=res_dev01
spring.datasource.password=res_dev01

# JWT (IMPORTANT: Change for production)
jwt.secret=myVeryLongSecretKeyThatIsAtLeast256BitsLongForJWTHMACAlgorithm
jwt.expiration=86400000

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**4. Build Project**:
```bash
# Using Gradle wrapper (recommended)
./gradlew clean build

# Or using installed Gradle
gradle clean build
```

**5. Run Application**:
```bash
./gradlew bootRun

# Or
java -jar build/libs/res-1.0.1-SNAPSHOT.jar
```

**6. Verify Setup**:
```bash
# Check API
curl http://localhost:8080/api/auth/login

# Check Swagger UI
open http://localhost:8080/swagger-ui.html
```

### IDE Setup

**IntelliJ IDEA**:

1. **Import Project**:
   - File → Open → Select project directory
   - Select "Import as Gradle project"

2. **Install Plugins**:
   - Lombok Plugin
   - Database Navigator
   - Git Integration (built-in)

3. **Configure Lombok**:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Enable "Enable annotation processing"

4. **Database Tool**:
   - View → Tool Windows → Database
   - Add PostgreSQL data source
   - Test connection

5. **Run Configuration**:
   - Run → Edit Configurations
   - Add Application
   - Main class: `com.resadmin.res.ResApplication`
   - Environment variables (if needed)

**VS Code**:

1. **Install Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support
   - PostgreSQL

2. **Settings** (`.vscode/settings.json`):
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.java.home": "/path/to/jdk-17"
}
```

### Environment Variables (Optional)

Create `.env` file (DO NOT commit):
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/res_dev01
DB_USERNAME=res_dev01
DB_PASSWORD=res_dev01

# JWT
JWT_SECRET=your-very-secure-secret-key
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080
```

Load in `application.properties`:
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
```

## Project Structure

```
restaurant-admin-spring/
├── src/
│   ├── main/
│   │   ├── java/com/resadmin/res/
│   │   │   ├── ResApplication.java           # Main application class
│   │   │   ├── config/                       # Configuration classes
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/                   # REST controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── DeliveryController.java
│   │   │   │   └── WebSocketController.java
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── entity/                       # JPA entities
│   │   │   ├── exception/                    # Custom exceptions
│   │   │   ├── mapper/                       # Entity-DTO mappers
│   │   │   ├── repository/                   # Data repositories
│   │   │   ├── security/                     # Security components
│   │   │   ├── service/                      # Business logic
│   │   │   └── util/                         # Utility classes
│   │   └── resources/
│   │       ├── application.properties        # Application config
│   │       ├── schema.sql                    # Database schema
│   │       └── data.sql                      # Sample data
│   └── test/
│       └── java/com/resadmin/res/
│           └── service/
│               └── OrderServiceTest.java     # Unit tests
├── build.gradle                              # Gradle build file
├── settings.gradle                           # Gradle settings
├── gradlew                                   # Gradle wrapper (Unix)
├── gradlew.bat                               # Gradle wrapper (Windows)
├── docs/                                     # Documentation
│   ├── README.md
│   ├── ARCHITECTURE.md
│   ├── API_DOCUMENTATION.md
│   ├── DATABASE_SCHEMA.md
│   ├── SECURITY.md
│   ├── WEBSOCKET.md
│   └── DEVELOPMENT.md
└── README.md                                 # Project README
```

## Development Workflow

### Git Workflow

**Branch Strategy**:
```
main (production-ready)
  └── develop (integration branch)
      ├── feature/user-management
      ├── feature/order-tracking
      ├── bugfix/order-calculation
      └── hotfix/security-patch
```

**Workflow**:

1. **Create Feature Branch**:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/new-feature
```

2. **Develop and Commit**:
```bash
# Make changes
git add .
git commit -m "Add: New feature description"

# Follow conventional commits
# Types: Add, Update, Fix, Refactor, Test, Docs
```

3. **Push and Create PR**:
```bash
git push origin feature/new-feature
# Create Pull Request on GitHub
```

4. **Code Review and Merge**:
```bash
# After approval
git checkout develop
git merge feature/new-feature
git push origin develop
```

### Development Cycle

```
1. Plan → 2. Develop → 3. Test → 4. Review → 5. Deploy
    ↑                                             ↓
    └─────────────── Feedback ──────────────────┘
```

**Daily Workflow**:
1. Pull latest changes: `git pull origin develop`
2. Create/switch to feature branch
3. Write code with tests
4. Run tests: `./gradlew test`
5. Run application: `./gradlew bootRun`
6. Test manually via Swagger UI
7. Commit changes
8. Push and create PR

## Coding Standards

### Java Conventions

**Naming**:
```java
// Classes: PascalCase
public class OrderService { }

// Methods: camelCase
public void createOrder() { }

// Variables: camelCase
private String customerName;

// Constants: UPPER_SNAKE_CASE
private static final int MAX_RETRY = 3;

// Packages: lowercase
package com.resadmin.res.service;
```

**Class Structure Order**:
```java
public class ExampleClass {
    // 1. Static variables
    private static final Logger log = LoggerFactory.getLogger(ExampleClass.class);

    // 2. Instance variables
    @Autowired
    private SomeRepository repository;

    private String instanceVariable;

    // 3. Constructors
    public ExampleClass() { }

    // 4. Public methods
    public void publicMethod() { }

    // 5. Protected methods
    protected void protectedMethod() { }

    // 6. Private methods
    private void privateMethod() { }

    // 7. Getters/Setters (or use Lombok)
    public String getInstanceVariable() {
        return instanceVariable;
    }
}
```

### Code Style

**Formatting**:
```java
// Braces on same line
public void method() {
    if (condition) {
        doSomething();
    } else {
        doSomethingElse();
    }
}

// Indentation: 4 spaces
// Line length: 120 characters max
// Blank lines: One between methods
```

**Best Practices**:

1. **Use Lombok**:
```java
// Good
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
}

// Avoid
public class UserDTO {
    private Long id;
    private String username;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... repetitive boilerplate
}
```

2. **Dependency Injection**:
```java
// Good: Constructor injection
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}

// Avoid: Field injection
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
}
```

3. **Exception Handling**:
```java
// Good: Specific exceptions
public Order getOrder(Long id) {
    return orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Order not found with id: " + id));
}

// Avoid: Generic exceptions
public Order getOrder(Long id) throws Exception {
    // ...
}
```

4. **Null Safety**:
```java
// Good: Optional
public Optional<User> findUser(String username) {
    return userRepository.findByUsername(username);
}

// Good: Objects.requireNonNull
public void process(Order order) {
    Objects.requireNonNull(order, "Order cannot be null");
}
```

5. **Logging**:
```java
// Good: Use SLF4J with placeholders
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

log.info("Processing order: {}", orderId);
log.error("Failed to process order: {}", orderId, exception);

// Avoid: String concatenation
log.info("Processing order: " + orderId);
```

### Documentation

**JavaDoc**:
```java
/**
 * Service for managing restaurant orders.
 *
 * <p>This service handles order creation, updates, status changes,
 * and provides various query methods for retrieving orders.
 *
 * @author Your Name
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class OrderService {

    /**
     * Creates a new order with the specified items.
     *
     * @param request the order creation request containing customer details and items
     * @return the created order wrapped in ApiResponseDTO
     * @throws ResourceNotFoundException if any product in the order is not found
     * @throws IllegalArgumentException if the order contains no items
     */
    public ApiResponseDTO<OrderDTO> createOrder(CreateOrderRequestDTO request) {
        // ...
    }
}
```

## Testing Guidelines

### Test Structure

```
src/test/java/com/resadmin/res/
├── service/
│   ├── OrderServiceTest.java
│   ├── UserServiceTest.java
│   └── DeliveryServiceTest.java
├── controller/
│   └── OrderControllerTest.java
└── repository/
    └── OrderRepositoryTest.java
```

### Unit Testing

**Service Layer Test Example**:

```java
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private WebSocketService webSocketService;

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrder() {
        // Arrange
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        // ... setup request

        Product product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("10.99"));
        product.setAvailable(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ApiResponseDTO<OrderDTO> result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(webSocketService).sendOrderNotification(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testCreateOrderProductNotFound() {
        // Arrange
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        // ... setup request

        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(request);
        });
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateOrderEndpoint() throws Exception {
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        // ... setup request

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }
}
```

### Test Coverage

**Run Tests with Coverage**:
```bash
./gradlew test jacocoTestReport
```

**View Coverage Report**:
```bash
open build/reports/jacoco/test/html/index.html
```

**Target Coverage**:
- Overall: 80%+
- Service layer: 90%+
- Controller layer: 70%+
- Repository layer: 50%+

## Debugging

### Enable Debug Logging

**application.properties**:
```properties
# General debug
logging.level.root=INFO
logging.level.com.resadmin.res=DEBUG

# SQL debug
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Security debug
logging.level.org.springframework.security=DEBUG

# WebSocket debug
logging.level.org.springframework.messaging=DEBUG
```

### IntelliJ IDEA Debugging

**Breakpoints**:
1. Click left margin to add breakpoint
2. Right-click breakpoint for conditions
3. Run → Debug 'ResApplication'

**Useful Features**:
- Evaluate Expression: Alt + F8
- Step Over: F8
- Step Into: F7
- Resume: F9

### Common Debug Scenarios

**1. Debug SQL Queries**:
```java
@Service
public class OrderService {

    public List<Order> getOrders() {
        List<Order> orders = orderRepository.findAll();
        log.debug("Retrieved {} orders", orders.size());  // Add logging
        return orders;
    }
}
```

**2. Debug JWT Token**:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String token = extractToken(request);
        log.debug("Extracted token: {}", token);  // Debug token

        if (token != null && jwtUtil.validateToken(token)) {
            log.debug("Token valid for user: {}", jwtUtil.getUsernameFromToken(token));
        }
    }
}
```

**3. Debug WebSocket**:
```properties
# Enable STOMP debug
logging.level.org.springframework.messaging.simp=TRACE
```

### Remote Debugging

**Run with Debug Port**:
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
     -jar build/libs/res-1.0.1-SNAPSHOT.jar
```

**Connect from IDE**:
1. Run → Edit Configurations
2. Add → Remote JVM Debug
3. Port: 5005
4. Run → Debug 'Remote Debug'

## Common Development Tasks

### Add New Entity

**1. Create Entity**:
```java
@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private Integer rating;
    private String comment;

    @CreatedDate
    private LocalDateTime createdAt;
}
```

**2. Create Repository**:
```java
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByOrderId(Long orderId);
    List<Review> findByRatingGreaterThanEqual(Integer rating);
}
```

**3. Create DTO**:
```java
@Data
@Builder
public class ReviewDTO {
    private Long id;
    private Long orderId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
```

**4. Create Service**:
```java
@Service
@Transactional
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public ApiResponseDTO<ReviewDTO> createReview(CreateReviewDTO request) {
        // Implementation
    }
}
```

**5. Create Controller**:
```java
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<ReviewDTO>> createReview(
            @Valid @RequestBody CreateReviewDTO request) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }
}
```

### Add New Endpoint

**1. Define in Controller**:
```java
@GetMapping("/orders/stats/revenue")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public ResponseEntity<ApiResponseDTO<RevenueStatsDTO>> getRevenueStats(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {
    return ResponseEntity.ok(orderService.getRevenueStats(startDate, endDate));
}
```

**2. Implement in Service**:
```java
public ApiResponseDTO<RevenueStatsDTO> getRevenueStats(
        LocalDate startDate, LocalDate endDate) {
    // Query database
    // Calculate stats
    // Return DTO
}
```

**3. Test**:
```bash
curl -X GET "http://localhost:8080/api/orders/stats/revenue?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer $TOKEN"
```

### Database Migration

**Using Flyway** (recommended for production):

**1. Add Dependency** (build.gradle):
```gradle
implementation 'org.flywaydb:flyway-core'
```

**2. Create Migration** (`src/main/resources/db/migration/V1__initial_schema.sql`):
```sql
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**3. Run Migration**:
```bash
./gradlew flywayMigrate
```

## Performance Optimization

### Database Optimization

**1. Use Pagination**:
```java
@GetMapping
public Page<OrderDTO> getOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return orderRepository.findAll(pageable).map(EntityMapper::toOrderDTO);
}
```

**2. Optimize Queries**:
```java
// Bad: N+1 problem
List<Order> orders = orderRepository.findAll();
orders.forEach(order -> order.getItems().size());  // Lazy loading!

// Good: Fetch join
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);
```

**3. Use Indexes**:
```sql
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);
CREATE INDEX idx_products_category_available ON products(category_id, is_available);
```

**4. Connection Pooling**:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Caching

**Add Redis** (optional):

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-cache'
```

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }
}

@Service
public class ProductService {
    @Cacheable("products")
    public ProductDTO getProduct(Long id) {
        // Cached result
    }

    @CacheEvict(value = "products", key = "#id")
    public void updateProduct(Long id, ProductDTO dto) {
        // Invalidate cache
    }
}
```

## Deployment

### Build for Production

```bash
# Build JAR
./gradlew clean build -Pprod

# JAR location
ls -lh build/libs/res-1.0.1-SNAPSHOT.jar
```

### Production Configuration

**application-prod.properties**:
```properties
# Server
server.port=8080

# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Logging
logging.level.root=WARN
logging.level.com.resadmin.res=INFO

# CORS (restrict origins)
cors.allowed-origins=https://yourdomain.com
```

### Docker Deployment

**Dockerfile**:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/res-1.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  db:
    image: postgres:14
    environment:
      POSTGRES_DB: res_prod
      POSTGRES_USER: res_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://db:5432/res_prod
      DB_USERNAME: res_user
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - db

volumes:
  postgres_data:
```

**Deploy**:
```bash
docker-compose up -d
```

## Troubleshooting

### Common Issues

**1. Port Already in Use**:
```bash
# Find process
lsof -i :8080
# Kill process
kill -9 <PID>
# Or change port in application.properties
server.port=8081
```

**2. Database Connection Failed**:
```bash
# Check PostgreSQL is running
sudo service postgresql status
# Test connection
psql -U res_dev01 -d res_dev01 -h localhost
# Check credentials in application.properties
```

**3. JWT Token Invalid**:
- Check jwt.secret is at least 256 bits
- Verify token hasn't expired
- Check Authorization header format: `Bearer <token>`

**4. Build Failed**:
```bash
# Clean build
./gradlew clean
# Delete .gradle directory
rm -rf .gradle
# Rebuild
./gradlew build
```

**5. Tests Failing**:
```bash
# Run specific test
./gradlew test --tests OrderServiceTest

# Skip tests
./gradlew build -x test
```

---

Happy Coding! 🚀
