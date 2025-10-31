# Security Documentation

Comprehensive security guide for the Restaurant Administration System.

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Authorization](#authorization)
4. [Password Security](#password-security)
5. [JWT Token Management](#jwt-token-management)
6. [Security Configuration](#security-configuration)
7. [Role-Based Access Control](#role-based-access-control)
8. [Security Best Practices](#security-best-practices)
9. [Common Security Vulnerabilities](#common-security-vulnerabilities)
10. [Security Checklist](#security-checklist)

## Overview

The Restaurant Administration System implements a comprehensive security architecture using Spring Security 3.x with JWT (JSON Web Token) authentication. The system follows industry best practices for authentication, authorization, and data protection.

### Security Features

- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control (RBAC)
- Method-level security annotations
- CORS configuration
- Custom authentication error handling
- Secure password change functionality
- Token expiration management

## Authentication

### Authentication Flow

```
┌────────────┐
│   Client   │
└─────┬──────┘
      │
      │ POST /api/auth/login
      │ {username, password}
      ▼
┌─────────────────┐
│AuthController   │
└─────┬───────────┘
      │
      ▼
┌─────────────────────────────┐
│   AuthenticationManager     │
│  (Spring Security)          │
└─────┬───────────────────────┘
      │
      ▼
┌─────────────────────────────┐
│ CustomUserDetailsService    │
│  - Load user from database  │
│  - Return UserDetails       │
└─────┬───────────────────────┘
      │
      ▼
┌─────────────────────────────┐
│   Password Verification     │
│  - BCrypt compare           │
└─────┬───────────────────────┘
      │
      ├─ Success ─▶ Generate JWT Token
      │
      └─ Failure ─▶ BadCredentialsException
                    (401 Unauthorized)
```

### Login Implementation

**Endpoint**: `POST /api/auth/login`

**Request**:
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Process**:
1. Client sends credentials
2. Spring Security validates credentials
3. `CustomUserDetailsService` loads user from database
4. BCrypt compares password hash
5. On success, `JwtUtil` generates JWT token
6. Token and user info returned to client

**Response**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN"
    }
  }
}
```

### Token Usage

**Authorization Header**:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token Validation Flow**:
```
Request with JWT Token
      │
      ▼
┌─────────────────────────────┐
│ JwtAuthenticationFilter     │
│  - Extract token from header│
│  - Validate token signature │
│  - Check expiration         │
└─────┬───────────────────────┘
      │
      ├─ Valid ─▶ Load user details
      │           Set SecurityContext
      │           Continue to controller
      │
      └─ Invalid ─▶ Clear SecurityContext
                    Return 401 Unauthorized
```

## Authorization

### Role-Based Access Control

The system implements four user roles with hierarchical permissions:

```
┌─────────────────────────────────────────────┐
│                  ADMIN                       │
│  - All system operations                    │
│  - User management (create, update, delete) │
│  - All CRUD operations                      │
│  - System configuration                     │
└─────────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────────┐
│                 MANAGER                      │
│  - Product/Category management              │
│  - Order creation and management            │
│  - User creation (limited)                  │
│  - Delivery assignment                      │
│  - View reports and statistics              │
└─────────────────────────────────────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
┌───────────────────┐   ┌───────────────────┐
│  KITCHEN_STAFF    │   │  DELIVERY_STAFF   │
│  - View orders    │   │  - View assigned  │
│  - Update order   │   │    deliveries     │
│    status         │   │  - Update delivery│
│  - Kitchen ops    │   │    status         │
└───────────────────┘   └───────────────────┘
```

### Method-Level Security

**Using @PreAuthorize**:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // Only ADMIN and MANAGER can access
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        // ...
    }

    // Only ADMIN can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // ...
    }

    // Any authenticated user can access
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getProfile() {
        // ...
    }
}
```

### Access Control Matrix

| Resource | GET | POST | PUT | PATCH | DELETE |
|----------|-----|------|-----|-------|--------|
| **/api/auth/*** | Public | Public | - | - | - |
| **/api/users** | ADMIN, MANAGER | ADMIN, MANAGER | ADMIN, MANAGER | - | ADMIN |
| **/api/categories** | All | ADMIN, MANAGER | ADMIN, MANAGER | - | ADMIN |
| **/api/products** | All | ADMIN, MANAGER | ADMIN, MANAGER | - | ADMIN |
| **/api/orders** | ADMIN, MANAGER, KITCHEN_STAFF | ADMIN, MANAGER | ADMIN, MANAGER | ADMIN, MANAGER, KITCHEN_STAFF | ADMIN |
| **/api/deliveries** | ADMIN, MANAGER, DELIVERY_STAFF | ADMIN, MANAGER | ADMIN, MANAGER | ADMIN, MANAGER, DELIVERY_STAFF | ADMIN |

## Password Security

### Password Hashing

**Algorithm**: BCrypt with cost factor 10

**Configuration**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### BCrypt Advantages

1. **Adaptive**: Slow by design, resistant to brute force
2. **Salted**: Automatic per-password salt
3. **One-way**: Cannot be reversed
4. **Configurable**: Cost factor can be increased

**Example**:
```java
// Plain password
String plainPassword = "password123";

// BCrypt hash (different each time due to random salt)
String hash1 = "$2a$10$XYZ...";
String hash2 = "$2a$10$ABC...";

// Both hashes validate the same password
passwordEncoder.matches(plainPassword, hash1); // true
passwordEncoder.matches(plainPassword, hash2); // true
```

### Password Requirements

**Current Implementation**: No enforced complexity

**Recommended Production Rules**:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character
- Not in common password list

**Implementation Example**:
```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character"
)
private String password;
```

### Change Password

**Endpoint**: `POST /api/auth/change-password`

**Security Measures**:
1. Requires valid JWT token
2. Validates current password
3. Hashes new password with BCrypt
4. Updates database
5. Does NOT invalidate existing tokens (consider implementing)

**Request**:
```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newSecurePassword456"
}
```

## JWT Token Management

### Token Structure

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9    ← Header
.
eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9  ← Payload
.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature
```

### Token Payload

**Claims**:
```json
{
  "sub": "admin",              // Subject (username)
  "role": "ADMIN",             // User role
  "iat": 1642234567,           // Issued at (timestamp)
  "exp": 1642320967            // Expiration (timestamp)
}
```

### Token Configuration

**application.properties**:
```properties
# Secret key (256-bit minimum for HS256)
jwt.secret=myVeryLongSecretKeyThatIsAtLeast256BitsLongForJWTHMACAlgorithm

# Expiration time (24 hours in milliseconds)
jwt.expiration=86400000
```

**IMPORTANT**: Change `jwt.secret` in production!

### Token Generation

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", ((User) userDetails).getRole().name());

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

### Token Validation

```java
public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token);
        return true;
    } catch (ExpiredJwtException e) {
        // Token expired
    } catch (MalformedJwtException e) {
        // Invalid token format
    } catch (SignatureException e) {
        // Invalid signature
    }
    return false;
}
```

### Token Expiration Handling

**Client-Side**:
1. Store token expiration time
2. Monitor expiration
3. Refresh token before expiration (if refresh endpoint exists)
4. Redirect to login when expired

**Server-Side**:
- Expired tokens return 401 Unauthorized
- Client should catch 401 and redirect to login

**Recommended**: Implement refresh token mechanism

## Security Configuration

### Spring Security Configuration

**SecurityConfig.java** (simplified):

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disabled for JWT
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()  // Non-API routes unrestricted
            )
            .addFilterBefore(jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(customAuthEntryPoint()));

        return http.build();
    }
}
```

### CORS Configuration

**Current Configuration**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Production Recommendation**:
```java
// Restrict to specific origins
configuration.setAllowedOrigins(Arrays.asList(
    "https://restaurant-admin.example.com",
    "https://restaurant-app.example.com"
));

// Only allow necessary headers
configuration.setAllowedHeaders(Arrays.asList(
    "Authorization",
    "Content-Type",
    "Accept"
));
```

### CSRF Protection

**Current**: Disabled (common for JWT-based APIs)

**Why Disabled**:
- JWT tokens stored in localStorage/sessionStorage (not cookies)
- Each request includes token in Authorization header
- CSRF attacks exploit automatic cookie inclusion

**If Using Cookies**: Enable CSRF protection

## Role-Based Access Control

### Role Hierarchy

**Recommendation**: Implement role hierarchy for cleaner code

```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_KITCHEN_STAFF");
    return roleHierarchy;
}
```

**Benefit**: ADMIN automatically has MANAGER and KITCHEN_STAFF permissions

### Custom Permission Checks

**Service-Level Authorization**:
```java
@Service
public class DeliveryService {

    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        User currentUser = getCurrentUser();

        // Delivery staff can only update their own deliveries
        if (currentUser.getRole() == Role.DELIVERY_STAFF) {
            if (!delivery.getDriver().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Cannot update other driver's delivery");
            }
        }

        // Proceed with update
        delivery.setStatus(status);
        deliveryRepository.save(delivery);
    }
}
```

## Security Best Practices

### 1. Secure JWT Secret

**Bad**:
```properties
jwt.secret=secret123
```

**Good**:
```properties
# Use environment variable
jwt.secret=${JWT_SECRET}

# Or generate strong secret (256+ bits)
jwt.secret=1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7a8b9c0d1e2f
```

**Generate Strong Secret**:
```bash
# Using OpenSSL
openssl rand -base64 64

# Using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

### 2. Token Storage

**Client-Side Storage Options**:

| Storage | XSS Vulnerability | CSRF Vulnerability | Recommendation |
|---------|-------------------|-------------------|----------------|
| localStorage | High | Low | Use with caution |
| sessionStorage | High | Low | Better than localStorage |
| Memory only | Low | Low | Best (lost on page refresh) |
| HttpOnly Cookie | Low | High (mitigated with CSRF token) | Good with CSRF protection |

**Recommendation**: Use `httpOnly` cookies with CSRF protection for maximum security.

### 3. Password Storage

**Never**:
- Store plain-text passwords
- Use weak hashing (MD5, SHA1)
- Use reversible encryption

**Always**:
- Use BCrypt, Argon2, or PBKDF2
- Use appropriate cost factor
- Validate password strength

### 4. Input Validation

```java
@PostMapping("/api/orders")
public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    // @Valid triggers validation
}

public class CreateOrderRequest {
    @NotNull(message = "Customer details required")
    private CustomerDetails customerDetails;

    @NotNull(message = "Order type required")
    private OrderType orderType;

    @NotEmpty(message = "At least one item required")
    private List<OrderItemRequest> items;
}
```

### 5. SQL Injection Prevention

**Good** (Using JPA):
```java
@Query("SELECT o FROM Order o WHERE o.status = :status")
List<Order> findByStatus(@Param("status") OrderStatus status);
```

**Bad** (String concatenation):
```java
// NEVER DO THIS!
String sql = "SELECT * FROM orders WHERE status = '" + status + "'";
```

### 6. Sensitive Data Exposure

**Exclude from JSON**:
```java
@Entity
public class User implements UserDetails {
    @JsonIgnore
    private String password;

    // Don't expose in API responses
}
```

### 7. API Rate Limiting

**Recommendation**: Implement rate limiting

```java
// Using Bucket4j or similar
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    // Limit to 100 requests per minute per IP
}
```

### 8. Logging Security

**Good**:
```java
log.info("User {} logged in successfully", username);
log.error("Login failed for user: {}", username);
```

**Bad**:
```java
log.info("User logged in: " + username + " with password: " + password);  // NEVER!
```

### 9. HTTPS in Production

**application.properties** (production):
```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
server.ssl.key-store-type=PKCS12
```

### 10. Security Headers

**Add Security Headers**:
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers(headers -> headers
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
        .frameOptions(frame -> frame.deny())
    );
    return http.build();
}
```

