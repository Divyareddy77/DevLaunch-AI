# DevLaunch - System Architecture

## Project Information

| Field | Details |
|--------|----------|
| Project Name | DevLaunch |
| Document | System Architecture |
| Architecture Style | Modular Monolith |
| Backend | Spring Boot |
| Frontend | React + TypeScript |
| Database | MySQL |

---

# 1. Introduction

This document describes the overall architecture of the DevLaunch application.

The project follows a **Modular Monolith Architecture** with **Layered Architecture** principles. Each module is logically separated while remaining within a single deployable Spring Boot application.

This approach provides clean code organization, easier maintenance, and the flexibility to migrate individual modules into microservices in the future if required.

---

# 2. High-Level Architecture

```
                    +---------------------------+
                    |     React Frontend        |
                    |   (TypeScript + Vite)     |
                    +------------+--------------+
                                 |
                           HTTP / REST APIs
                                 |
                    +------------v--------------+
                    |    Spring Boot Backend    |
                    +------------+--------------+
                                 |
             +-------------------+-------------------+
             |                   |                   |
      Authentication       Business Logic       Notifications
             |                   |                   |
             +-------------------+-------------------+
                                 |
                         Spring Data JPA
                                 |
                    +------------v--------------+
                    |        MySQL Database      |
                    +---------------------------+
```

---

# 3. Architecture Pattern

The application follows a **Modular Monolith** architecture.

Each feature is developed as an independent module while sharing a common application runtime.

Modules include:

- Authentication
- Resume Builder
- Job Tracker
- Study Planner
- AI Mock Interview
- GitHub Analytics
- LeetCode Tracker
- Dashboard
- Notification Service
- Admin Module

---

# 4. Layered Architecture

Each module follows the same layered structure.

```
Controller
      │
      ▼
Service Interface
      │
      ▼
Service Implementation
      │
      ▼
Repository
      │
      ▼
Database
```

Supporting layers:

- DTO
- Entity
- Mapper
- Validation
- Exception Handling
- Security
- Configuration

---

# 5. Backend Package Structure

```
backend/
└── src/main/java/com/devlaunch
    ├── config
    ├── controller
    ├── dto
    │   ├── request
    │   └── response
    ├── entity
    ├── exception
    ├── mapper
    ├── repository
    ├── security
    ├── service
    │   ├── interfaces
    │   └── impl
    ├── validation
    ├── util
    └── DevLaunchApplication.java
```

---

# 6. Frontend Structure

```
frontend/
src/
│
├── api
├── assets
├── components
├── context
├── hooks
├── layouts
├── pages
├── routes
├── services
├── types
├── utils
└── App.tsx
```

---

# 7. Module Responsibilities

### Authentication

- Registration
- Login
- JWT Authentication
- Profile Management

---

### Resume Builder

- Resume CRUD
- Resume Templates
- PDF Export

---

### Job Tracker

- Add Job Applications
- Update Status
- Search & Filter

---

### Study Planner

- Daily Tasks
- Weekly Planner
- Progress Tracking

---

### Dashboard

- Analytics
- Progress Summary
- Career Statistics

---

### Notification Service

- Interview Reminders
- Study Reminders
- Application Updates

---

### Admin

- User Management
- Reports
- Announcements

---

# 8. Request Flow

```
User

↓

React UI

↓

Axios

↓

REST API

↓

Controller

↓

Service

↓

Repository

↓

MySQL
```

---

# 9. Security Architecture

Security components:

- Spring Security
- JWT Authentication
- BCrypt Password Encryption
- Role-Based Authorization
- Request Validation
- Global Exception Handling

---

# 10. Database Access

The application uses:

- Spring Data JPA
- Hibernate ORM
- Repository Pattern

Business logic never directly interacts with the database.

---

# 11. Exception Handling

The project uses centralized exception handling.

Examples:

- ResourceNotFoundException
- DuplicateEmailException
- UnauthorizedException
- ValidationException

These are handled using a global exception handler to return consistent API responses.

---

# 12. Logging Strategy

Application logging will be implemented using Spring Boot logging.

Logs include:

- Authentication Events
- API Requests
- Errors
- Warnings

Sensitive information such as passwords and tokens will never be logged.

---

# 13. Deployment Architecture

```
React Frontend
       │
       ▼
Azure App Service

Spring Boot Backend
       │
       ▼
Azure App Service

MySQL Database
       │
       ▼
Azure Database for MySQL

RabbitMQ
       │
       ▼
Azure / Docker Container
```

---

# 14. Architectural Principles

The application follows these principles:

- Separation of Concerns
- Single Responsibility Principle
- Open/Closed Principle
- Dependency Injection
- Reusability
- Scalability
- Maintainability

---

# 15. Conclusion

The DevLaunch architecture is designed to provide a clean, modular, secure, and scalable foundation for the application.

The modular monolith approach allows rapid development while maintaining the flexibility to migrate individual modules into microservices in future versions if required.