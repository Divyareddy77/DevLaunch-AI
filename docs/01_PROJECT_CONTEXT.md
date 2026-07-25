# DevLaunch - Project Context

| Field | Details |
|--------|----------|
| Project Name | DevLaunch |
| Repository Name | DevLaunch-AI |
| Project Type | Full Stack Web Application |
| Domain | Career Development & Placement Preparation |
| Architecture | Modular Monolith |
| Development Methodology | Agile (Iterative Development) |
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Backend | Java 21, Spring Boot |
| Database | MySQL |
| Authentication | JWT Authentication |
| Cloud Platform | Microsoft Azure |
| Version | 1.0 |

---

# 1. Executive Summary

DevLaunch is a full-stack web application designed to simplify software career preparation by bringing multiple placement-related services into one centralized platform.

Students preparing for software jobs usually use several different applications for resume creation, coding practice, GitHub portfolio management, interview preparation, study planning, and job application tracking. Switching between these applications makes it difficult to monitor overall progress and wastes valuable preparation time.

DevLaunch solves this problem by integrating these services into a single web application. Users can manage their resumes, track job applications, monitor coding progress, prepare for interviews, organize study schedules, and visualize their overall placement readiness from one dashboard.

The project follows modern software engineering practices using Java, Spring Boot, React, TypeScript, MySQL, JWT Authentication, RabbitMQ, Docker, and Microsoft Azure.

---

# 2. Vision

To become a unified platform that helps aspiring software professionals prepare for placements through one intelligent and organized career development ecosystem.

---

# 3. Mission

To simplify software career preparation by integrating essential placement tools into a secure, scalable, and user-friendly web application.

---

# 4. Project Objectives

The primary objectives of DevLaunch are:

- Build one centralized platform for software career preparation.
- Reduce dependency on multiple third-party applications.
- Improve productivity during placement preparation.
- Help users monitor their career progress from one dashboard.
- Follow enterprise-level software engineering practices.
- Develop a scalable application that supports future enhancements.

---

# 5. Problem Statement

Students preparing for software placements rely on several disconnected platforms for different activities.

Examples include:

- Canva or Resume.io for resume creation
- LeetCode for coding practice
- GitHub for project management
- ChatGPT for interview preparation
- Excel or Huntr for job tracking
- Notion for study planning

Using multiple applications creates several problems:

- Career information is scattered across different platforms.
- Users repeatedly enter the same information.
- Progress cannot be tracked from one location.
- Interview preparation lacks centralized monitoring.
- Managing multiple tools reduces productivity.

---

# 6. Proposed Solution

DevLaunch provides one centralized platform where users can:

- Build professional resumes
- Track software job applications
- Prepare for HR and technical interviews
- Monitor GitHub activity
- Track LeetCode progress
- Plan daily study schedules
- Receive career insights
- View complete placement readiness from one dashboard

---

# 7. Business Objectives

The application aims to:

- Improve placement preparation efficiency.
- Provide a better learning experience.
- Encourage continuous skill development.
- Organize career-related information.
- Build a maintainable and scalable software platform.

---

# 8. Target Users

## Primary Users

- College Students
- Fresh Graduates
- Software Job Seekers

## Secondary Users

- Placement Coordinators
- Career Mentors
- Software Developers

---

# 9. Project Scope

## Phase 1 (MVP)

The first version of DevLaunch includes:

- User Authentication
- Resume Builder
- Job Application Tracker
- Study Planner
- AI Mock Interview
- GitHub Analytics
- LeetCode Progress Tracker
- Dashboard
- Notification System
- Admin Panel

## Phase 2 (Future Enhancements)

Future releases may include:

- Google Login
- GitHub Login
- LinkedIn Integration
- Portfolio Generator
- ATS Resume Analyzer
- Mobile Application

---

# 10. Module Overview

The application consists of four major business domains.

## Identity Domain

Responsible for user management.

Modules:

- User Registration
- Login
- JWT Authentication
- User Profile
- Role Management

---

## Career Domain

Responsible for placement preparation.

Modules:

- Resume Builder
- Job Application Tracker
- Achievement Management

---

## Learning Domain

Responsible for technical skill development.

Modules:

- Study Planner
- AI Mock Interview
- GitHub Analytics
- LeetCode Progress Tracker

---

