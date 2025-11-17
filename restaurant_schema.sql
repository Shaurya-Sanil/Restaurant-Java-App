-- Restaurant DB schema and sample data
CREATE DATABASE IF NOT EXISTS restaurantdb;
USE restaurantdb;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS dining_tables;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100)
);

CREATE TABLE dining_tables (
  id INT AUTO_INCREMENT PRIMARY KEY,
  table_number VARCHAR(10) UNIQUE,
  capacity INT NOT NULL,
  status ENUM('free','reserved','occupied') DEFAULT 'free'
);

CREATE TABLE reservations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT,
  table_id INT,
  reservation_time DATETIME,
  party_size INT,
  status ENUM('booked','cancelled','completed') DEFAULT 'booked',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
  FOREIGN KEY (table_id) REFERENCES dining_tables(id) ON DELETE SET NULL
);

CREATE TABLE menu_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  price DECIMAL(8,2) NOT NULL,
  is_available BOOLEAN DEFAULT TRUE
);

CREATE TABLE orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT,
  order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status ENUM('placed','preparing','ready','completed','cancelled') DEFAULT 'placed',
  total_amount DECIMAL(10,2) DEFAULT 0,
  payment_method VARCHAR(50),
  FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

CREATE TABLE order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT,
  menu_item_id INT,
  quantity INT,
  price DECIMAL(8,2),
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE SET NULL
);

-- Sample data
INSERT INTO dining_tables (table_number, capacity) VALUES ('T1',2),('T2',4),('T3',4),('T4',6);
INSERT INTO menu_items (name, description, price) VALUES
('Margherita Pizza','Classic tomato + mozzarella',8.99),
('Paneer Butter Masala','Indian curry with paneer',7.50),
('French Fries','Crispy fries',3.25),
('Coke','330ml can',1.50);

INSERT INTO customers (name, phone, email) VALUES ('Amit', '9876543210', 'amit@example.com'),('Neha','9123456780','neha@example.com');