## Common Security Vulnerabilities

### 1. Broken Authentication

**Vulnerability**: Weak password requirements, no account lockout

**Mitigation**:
- Enforce strong passwords
- Implement account lockout after failed attempts
- Use multi-factor authentication (MFA)
- Monitor for suspicious login patterns

### 2. Sensitive Data Exposure

**Vulnerability**: Passwords, tokens in logs or API responses

**Mitigation**:
- Use `@JsonIgnore` on sensitive fields
- Never log passwords or tokens
- Encrypt sensitive data at rest
- Use HTTPS for data in transit

### 3. Injection Attacks

**Vulnerability**: SQL injection, NoSQL injection

**Mitigation**:
- Use parameterized queries (JPA)
- Validate and sanitize input
- Use ORMs properly
- Avoid string concatenation in queries

### 4. Broken Access Control

**Vulnerability**: Users accessing resources they shouldn't

**Mitigation**:
- Implement proper authorization checks
- Verify ownership of resources
- Use @PreAuthorize annotations
- Test authorization thoroughly

### 5. Security Misconfiguration

**Vulnerability**: Default credentials, exposed admin interfaces

**Mitigation**:
- Change default passwords
- Disable unnecessary features
- Keep dependencies updated
- Use security headers

### 6. Cross-Site Scripting (XSS)

