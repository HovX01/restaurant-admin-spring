# Database Schema Documentation

Complete database schema reference for the Restaurant Administration System.

## Table of Contents

1. [Overview](#overview)
2. [Entity Relationship Diagram](#entity-relationship-diagram)
3. [Tables](#tables)
4. [Indexes](#indexes)
5. [Enumerations](#enumerations)
6. [Sample Data](#sample-data)
7. [Database Maintenance](#database-maintenance)

## Overview

**Database**: PostgreSQL 12+
**Schema**: Public
**Timezone**: Asia/Phnom_Penh
**Initialization**: Automatic via `schema.sql` and `data.sql`

The database consists of 6 main tables with well-defined relationships and constraints to ensure data integrity.

## Entity Relationship Diagram

```
┌──────────────┐
│    users     │
└──────┬───────┘
       │
       │ 1:N (driver)
       │
       ▼
┌──────────────┐         ┌──────────────┐
│  deliveries  │◄────────│    orders    │
└──────────────┘  1:1    └──────┬───────┘
                                │
                                │ 1:N
                                ▼
                         ┌──────────────┐         ┌──────────────┐
                         │ order_items  │─────────│   products   │
                         └──────────────┘   N:1   └──────┬───────┘
                                                         │
                                                         │ N:1
                                                         ▼
                                                  ┌──────────────┐
                                                  │  categories  │
                                                  └──────────────┘
```

## Tables

### 1. users

Stores user authentication and profile information.

**Table Structure**:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique user identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| email | VARCHAR(100) | UNIQUE, NOT NULL | User email address |
| full_name | VARCHAR(100) | NOT NULL | User's full name |
| role | VARCHAR(20) | NOT NULL | User role (enum) |
| enabled | BOOLEAN | DEFAULT true | Account active status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Indexes**:
- `idx_users_username` on username
- `idx_users_email` on email
- `idx_users_role` on role
- `idx_users_enabled` on enabled

**Sample Record**:
```sql
INSERT INTO users (username, password, email, full_name, role, enabled)
VALUES ('admin', '$2a$10$...', 'admin@restaurant.com', 'System Administrator', 'ADMIN', true);
```

**Roles**:
- ADMIN - Full system access
- MANAGER - Product/order management
- KITCHEN_STAFF - Kitchen operations
- DELIVERY_STAFF - Delivery operations

---

### 2. categories

Product categorization table.

**Table Structure**:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique category identifier |
| name | VARCHAR(100) | UNIQUE, NOT NULL | Category name |
| description | TEXT | NULL | Category description |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Indexes**:
- `idx_categories_name` on name

**Sample Record**:
```sql
INSERT INTO categories (name, description)
VALUES ('Appetizers', 'Starters and small bites');
```

---

### 3. products

Menu items and product catalog.

**Table Structure**:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique product identifier |
| name | VARCHAR(100) | UNIQUE, NOT NULL | Product name |
| description | TEXT | NULL | Product description |
| price | DECIMAL(10,2) | NOT NULL, CHECK (price >= 0) | Product price |
| image_url | VARCHAR(255) | NULL | Product image URL |
| is_available | BOOLEAN | DEFAULT true | Availability status |
| category_id | BIGINT | FOREIGN KEY → categories(id) | Category reference |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Indexes**:
- `idx_products_category_id` on category_id
- `idx_products_is_available` on is_available
- `idx_products_name` on name

**Constraints**:
- Foreign key: category_id references categories(id) ON DELETE RESTRICT
- Check constraint: price >= 0

**Sample Record**:
```sql
INSERT INTO products (name, description, price, image_url, is_available, category_id)
VALUES ('Spring Rolls', 'Crispy vegetable spring rolls (4 pieces)', 5.99,
        'http://example.com/spring-rolls.jpg', true, 1);
```

**Notes**:
- Uses DECIMAL(10,2) for precise monetary calculations
- ON DELETE RESTRICT prevents category deletion if products exist

---

### 4. orders

Customer orders table.

**Table Structure**:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique order identifier |
| customer_details | JSONB | NOT NULL | Customer information (name, phone, email, address) |
| order_type | VARCHAR(20) | NOT NULL | Order type (enum) |
| status | VARCHAR(30) | NOT NULL, DEFAULT 'PENDING' | Order status (enum) |
| total_price | DECIMAL(10,2) | NOT NULL, CHECK (total_price >= 0) | Total order amount |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Order creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Indexes**:
- `idx_orders_status` on status
- `idx_orders_order_type` on order_type
- `idx_orders_created_at` on created_at

**Constraints**:
- Check constraint: total_price >= 0

**Customer Details JSONB Structure**:
```json
{
  "name": "John Doe",
  "phone": "+1234567890",
  "email": "john@example.com",
  "address": "123 Main St, City, State 12345"
}
```

**Order Types**:
- DINE_IN - Eat in restaurant
- TAKEOUT - Take away
- DELIVERY - Home delivery
- PICKUP - Customer pickup

**Order Statuses**:
- PENDING - Order received
- CONFIRMED - Order confirmed
- PREPARING - Being prepared
- READY_FOR_PICKUP - Ready for customer pickup
- READY_FOR_DELIVERY - Ready for delivery
- OUT_FOR_DELIVERY - Driver on the way
- COMPLETED - Order completed
- CANCELLED - Order cancelled

**Sample Record**:
```sql
INSERT INTO orders (customer_details, order_type, status, total_price)
VALUES ('{"name":"John Doe","phone":"+1234567890","email":"john@example.com","address":"123 Main St"}',
        'DELIVERY', 'PENDING', 25.97);
```

---

### 5. order_items

Order line items (many-to-many bridge between orders and products).

**Table Structure**:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique item identifier |
| order_id | BIGINT | FOREIGN KEY → orders(id), NOT NULL | Order reference |
| product_id | BIGINT | FOREIGN KEY → products(id), NOT NULL | Product reference |
| quantity | INTEGER | NOT NULL, CHECK (quantity > 0) | Item quantity |
| price | DECIMAL(10,2) | NOT NULL, CHECK (price >= 0) | Price at time of order |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update time |

**Indexes**:
- `idx_order_items_order_id` on order_id
- `idx_order_items_product_id` on product_id

**Constraints**:
- Foreign key: order_id references orders(id) ON DELETE CASCADE
- Foreign key: product_id references products(id) ON DELETE RESTRICT
- Check constraint: quantity > 0
- Check constraint: price >= 0

**Sample Record**:
```sql
INSERT INTO order_items (order_id, product_id, quantity, price)
VALUES (1, 1, 2, 5.99);
```

**Notes**:
- Stores price at time of order (historical pricing)
- ON DELETE CASCADE removes items when order is deleted
- ON DELETE RESTRICT prevents product deletion if used in orders

---

### 6. deliveries

Delivery tracking and assignment.

**Table Structure**:

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique delivery identifier |
| order_id | BIGINT | FOREIGN KEY → orders(id), UNIQUE, NOT NULL | Order reference (one-to-one) |
| driver_id | BIGINT | FOREIGN KEY → users(id), NOT NULL | Driver reference |
| delivery_address | TEXT | NOT NULL | Delivery address |
| delivery_notes | TEXT | NULL | Special delivery instructions |
| status | VARCHAR(30) | NOT NULL, DEFAULT 'PENDING' | Delivery status (enum) |
| dispatched_at | TIMESTAMP | NULL | Time driver dispatched |
| delivered_at | TIMESTAMP | NULL | Time delivery completed |

**Indexes**:
- `idx_deliveries_order_id` on order_id (unique)
- `idx_deliveries_driver_id` on driver_id
- `idx_deliveries_status` on status
- `idx_deliveries_dispatched_at` on dispatched_at

**Constraints**:
- Foreign key: order_id references orders(id) ON DELETE CASCADE (UNIQUE)
- Foreign key: driver_id references users(id) ON DELETE RESTRICT

**Delivery Statuses**:
- PENDING - Awaiting assignment
- ASSIGNED - Assigned to driver
- OUT_FOR_DELIVERY - Driver en route
- DELIVERED - Delivery completed

**Sample Record**:
```sql
INSERT INTO deliveries (order_id, driver_id, delivery_address, delivery_notes, status, dispatched_at)
VALUES (5, 4, '123 Main St, City', 'Ring doorbell', 'ASSIGNED', CURRENT_TIMESTAMP);
```

**Notes**:
- One-to-one relationship with orders (UNIQUE constraint on order_id)
- driver_id must reference a user with DELIVERY_STAFF role (validated in application)
- ON DELETE RESTRICT prevents driver deletion if they have deliveries

---

## Indexes

Complete index listing for query optimization:

### Performance Indexes

```sql
-- Users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_enabled ON users(enabled);

-- Categories
CREATE INDEX idx_categories_name ON categories(name);

-- Products
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_is_available ON products(is_available);
CREATE INDEX idx_products_name ON products(name);

-- Orders
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_type ON orders(order_type);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Order Items
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Deliveries
CREATE UNIQUE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_driver_id ON deliveries(driver_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_dispatched_at ON deliveries(dispatched_at);
```

### Composite Indexes (Recommended for Production)

```sql
-- For filtering products by category and availability
CREATE INDEX idx_products_category_available
ON products(category_id, is_available);

-- For filtering orders by status and date
CREATE INDEX idx_orders_status_created
ON orders(status, created_at DESC);

-- For delivery driver queries
CREATE INDEX idx_deliveries_driver_status
ON deliveries(driver_id, status);

-- For JSONB queries on customer details
CREATE INDEX idx_orders_customer_details
ON orders USING GIN (customer_details);
```

---

## Enumerations

### User Roles

Defined in Java enum `com.resadmin.res.entity.User.Role`:

```java
public enum Role {
    ADMIN,           // Full system access
    MANAGER,         // Product/order management
    KITCHEN_STAFF,   // Kitchen operations
    DELIVERY_STAFF   // Delivery operations
}
```

**Database Storage**: VARCHAR(20)

---

### Order Types

Defined in Java enum `com.resadmin.res.entity.Order.OrderType`:

```java
public enum OrderType {
    DINE_IN,    // Eat in restaurant
    TAKEOUT,    // Take away
    DELIVERY,   // Home delivery
    PICKUP      // Customer pickup
}
```

**Database Storage**: VARCHAR(20)

---

### Order Status

Defined in Java enum `com.resadmin.res.entity.Order.OrderStatus`:

```java
public enum OrderStatus {
    PENDING,              // Order received
    CONFIRMED,            // Order confirmed
    PREPARING,            // Being prepared in kitchen
    READY_FOR_PICKUP,     // Ready for customer pickup
    READY_FOR_DELIVERY,   // Ready for delivery
    OUT_FOR_DELIVERY,     // Driver en route
    COMPLETED,            // Order completed
    CANCELLED             // Order cancelled
}
```

**Database Storage**: VARCHAR(30)

**Status Flow**:
```
PENDING
  ├─→ CONFIRMED
  │     ├─→ PREPARING
  │     │     ├─→ READY_FOR_PICKUP → COMPLETED
  │     │     └─→ READY_FOR_DELIVERY → OUT_FOR_DELIVERY → COMPLETED
  │     └─→ CANCELLED
  └─→ CANCELLED
```

---

### Delivery Status

Defined in Java enum `com.resadmin.res.entity.Delivery.DeliveryStatus`:

```java
public enum DeliveryStatus {
    PENDING,            // Awaiting assignment
    ASSIGNED,           // Assigned to driver
    OUT_FOR_DELIVERY,   // Driver en route
    DELIVERED           // Delivery completed
}
```

**Database Storage**: VARCHAR(30)

**Status Flow**:
```
PENDING → ASSIGNED → OUT_FOR_DELIVERY → DELIVERED
```

---

## Sample Data

### Pre-configured Users

```sql
-- Admin user
INSERT INTO users (username, password, email, full_name, role, enabled)
VALUES ('admin', '$2a$10$...hashed...', 'admin@restaurant.com',
        'System Administrator', 'ADMIN', true);

-- Manager user
INSERT INTO users (username, password, email, full_name, role, enabled)
VALUES ('manager1', '$2a$10$...hashed...', 'manager@restaurant.com',
        'Restaurant Manager', 'MANAGER', true);

-- Kitchen staff
INSERT INTO users (username, password, email, full_name, role, enabled)
VALUES ('chef1', '$2a$10$...hashed...', 'chef@restaurant.com',
        'Head Chef', 'KITCHEN_STAFF', true);

-- Delivery drivers
INSERT INTO users (username, password, email, full_name, role, enabled)
VALUES ('driver1', '$2a$10$...hashed...', 'driver1@restaurant.com',
        'Driver One', 'DELIVERY_STAFF', true);

INSERT INTO users (username, password, email, full_name, role, enabled)
VALUES ('driver2', '$2a$10$...hashed...', 'driver2@restaurant.com',
        'Driver Two', 'DELIVERY_STAFF', true);
```

**Default Password**: `password123` (BCrypt hashed)

### Sample Categories

```sql
INSERT INTO categories (name, description) VALUES
('Appetizers', 'Starters and small bites'),
('Main Courses', 'Main dishes and entrees'),
('Desserts', 'Sweet treats'),
('Beverages', 'Drinks and beverages'),
('Salads', 'Fresh salads and healthy options');
```

### Sample Products

17 products across 5 categories with prices ranging from $2.99 to $16.99.

---

## Database Queries

### Common Queries

**Get all available products with category**:
```sql
SELECT p.*, c.name as category_name
FROM products p
JOIN categories c ON p.category_id = c.id
WHERE p.is_available = true
ORDER BY c.name, p.name;
```

**Get order with items**:
```sql
SELECT
    o.*,
    oi.id as item_id,
    oi.quantity,
    oi.price,
    p.name as product_name
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id
WHERE o.id = ?;
```

**Get kitchen orders**:
```sql
SELECT *
FROM orders
WHERE status IN ('CONFIRMED', 'PREPARING')
ORDER BY created_at ASC;
```

**Get driver's deliveries**:
```sql
SELECT
    d.*,
    o.customer_details,
    o.total_price
FROM deliveries d
JOIN orders o ON d.order_id = o.id
WHERE d.driver_id = ?
AND d.status != 'DELIVERED'
ORDER BY d.dispatched_at ASC;
```

**Today's revenue**:
```sql
SELECT
    COUNT(*) as order_count,
    SUM(total_price) as total_revenue
FROM orders
WHERE status = 'COMPLETED'
AND created_at >= CURRENT_DATE
AND created_at < CURRENT_DATE + INTERVAL '1 day';
```

**Most popular products**:
```sql
SELECT
    p.name,
    SUM(oi.quantity) as times_ordered,
    SUM(oi.quantity * oi.price) as total_revenue
FROM order_items oi
JOIN products p ON oi.product_id = p.id
JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'COMPLETED'
GROUP BY p.id, p.name
ORDER BY times_ordered DESC
LIMIT 10;
```

---

## Database Maintenance

### Backup

```bash
# Full database backup
pg_dump -h localhost -U res_dev01 res_dev01 > backup_$(date +%Y%m%d).sql

# Schema only
pg_dump -h localhost -U res_dev01 --schema-only res_dev01 > schema_backup.sql

# Data only
pg_dump -h localhost -U res_dev01 --data-only res_dev01 > data_backup.sql
```

### Restore

```bash
# Restore from backup
psql -h localhost -U res_dev01 res_dev01 < backup_20240115.sql
```

### Vacuum and Analyze

```sql
-- Vacuum all tables
VACUUM ANALYZE;

-- Vacuum specific table
VACUUM ANALYZE orders;

-- Check table statistics
SELECT
    schemaname,
    tablename,
    n_tup_ins,
    n_tup_upd,
    n_tup_del
FROM pg_stat_user_tables;
```

### Performance Monitoring

```sql
-- Check slow queries
SELECT
    query,
    calls,
    total_time,
    mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 20;

-- Check table sizes
SELECT
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check index usage
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC;
```

---

## Migration Strategy

### Adding New Fields

```sql
-- Add new column
ALTER TABLE products ADD COLUMN allergens TEXT[];

-- Add with default
ALTER TABLE orders ADD COLUMN notes TEXT DEFAULT '';

-- Add not null with default
ALTER TABLE users ADD COLUMN last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

### Modifying Existing Fields

```sql
-- Change column type
ALTER TABLE products ALTER COLUMN price TYPE NUMERIC(12,2);

-- Add constraint
ALTER TABLE products ADD CONSTRAINT check_positive_price CHECK (price > 0);

-- Drop constraint
ALTER TABLE products DROP CONSTRAINT check_positive_price;
```

### Creating Indexes

```sql
-- Standard index
CREATE INDEX idx_products_price ON products(price);

-- Partial index
CREATE INDEX idx_active_products ON products(name) WHERE is_available = true;

-- GIN index for JSONB
CREATE INDEX idx_orders_customer_jsonb ON orders USING GIN (customer_details);
```

---

## Data Integrity Rules

1. **Referential Integrity**: All foreign keys use appropriate ON DELETE rules
2. **Price Precision**: All monetary values use DECIMAL(10,2)
3. **Timestamps**: All tables include created_at and updated_at
4. **Soft Deletes**: Consider implementing soft deletes for audit trail
5. **Unique Constraints**: Prevent duplicate usernames, emails, product names, etc.
6. **Check Constraints**: Ensure positive prices and quantities
7. **JSONB Validation**: Customer details follow consistent structure

---

## Best Practices

1. **Always use transactions** for multi-table operations
2. **Use prepared statements** to prevent SQL injection
3. **Index foreign keys** for join performance
4. **Monitor query performance** regularly
5. **Keep statistics up to date** with VACUUM ANALYZE
6. **Use connection pooling** (configured in Spring Boot)
7. **Implement database versioning** (e.g., Flyway, Liquibase)
8. **Regular backups** before schema changes
9. **Test migrations** on development environment first
10. **Document all schema changes** in migration files
