-- Seed default users if they do not already exist

INSERT INTO users (username, password, email, full_name, role, enabled, created_at, updated_at)
SELECT 'admin',
       '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu',
       'admin@resadmin.com',
       'System Administrator',
       'ADMIN',
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, email, full_name, role, enabled, created_at, updated_at)
SELECT 'manager1',
       '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu',
       'manager@resadmin.com',
       'Restaurant Manager',
       'MANAGER',
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'manager1');

INSERT INTO users (username, password, email, full_name, role, enabled, created_at, updated_at)
SELECT 'chef1',
       '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu',
       'chef@resadmin.com',
       'Head Chef',
       'KITCHEN_STAFF',
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'chef1');

INSERT INTO users (username, password, email, full_name, role, enabled, created_at, updated_at)
SELECT 'driver1',
       '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu',
       'driver1@resadmin.com',
       'John Delivery',
       'DELIVERY_STAFF',
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'driver1');

INSERT INTO users (username, password, email, full_name, role, enabled, created_at, updated_at)
SELECT 'driver2',
       '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu',
       'driver2@resadmin.com',
       'Jane Express',
       'DELIVERY_STAFF',
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'driver2');

