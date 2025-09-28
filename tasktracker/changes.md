# TaskTracker Changes

Hi Mario! Here's what we changed in the TaskTracker app today.

## What We Started With
The app was broken. It couldn't start and had default web pages provided by Spring Boot itself for sampling the login page for users.

## What We Fixed and Added

### 1. Database Connection
**File:** `application.properties`

**Problem:** The app couldn't connect to any database.
**Solution:** We added MySQL database settings so the app can save and load data.

### 2. Web Pages 
**Files:** `login.html` and `home.html`

**Problem:** There were no web pages. Users had nowhere to go.
**Solution:** We created:
- A login page where users can sign in or create new accounts
- A home page that welcomes users after they log in

### 3. Making Pages Work
**Files:** `LoginController.java` and `HomeController.java`

**Problem:** The app didn't know how to show web pages.
**Solution:** We added code that tells the app which pages to show when.

### 4. User Registration
**File:** `AuthController.java`

**Problem:** Only programmers could create user accounts by typing commands.
**Solution:** Now anyone can create an account using the website.

### 5. Security Settings
**File:** `SecurityConfig.java`

**Problem:** The login system was ugly and confusing.
**Solution:** We made it use our nice-looking login page instead.

### 6. Template Engine
**File:** `pom.xml`

**Problem:** The app couldn't make pretty web pages.
**Solution:** We added Thymeleaf so the app can create beautiful pages.

### 7. Environment Variables (.env file) - NEW SECURITY FEATURE!
**Files:** `.env`, `DotenvConfig.java`, `spring.factories`, updated `pom.xml` and `application.properties`

**Problem:** Database passwords were stored in plain text in the code.
**Solution:** We created a secure .env file to store sensitive information like passwords.

## Before vs After

### Before:
- App crashed when you tried to start it
- No website - just error messages
- Couldn't create user accounts through the web
- Everything looked ugly
- Database passwords were visible in code

### After:
- App starts perfectly
- Beautiful login and home pages
````markdown
# TaskTracker Changes

## Session on September 28, 2025

### 🧩 Microservices Integration & Testing Interface

**What’s New:**
- Introduced two new standalone microservices:
  - **User Profile Service**: Full CRUD and search for user profiles
  - **File Attachment Service**: File upload, download, search, and metadata management
- Added a **Microservices Test Lab** page to the frontend for direct interaction with both microservices (create, search, delete, upload, download, health check, and demo data)
- Updated the dashboard and navigation to include a quick action and feature card for microservices testing
- All microservice endpoints are now accessible via the API Gateway

**Technical Improvements:**
- Enhanced both microservices with comprehensive endpoints and in-memory storage for demo/testing
- Fixed Java 11 compatibility issues (replaced Java 16+ records and .toList() with standard classes and Collectors.toList())
- Updated security configuration to ensure authenticated access to the new test page
- Improved error handling and health monitoring for microservices

**Frontend Enhancements:**
- Created a modern, interactive test page for microservices with dark/light mode, tabbed panels, and real-time feedback
- Added navigation links and feature cards for easy access to microservices testing

**How to Use:**
1. Start all servers (Eureka, Gateway, both microservices, and main app)
2. Log in to the main dashboard
3. Click the new “🧪 Test Microservices” button or feature card
4. Use the Microservices Test Lab to create/search/delete profiles, upload/download/search files, and check service health

**Result:**
You can now fully test and demonstrate the new microservices directly from the frontend, with all features accessible through a unified, user-friendly interface.

---
*Updated on September 28, 2025*
````
### Prerequisites (Do this once):