## Platform Domain

Responsible for system-wide services.

Modules:

- Notification Service
- Feedback Management
- Administration

---

# 11. High-Level Module Diagram

```
                           DevLaunch
                                │
      ┌──────────────┬──────────────┬──────────────┬──────────────┐
      │              │              │              │
  Identity       Career        Learning       Platform
      │              │              │              │
Authentication  Resume      Study Planner   Notifications
Profile         Job Tracker Mock Interview  Admin
Roles           Dashboard   GitHub          Feedback
                            LeetCode
```

---

# 12. Core Features

- Secure User Authentication
- Resume Builder
- Job Application Tracking
- AI Mock Interview
- GitHub Analytics
- LeetCode Progress Tracking
- Study Planner
- Placement Dashboard
- Notification Management
- Administrative Dashboard

---

# 13. Technology Stack

## Frontend

- React
- TypeScript
- Vite
- Tailwind CSS
- React Router
- Axios
- Context API

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT Authentication
- RabbitMQ
- Lombok
- MapStruct

## Database

- MySQL

## DevOps

- Docker
- GitHub Actions
- Microsoft Azure

---

# 14. High-Level Architecture

The application follows a Modular Monolith Architecture.

```
+-------------------------------+
|       React Frontend          |
+---------------+---------------+
                |
                | REST APIs
                |
+---------------v---------------+
|     Spring Boot Backend       |
|-------------------------------|
| Authentication Module         |
| Resume Module                 |
| Job Tracker Module            |
| Dashboard Module              |
| Study Planner Module          |
| GitHub Analytics Module       |
| Notification Module           |
+---------------+---------------+
                |
                |
+---------------v---------------+
|          MySQL Database       |
+-------------------------------+
                |
                |
+---------------v---------------+
| RabbitMQ Notification Service |
+-------------------------------+
```

---

# 15. Development Methodology

The project follows Agile software development with an iterative approach.

Each module will pass through the following stages:

1. Requirement Analysis
2. System Design
3. Backend Development
4. Frontend Development
5. Testing
6. Code Review
7. Integration
8. Deployment

---

# 16. Git Workflow

The project follows a Git Flow-inspired branching strategy.

```
main
│
└── develop
      │
      ├── feature/documentation
      ├── feature/backend-auth
      ├── feature/resume-builder
      ├── feature/job-tracker
      ├── feature/dashboard
      ├── feature/github-analytics
      ├── feature/leetcode-tracker
      ├── feature/study-planner
      ├── feature/notification
      ├── feature/admin
      └── feature/deployment
```

Feature branches are merged into **develop**, and stable milestones are merged into **main**.

---

# 17. Coding Standards

The project follows modern software engineering practices.

- Clean Architecture
- Layered Architecture
- SOLID Principles
- DTO Pattern
- Repository Pattern
- REST API Standards
- Global Exception Handling
- Bean Validation
- Conventional Git Commits
- Clean Code Principles

---

# 18. Security Strategy

Security measures implemented in the application include:

- JWT Authentication
- Password Encryption using BCrypt
- Role-Based Authorization
- Input Validation
- Secure REST APIs
- Environment-based Configuration
- Global Exception Handling

---

# 19. Deployment Strategy

The application will be deployed using Microsoft Azure.

Deployment includes:

- React Frontend
- Spring Boot Backend
- MySQL Database
- RabbitMQ
- Docker Containers
- GitHub Actions for CI/CD

---

# 20. Success Criteria

The project will be considered successful if users are able to:

- Register and securely log in.
- Build professional resumes.
- Track software job applications.
- Monitor GitHub and LeetCode progress.
- Prepare for interviews.
- Manage study schedules.
- View career progress from one dashboard.

---

# 21. Future Enhancements

Future versions of DevLaunch may include:

- Google Authentication
- GitHub Authentication
- AI Resume Review
- ATS Resume Score
- LinkedIn Profile Analysis
- Portfolio Website Generator
- Placement Readiness Score
- Mobile Application

---

# 22. Conclusion

DevLaunch is designed as a scalable, secure, and modular career development platform that brings multiple placement preparation services into one application.

The project follows modern software engineering principles, industry-standard architecture, and clean development practices. It serves as both a practical career management solution for users and a production-quality portfolio project demonstrating full-stack development skills.