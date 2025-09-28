@echo off
REM Start Eureka Server
start "Eureka Server" cmd /k "cd eureka-server && mvn spring-boot:run -X"
REM Start API Gateway
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run -X"
REM Start TaskTracker Service
start "TaskTracker service" cmd /k "cd tasktracker && mvn spring-boot:run -X"
REM Start User Profile Service
start "User Profile Service" cmd /k "cd user-profile-service && mvn spring-boot:run -X"
REM Start File Attachment Service
start "File Attachment Service" cmd /k "cd file-attachment-service && mvn spring-boot:run -X"
REM Optionally, start main backend
REM start "Main Backend" cmd /k "cd .. && mvnw spring-boot:run"
@echo All servers started in separate windows.
