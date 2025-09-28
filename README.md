# TaskTracker Microservices Edition

## Overview
TaskTracker is a modern, secure, and extensible task management platform built with Spring Boot, Spring Cloud, and a microservices architecture. It features JWT-based authentication, a neo-brutalist frontend, and robust support for user profiles and file attachments via dedicated microservices.

---

## Features
- **Monolithic Core**: Main TaskTracker app with user authentication, task management, categories, analytics, and notifications
- **Microservices**:
  - **User Profile Service**: Standalone service for managing user profiles (CRUD, search)
  - **File Attachment Service**: Standalone service for file uploads, downloads, and metadata management
- **API Gateway**: Spring Cloud Gateway for unified routing and security
- **Service Discovery**: Eureka server for dynamic service registration
- **JWT Security**: Secure authentication and authorization across all services
- **Neo-Brutalist Frontend**: Modern, responsive UI with dark/light mode and interactive dashboard
- **Microservices Test Lab**: Built-in frontend page for testing all microservice endpoints

---

## Architecture
- **Spring Boot 3.1.8** (core app, microservices)
- **Spring Cloud Netflix Eureka** (service discovery)
- **Spring Cloud Gateway** (API gateway)
- **JWT (JJWT library)** (security)
- **Thymeleaf** (frontend templates)
- **Maven** (build system)

---

## Getting Started

### Prerequisites
- Java 17+
- Maven
- MySQL (for main app)

### Running the Application
1. **Start Eureka Server**
2. **Start API Gateway**
3. **Start User Profile Service**
4. **Start File Attachment Service**
5. **Start Main TaskTracker App**

Or use the provided `start-all-servers.bat` script to launch all components.

### Accessing the App
- Main Dashboard: [http://localhost:8082/](http://localhost:8082/)
- Microservices Test Lab: [http://localhost:8082/microservices-test](http://localhost:8082/microservices-test)

---

## Microservices Test Lab
A dedicated frontend page for:
- Creating, searching, and deleting user profiles
- Uploading, searching, downloading, and deleting files
- Health checks for each microservice
- Demo data generation for quick testing

---

## Security
- JWT-based authentication for all endpoints
- BCrypt password hashing
- Centralized security configuration
- Custom login and registration pages

---

## Development Notes
- All microservices are now Java 11 compatible (no records, no .toList())
- API Gateway routes all /profile and /file requests to the respective microservices
- The frontend is fully integrated with the microservices via the gateway
- All configuration and sensitive data are managed via environment variables and .env files

---

## Recent Changes (September 28, 2025)
- Added comprehensive microservices test page to the frontend
- Enhanced User Profile and File Attachment microservices with full CRUD and search
- Fixed Java 11 compatibility issues (removed records, .toList())
- Updated navigation and dashboard to include microservices testing
- Improved security and error handling across all services

---

## Authors
- Mario (original project)
- GurleenStyles (microservices, security, and frontend enhancements)

---

## License
MIT
