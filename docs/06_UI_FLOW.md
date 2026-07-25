# DevLaunch - UI Flow

## Project Information

| Field | Details |
|--------|----------|
| Project | DevLaunch |
| Frontend | React + TypeScript |
| UI Framework | Tailwind CSS |
| Routing | React Router DOM |

---

# 1. Introduction

This document describes the navigation flow and screen structure of the DevLaunch application.

The objective is to provide users with a simple, responsive, and intuitive interface while maintaining a clean navigation experience.

---

# 2. Application Flow

```
Landing Page
      │
      ▼
Login / Register
      │
      ▼
Authentication
      │
      ▼
Dashboard
      │
 ┌────┼────────┬─────────┬─────────┬────────┐
 ▼    ▼        ▼         ▼         ▼        ▼
Resume Jobs Study GitHub LeetCode Profile
      │
      ▼
Notifications
```

---

# 3. Authentication Flow

```
Landing Page
      │
      ▼
Register
      │
      ▼
Login
      │
      ▼
JWT Authentication
      │
      ▼
Dashboard
```

---

# 4. Dashboard

The dashboard is the central screen of the application.

It displays:

- Welcome Message
- Resume Statistics
- Job Application Summary
- GitHub Analytics
- LeetCode Progress
- Study Progress
- Interview Performance
- Recent Notifications

---

# 5. Navigation Menu

The sidebar contains:

- Dashboard
- Resume Builder
- Job Tracker
- Study Planner
- AI Mock Interview
- GitHub Analytics
- LeetCode Tracker
- Notifications
- Profile
- Logout

Admin users additionally see:

- User Management
- Reports
- Announcements

---

# 6. Resume Builder Flow

```
Dashboard
      │
      ▼
Resume List
      │
 ┌────┼─────────────┐
 ▼    ▼             ▼
Create Edit       Delete
      │
      ▼
Preview
      │
      ▼
Download PDF
```

---

# 7. Job Tracker Flow

```
Dashboard
      │
      ▼
Job Applications
      │
 ┌────┼────────────┬─────────────┐
 ▼    ▼            ▼             ▼
Add Edit       Update Status   Delete
```

Supported Statuses:

- Wishlist
- Applied
- Assessment
- Interview
- Offer
- Rejected

---

# 8. Study Planner Flow

```
Dashboard
      │
      ▼
Study Plans
      │
      ▼
Tasks
      │
 ┌────┼────────────┐
 ▼    ▼            ▼
Add Complete    Delete
```

---

# 9. AI Mock Interview Flow

```
Dashboard
      │
      ▼
Choose Category
      │
      ▼
Interview Questions
      │
      ▼
Submit Answers
      │
      ▼
Feedback & Score
```

Categories:

- HR
- Java
- Spring Boot
- SQL
- React

---

# 10. GitHub Analytics Flow

```
Dashboard
      │
      ▼
Connect GitHub
      │
      ▼
GitHub Statistics
```

Displays:

- Repository Count
- Programming Languages
- Followers
- Following

---

# 11. LeetCode Flow

```
Dashboard
      │
      ▼
Connect Username
      │
      ▼
Progress Statistics
```

Displays:

- Easy Solved
- Medium Solved
- Hard Solved
- Total Solved

---

# 12. Notifications Flow

```
Dashboard
      │
      ▼
Notifications
      │
 ┌────┼────────────┐
 ▼    ▼            ▼
View Mark Read Delete
```

---

# 13. Profile Flow

```
Dashboard
      │
      ▼
Profile
      │
 ┌────┼────────────┐
 ▼    ▼            ▼
View Edit Change Password
```

---

# 14. Admin Flow

```
Admin Dashboard
      │
 ┌────┼────────────┬────────────┐
 ▼    ▼            ▼            ▼
Users Reports Announcements Feedback
```

---

# 15. Responsive Design

The application supports:

- Desktop
- Laptop
- Tablet
- Mobile

The layout adapts automatically using responsive design principles.

---

# 16. UI Design Principles

The interface follows these principles:

- Clean Layout
- Consistent Navigation
- Minimal Clicks
- Responsive Design
- Accessibility
- Modern UI Components
- User-Friendly Forms

---

# 17. Conclusion

The DevLaunch UI is designed to provide an organized and intuitive user experience.

Every major feature is accessible from the dashboard, allowing users to manage their complete placement journey from one centralized application.