**Vulnerability**: Malicious scripts in user input

**Mitigation**:
- Validate and sanitize input
- Encode output
- Use Content Security Policy headers
- Avoid rendering user input as HTML

### 7. Insecure Deserialization

**Vulnerability**: Arbitrary code execution via malicious serialized objects

**Mitigation**:
- Avoid deserializing untrusted data
- Use safe serialization formats (JSON)
- Validate deserialized objects
- Implement type whitelisting

### 8. Using Components with Known Vulnerabilities

**Vulnerability**: Outdated dependencies with security flaws

**Mitigation**:
- Keep dependencies updated
- Use dependency scanning tools
- Monitor security advisories
- Automate dependency updates

### 9. Insufficient Logging & Monitoring

**Vulnerability**: Security breaches go undetected

**Mitigation**:
- Log authentication events
- Log authorization failures
- Monitor suspicious patterns
- Set up alerts for security events

### 10. Server-Side Request Forgery (SSRF)

**Vulnerability**: Server making requests to internal resources

**Mitigation**:
- Validate and sanitize URLs
- Use allowlists for external requests
- Disable unnecessary protocols
- Implement network segmentation

## Security Checklist

### Development

- [ ] All passwords are hashed with BCrypt
- [ ] JWT secret is strong and configured via environment variable
- [ ] Input validation on all endpoints
- [ ] No sensitive data in logs
- [ ] `@PreAuthorize` annotations on protected endpoints
- [ ] SQL injection prevention (using JPA)
- [ ] XSS prevention (input validation, output encoding)

### Testing

- [ ] Test authentication with invalid credentials
- [ ] Test authorization for all roles
- [ ] Test token expiration handling
- [ ] Test password change functionality
- [ ] Test CORS configuration
- [ ] Test input validation error messages
- [ ] Penetration testing (if possible)

### Production

- [ ] Change JWT secret to strong random value
- [ ] Enable HTTPS
- [ ] Restrict CORS to specific origins
- [ ] Implement rate limiting
- [ ] Set up security monitoring
- [ ] Configure security headers
- [ ] Regular security audits
- [ ] Dependency vulnerability scanning
- [ ] Backup and disaster recovery plan
- [ ] Incident response plan

### Compliance

- [ ] GDPR compliance (if applicable)
- [ ] Data retention policies
- [ ] Privacy policy
- [ ] Terms of service
- [ ] Cookie consent (if using cookies)

---

## Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [BCrypt Documentation](https://en.wikipedia.org/wiki/Bcrypt)
