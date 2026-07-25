# DevLaunch - API Contract

## Project Information

| Field | Details |
|--------|----------|
| Project | DevLaunch |
| API Style | REST |
| Data Format | JSON |
| Authentication | JWT Bearer Token |

---

# 1. Introduction

This document defines the REST API endpoints exposed by the DevLaunch backend.

All APIs exchange data using JSON and follow RESTful principles.

Protected endpoints require JWT authentication.

---

# 2. Authentication APIs

## Register User

POST /api/auth/register

### Request

```json
{
  "firstName": "Divya",
  "lastName": "Reddy",
  "email": "divya@gmail.com",
  "password": "Password@123"
}
```

### Response

```json
{
  "message": "Registration Successful"
}
```

---

## Login

POST /api/auth/login

### Request

```json
{
  "email":"divya@gmail.com",
  "password":"Password@123"
}
```

### Response

```json
{
  "accessToken":"JWT_TOKEN"
}
```

---

## Forgot Password

POST /api/auth/forgot-password

---

## Reset Password

POST /api/auth/reset-password

---

# 3. User APIs

GET /api/users/profile

PUT /api/users/profile

DELETE /api/users/profile

---

# 4. Resume APIs

GET /api/resumes

GET /api/resumes/{id}

POST /api/resumes

PUT /api/resumes/{id}

DELETE /api/resumes/{id}

---

# 5. Job Application APIs

GET /api/jobs

GET /api/jobs/{id}

POST /api/jobs

PUT /api/jobs/{id}

DELETE /api/jobs/{id}

PATCH /api/jobs/{id}/status

---

# 6. Study Planner APIs

GET /api/study-plans

POST /api/study-plans

PUT /api/study-plans/{id}

DELETE /api/study-plans/{id}

---

# 7. Study Task APIs

GET /api/tasks

POST /api/tasks

PUT /api/tasks/{id}

DELETE /api/tasks/{id}

---

# 8. Mock Interview APIs

POST /api/interviews/start

POST /api/interviews/submit

GET /api/interviews/history

---

# 9. GitHub APIs

POST /api/github/connect

GET /api/github/profile

GET /api/github/statistics

---

# 10. LeetCode APIs

POST /api/leetcode/connect

GET /api/leetcode/profile

GET /api/leetcode/statistics

---

# 11. Notification APIs

GET /api/notifications

PATCH /api/notifications/{id}/read

DELETE /api/notifications/{id}

---

# 12. Admin APIs

GET /api/admin/users

GET /api/admin/reports

POST /api/admin/announcements

DELETE /api/admin/users/{id}

---

# 13. Standard Response Format

Success Response

```json
{
  "success": true,
  "message": "Operation Successful",
  "data": {}
}
```

Error Response

```json
{
  "success": false,
  "message": "Validation Failed",
  "errors": []
}
```

---

# 14. Authentication

Protected APIs require:

```
Authorization: Bearer JWT_TOKEN
```

---

# 15. HTTP Status Codes

200 OK

201 Created

204 No Content

400 Bad Request

401 Unauthorized

403 Forbidden

404 Not Found

500 Internal Server Error

---

# Conclusion

The API contract defines all REST endpoints required by the DevLaunch application and serves as the implementation reference for the Spring Boot backend.