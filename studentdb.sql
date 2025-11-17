CREATE DATABASE studentdb;
USE studentdb;

CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    marks INT
);

INSERT INTO students (name, marks) VALUES
('Amit', 85),
('Neha', 90),
('Ravi', 78),
('Pooja', 92);
