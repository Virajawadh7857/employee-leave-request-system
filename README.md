# Employee Leave Request System

A REST API built with Spring Boot to manage employee leave requests.

## Technology Stack
- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- MySQL
- Maven
- Postman

## Features
- Create, retrieve, update, and delete leave requests
- Validate required fields and leave date ranges
- Handle errors with appropriate HTTP status codes

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/leave-requests | Create a request |
| GET | /api/leave-requests | Retrieve all requests |
| GET | /api/leave-requests/{id} | Retrieve a request by ID |
| PUT | /api/leave-requests/{id} | Update a request |
| DELETE | /api/leave-requests/{id} | Delete a request |

## Run Locally
1. Install Java 17 and MySQL.
2. Create the database `leave_request_db`.
3. Configure your local database credentials in `application.properties`.
4. Start the application with `.\mvnw.cmd spring-boot:run`.

## Security
Never commit passwords, API keys, or other secrets.
