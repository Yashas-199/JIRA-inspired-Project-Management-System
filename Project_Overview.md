# Project Management System (PMS) — Full Stack Overview

## 1) Project Summary
The Project Management System (PMS) is a JIRA-inspired full-stack application for managing projects, tasks, issues, comments, and notifications. It is built to demonstrate OOAD, SOLID, layered architecture, and clean backend design.

Key capabilities:
- User authentication and role-based access control
- Project creation and member management
- Task assignment and status transitions
- Issue (bug) tracking with lifecycle control
- Commenting on tasks and issues
- Event-driven notification system

## 2) Backend Stack and Architecture (Spring Boot, MVC)

### Technologies Used
- Java 21
- Spring Boot 3.5.x
- Spring Web (REST controllers)
- Spring Security + JWT
- Spring Data JPA + Hibernate
- Maven
- JUnit 5 + Mockito + MockMvc

### MVC / Layered Design
The backend follows a layered MVC-style structure:

- Controller layer: REST APIs and request/response handling
- Service layer: business rules and authorization checks
- Repository layer: database access through JPA
- Domain layer: entities, enums, and business rules

Package structure (high-level):
- auth/: login/register flow, auth DTOs, auth service
- user/, project/, task/, issue/, comment/, notification/
  - controller/: API endpoints
  - service/: business logic
  - repository/: JPA repositories
  - domain/: entities and enums
- security/: JWT filter, user details, security config
- events/: application events for notifications
- config/: data initializer
- common/exception/: global errors and custom exceptions

### Authentication and Authorization
- JWT-based authentication
- Roles: ADMIN, PROJECT_LEADER, TEAM_MEMBER
- Authorization rules enforced in service layer

### Notification System (Event-Driven)
Events are published by services and handled by listeners:

Service -> Event -> Listener -> NotificationService -> Database

Supported notification types:
- TASK_ASSIGNED
- ISSUE_ASSIGNED
- COMMENT_ADDED
- TASK_STATUS_CHANGED
- ISSUE_STATUS_CHANGED
- PROJECT_MEMBER_ADDED

## 3) Database (MySQL)

### Technology
- MySQL 8 (Docker container)
- Schema file at: database/schema.sql

### Core Tables
- users
- projects
- project_members (many-to-many)
- tasks
- issues
- comments
- notification

### Local DB Configuration
Defined in application.properties:
- jdbc:mysql://localhost:3306/project_management
- username: devuser
- password: Yash@2005

The schema is mounted into the MySQL container and executed on first startup.

## 4) Frontend Stack (Vite + React)

### Technologies Used
- React 18
- Vite 5
- TailwindCSS
- Axios
- Recharts
- Lucide Icons

### Local Dev Server
- Runs at: http://localhost:5173

### API Integration
Vite proxy forwards API calls:
- /api -> http://localhost:8080

This allows the frontend to call backend endpoints without CORS issues.

## 5) System Integration (End-to-End Flow)

1) User logs in from frontend
2) Backend authenticates and returns JWT token
3) Frontend stores token and attaches it to API calls
4) Backend enforces role checks at service layer
5) JPA persists changes in MySQL
6) Events trigger notifications when needed
7) Frontend fetches updated state via REST APIs

## 6) Key Backend API Base URL
- http://localhost:8080/api

Authentication header format:
- Authorization: Bearer <JWT_TOKEN>

## 7) How to Run the Project (Local)

### Step 1: Start MySQL (Docker)
From repository root:

```powershell
docker compose up -d
```

### Step 2: Start Backend (Spring Boot)

```powershell
# Option A
.\backend\pms\mvnw.cmd spring-boot:run

# Option B
Set-Location .\backend\pms
.\mvnw.cmd spring-boot:run
```

### Step 3: Start Frontend (Vite)

```powershell
Set-Location .\frontend
npm install
npm run dev
```

### Step 4: Open in Browser
- http://localhost:5173

### Stop Everything

```powershell
# Backend and frontend: Ctrl+C in each terminal
# MySQL container:
docker compose down
```

## 8) Testing Summary

- Unit tests: service layer (JUnit + Mockito)
- Controller tests: MockMvc with security disabled
- Event listeners are covered in service tests

Test reports are stored under:
- backend/pms/target/surefire-reports/

## 9) How the Project Works (Functional Flow)

1) Admin registers users and creates a project
2) Admin assigns a project leader
3) Project leader manages team members
4) Tasks are created and assigned
5) Issues can be reported and assigned
6) Comments provide discussion on tasks/issues
7) Status transitions are strictly validated
8) Notifications are generated for key events

## 10) Quick Reference: Core Modules

- Auth: login, register, JWT handling
- Projects: lifecycle, members, leader management
- Tasks: status lifecycle (TODO -> IN_PROGRESS -> DONE)
- Issues: lifecycle (OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED)
- Comments: immutable discussion entities
- Notifications: event-driven updates

---

If you want, I can also add:
- A visual architecture diagram
- A database ER diagram
- A short API cheatsheet for frontend calls
