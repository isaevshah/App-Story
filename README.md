# 🛒 Kupidai.kz — E-commerce Platform for Cross-Border Sales

Kupidai.kz is a microservice-based e-commerce platform built using **Java Spring Boot**. It facilitates cross-border sales of goods from China to Kazakhstan, with secure 2FA login, order management, catalog filtering, QR code-based logistics, and payment integration.

## 🌐 Features

- 🔐 **Authentication with 2FA OTP (Email-based)**
  - Role-based access: `ADMIN`, `MANAGER`, `WAREHOUSE_WORKER`, `CUSTOMER`

- 💳 **Payment Integration**
  - Currently via Kaspi QR (client uploads receipt manually)
  - Planned: Robokassa integration for direct card payments

- 🧾 **Catalog and Filters**
  - Multi-category product catalog
  - Full product descriptions
  - Filtering by price

- 📬 **Email Notifications**
  - OTP for login
  - No spam policy

- 📦 **QR Code Generation**
  - After approval, manager in China ships product to Kazakhstan
  - QR code is automatically generated with full order information

- 📡 **RESTful API with OpenAPI/Swagger**
  - Swagger UI enabled for testing and documentation

## 🛠 Tech Stack

- **Java 21 + Spring Boot**
- **PostgreSQL** for relational data
- **Docker + Railway** for deployment
- **Swagger (OpenAPI)** for API documentation
- **Email Service** for 2FA
- **QR Code Generator** for logistics
- **Role-based access control (RBAC)**

## 🚀 Run Locally
