# DevLaunch - Software Requirements Specification (SRS)

## Project Information

| Field | Details |
|--------|----------|
| Project Name | DevLaunch |
| Repository | DevLaunch-AI |
| Document | Software Requirements Specification |
| Version | 1.0 |

---

# 1. Introduction

This document defines the functional and non-functional requirements of the DevLaunch application.

It serves as the foundation for designing the backend, frontend, database, REST APIs, and user interface.

All future development activities should follow the requirements defined in this document.

---

# 2. Purpose

The purpose of DevLaunch is to provide students and job seekers with a centralized platform for software career preparation.

The application integrates multiple career-related activities including:

- Resume Building
- Job Application Tracking
- AI Mock Interviews
- Study Planning
- GitHub Analytics
- LeetCode Progress Tracking
- Career Dashboard

---

# 3. User Roles

The application supports two user roles.

## Student

Students can:

- Register
- Login
- Manage Profile
- Build Resume
- Track Job Applications
- Manage Study Plans
- Practice Mock Interviews
- View Dashboard
- Receive Notifications

---

## Admin

Administrators can:

- Login
- Manage Users
- Manage Announcements
- View Reports
- Monitor Platform Usage
- Review Feedback

---

# 4. Functional Requirements

---

## Module 1 - Authentication

### FR-1

The system shall allow users to register using email and password.

### FR-2

The system shall validate user input before registration.

### FR-3

The system shall encrypt passwords using BCrypt.

### FR-4

The system shall allow users to login.

### FR-5

The system shall generate JWT tokens after successful login.

### FR-6

The system shall support forgot password functionality.

### FR-7

The system shall allow users to reset passwords.

### FR-8

The system shall allow users to update profile information.

---

## Module 2 - Resume Builder

### FR-9

Users shall create multiple resumes.

### FR-10

Users shall edit resumes.

### FR-11

Users shall delete resumes.

### FR-12

Users shall preview resumes.

### FR-13

Users shall download resumes as PDF.

### FR-14

Users shall select different resume templates.

---

## Module 3 - Job Application Tracker

### FR-15

Users shall add job applications.

### FR-16

Users shall edit application details.

### FR-17

Users shall delete job applications.

### FR-18

Users shall update application status.

Supported Statuses:

- Wishlist
- Applied
- Assessment
- Interview
- Offer
- Rejected

### FR-19

Users shall search applications.

### FR-20

Users shall filter applications.

---

## Module 4 - AI Mock Interview

### FR-21

Users shall select interview categories.

Categories:

- HR
- Java
- Spring Boot
- SQL
- React

### FR-22

The system shall generate interview questions.

### FR-23

The system shall store interview history.

### FR-24

The system shall generate interview scores.

---

## Module 5 - GitHub Analytics

### FR-25

Users shall connect their GitHub profile.

### FR-26

The system shall display repository statistics.

### FR-27

The system shall display programming language usage.

### FR-28

The system shall display contribution statistics.

---

## Module 6 - LeetCode Progress

### FR-29

Users shall connect their LeetCode username.

### FR-30

The system shall display solved problems.

### FR-31

The system shall display difficulty-wise analysis.

### FR-32

The system shall display daily streak information.

---

## Module 7 - Study Planner

### FR-33

Users shall create study plans.

### FR-34

Users shall create daily tasks.

### FR-35

Users shall update task status.

### FR-36

Users shall delete tasks.

### FR-37

Users shall monitor study progress.

---

## Module 8 - Dashboard

### FR-38

The dashboard shall display resume statistics.

### FR-39

The dashboard shall display job application statistics.

### FR-40

The dashboard shall display GitHub analytics.

### FR-41

The dashboard shall display LeetCode statistics.

### FR-42

The dashboard shall display study progress.

### FR-43

The dashboard shall display interview performance.

---

## Module 9 - Notification Service

### FR-44

The system shall notify users about interview schedules.

### FR-45

The system shall notify users about study reminders.

### FR-46

The system shall notify users about application updates.

---

## Module 10 - Admin Module

### FR-47

Admins shall manage users.

### FR-48

Admins shall manage announcements.

### FR-49

Admins shall view reports.

### FR-50

Admins shall review user feedback.

---

# 5. Non-Functional Requirements

## Security

- JWT Authentication
- Password Encryption
- Role-Based Authorization
- Input Validation

---

## Performance

- API response time should be under 2 seconds for normal operations.
- The application should efficiently handle concurrent users.

---

## Scalability

The architecture should support future feature expansion without significant redesign.

---

## Availability

The application should be available whenever users need to access their placement data.

---

## Maintainability

The codebase should follow clean architecture and modular design principles.

---

## Usability

The user interface should be responsive, intuitive, and easy to navigate.

---

# 6. Business Rules

BR-1

Each email address must be unique.

BR-2

Passwords must be encrypted before storage.

BR-3

Only authenticated users may access protected resources.

BR-4

Users may only edit their own data.

BR-5

Only administrators can manage announcements.

BR-6

Deleted job applications cannot be recovered.

---

# 7. Assumptions

- Users have internet connectivity.
- Users provide valid information.
- GitHub profiles are publicly accessible.
- External APIs are available during normal operation.

---

# 8. Constraints

- Initial version supports web browsers only.
- Google Login is not included in Version 1.
- Mobile application is out of scope.
- Development timeline is one week.

---

# 9. Acceptance Criteria

The project will be considered complete when users can:

- Register and login securely.
- Build professional resumes.
- Track job applications.
- Manage study plans.
- Complete mock interviews.
- View GitHub analytics.
- Track LeetCode progress.
- View dashboard analytics.
- Receive notifications.
- Logout securely.

---

# 10. Conclusion

This document defines the complete functional and non-functional requirements for DevLaunch.

These requirements will serve as the reference for designing the architecture, database schema, REST APIs, frontend interface, and testing strategy.