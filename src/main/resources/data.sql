-- Insert sample categories
INSERT INTO categories (name, description, created_at, updated_at) VALUES
('Appetizers', 'Delicious starters to begin your meal', NOW(), NOW()),
('Main Courses', 'Hearty and satisfying main dishes', NOW(), NOW()),
('Desserts', 'Sweet treats to end your meal perfectly', NOW(), NOW()),
('Beverages', 'Refreshing drinks and beverages', NOW(), NOW()),
('Salads', 'Fresh and healthy salad options', NOW(), NOW());

-- Insert sample products
INSERT INTO products (name, description, price, image_url, is_available, category_id, created_at, updated_at) VALUES
-- Appetizers
('Chicken Wings', 'Crispy chicken wings with your choice of sauce', 12.99, '/images/chicken-wings.jpg', true, 1, NOW(), NOW()),
('Mozzarella Sticks', 'Golden fried mozzarella with marinara sauce', 8.99, '/images/mozzarella-sticks.jpg', true, 1, NOW(), NOW()),
('Garlic Bread', 'Toasted bread with garlic butter and herbs', 6.99, '/images/garlic-bread.jpg', true, 1, NOW(), NOW()),

-- Main Courses
('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 16.99, '/images/margherita-pizza.jpg', true, 2, NOW(), NOW()),
('Pepperoni Pizza', 'Traditional pizza topped with pepperoni and cheese', 18.99, '/images/pepperoni-pizza.jpg', true, 2, NOW(), NOW()),
('Grilled Chicken Breast', 'Juicy grilled chicken with seasonal vegetables', 22.99, '/images/grilled-chicken.jpg', true, 2, NOW(), NOW()),
('Beef Burger', 'Angus beef patty with lettuce, tomato, and fries', 15.99, '/images/beef-burger.jpg', true, 2, NOW(), NOW()),
('Pasta Carbonara', 'Creamy pasta with bacon, eggs, and parmesan', 19.99, '/images/pasta-carbonara.jpg', true, 2, NOW(), NOW()),

-- Desserts
('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 7.99, '/images/chocolate-cake.jpg', true, 3, NOW(), NOW()),
('Tiramisu', 'Classic Italian dessert with coffee and mascarpone', 8.99, '/images/tiramisu.jpg', true, 3, NOW(), NOW()),
('Ice Cream Sundae', 'Vanilla ice cream with chocolate sauce and nuts', 6.99, '/images/ice-cream-sundae.jpg', true, 3, NOW(), NOW()),

-- Beverages
('Coca Cola', 'Classic refreshing cola drink', 2.99, '/images/coca-cola.jpg', true, 4, NOW(), NOW()),
('Fresh Orange Juice', 'Freshly squeezed orange juice', 4.99, '/images/orange-juice.jpg', true, 4, NOW(), NOW()),
('Coffee', 'Freshly brewed coffee', 3.99, '/images/coffee.jpg', true, 4, NOW(), NOW()),
('Iced Tea', 'Refreshing iced tea with lemon', 3.49, '/images/iced-tea.jpg', true, 4, NOW(), NOW()),

-- Salads
('Caesar Salad', 'Crisp romaine lettuce with caesar dressing and croutons', 11.99, '/images/caesar-salad.jpg', true, 5, NOW(), NOW()),
('Greek Salad', 'Fresh vegetables with feta cheese and olives', 12.99, '/images/greek-salad.jpg', true, 5, NOW(), NOW()),
('Garden Salad', 'Mixed greens with seasonal vegetables', 9.99, '/images/garden-salad.jpg', true, 5, NOW(), NOW());

-- Insert sample users (passwords are 'password123' encoded with BCrypt)
INSERT INTO users (username, password, email, full_name, role, enabled, created_at, updated_at) VALUES
('admin', '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu', 'admin@resadmin.com', 'System Administrator', 'ADMIN', true, NOW(), NOW()),
('manager1', '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu', 'manager@resadmin.com', 'Restaurant Manager', 'MANAGER', true, NOW(), NOW()),
('chef1', '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu', 'chef@resadmin.com', 'Head Chef', 'KITCHEN_STAFF', true, NOW(), NOW()),
('driver1', '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu', 'driver1@resadmin.com', 'John Delivery', 'DELIVERY_STAFF', true, NOW(), NOW()),
('driver2', '$2a$10$h0tJl6dZu72Dc6XPbPqh6esHi72GBo5NndHmTRmjc2IG1WANj6dVu', 'driver2@resadmin.com', 'Jane Express', 'DELIVERY_STAFF', true, NOW(), NOW());

-- Insert sample orders
INSERT INTO orders (customer_details, order_type, status, total_price, created_at, updated_at) VALUES
('Name: John Doe | Phone: 555-1234 | Address: 123 Main Street, Apt 4B | Notes: Please ring doorbell', 'DELIVERY', 'PENDING', 45.97, NOW(), NOW()),
('Name: Jane Smith | Phone: 555-5678', 'PICKUP', 'CONFIRMED', 28.98, NOW(), NOW()),
('Name: Bob Johnson | Phone: 555-9012 | Address: 456 Oak Avenue | Notes: Leave at front desk', 'DELIVERY', 'PREPARING', 52.96, NOW(), NOW());

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, quantity, price, created_at, updated_at) VALUES
-- Order 1 items
(1, 4, 1, 16.99, NOW(), NOW()),  -- Margherita Pizza
(1, 1, 1, 12.99, NOW(), NOW()),  -- Chicken Wings
(1, 4, 1, 15.99, NOW(), NOW()),  -- Beef Burger

-- Order 2 items
(2, 5, 1, 18.99, NOW(), NOW()),  -- Pepperoni Pizza
(2, 9, 1, 7.99, NOW(), NOW()),   -- Chocolate Cake
(2, 12, 1, 2.99, NOW(), NOW()),  -- Coca Cola

-- Order 3 items
(3, 6, 1, 22.99, NOW(), NOW()),  -- Grilled Chicken Breast
(3, 16, 1, 11.99, NOW(), NOW()), -- Caesar Salad
(3, 10, 1, 8.99, NOW(), NOW()),  -- Tiramisu
(3, 14, 1, 4.99, NOW(), NOW()),  -- Fresh Orange Juice
(3, 15, 1, 3.99, NOW(), NOW());  -- Coffee
