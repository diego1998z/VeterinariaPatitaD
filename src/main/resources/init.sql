-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS patitas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE patitas_db;

-- Crear usuario si no existe
CREATE USER IF NOT EXISTS 'patitasuser'@'localhost' IDENTIFIED BY 'patitas123';
GRANT ALL PRIVILEGES ON patitas_db.* TO 'patitasuser'@'localhost';
FLUSH PRIVILEGES;