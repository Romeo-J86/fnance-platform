Based on the project structure and the code I've analyzed so far, here is a comprehensive README file for the Finance Platform.

# Finance Platform

A comprehensive microservices-based Finance Platform designed for managing users, financial transactions, and AI-driven insights. The system is built using modern Java technologies and follows a distributed architecture pattern.

## Project Overview

The Finance Platform is split into several microservices to ensure scalability and maintainability. It provides secure user authentication (including role-based access control), financial data management, and integration with AI services.

### Key Features

*   **Microservices Architecture:** Modular design with distinct services for Authentication, Finance, AI, and Gateway.
*   **Secure Authentication:** JWT-based security with role-based access control (User, Admin).
*   **User Management:** robust user registration flows, including admin registration and invitation-based user onboarding.
*   **Invitation System:** Secure token-based invitation system for onboarding new users with specific roles.
*   **AI Integration:** Dedicated service for handling AI-related financial operations (implied by structure).
*   **Service Discovery:** Built-in service discovery for dynamic service registration.

## Project Structure

The project is organized into the following modules:

*   **`api-gateway`**: Entry point for all client requests, handling routing and cross-cutting concerns.
*   **`auth-service`**: Handles user registration, login, JWT token generation/validation, and the invitation system.
*   **`finance-service`**: Core business logic for financial operations (transactions, accounts, etc.).
*   **`ai-service`**: microservice dedicated to Artificial Intelligence functionalities.
*   **`service-discovery`**: (Likely Eureka) Server for service registration and discovery.
*   **`commons`**: Shared library containing common DTOs, utilities, and configurations used across services.

## Technologies Used

*   **Java SDK:** 17
*   **Framework:** Spring Boot (v3.x implied)
*   **Database:** JPA / Hibernate with H2 (for dev/test) or other SQL databases.
*   **Security:** Spring Security, JWT (JSON Web Tokens)
*   **Architecture:** Spring Cloud (Gateway, Discovery)
*   **Build Tool:** Maven
*   **Utilities:** Lombok, Jakarta EE

## Getting Started

### Prerequisites

*   Java JDK 17 or higher
*   Maven 3.8+
*   Git

### Installation

1.  **Clone the repository**
```shell script
git clone <repository-url>
    cd finance-platform
```


2. **Build the project**
    You can build the entire project from the root directory:
```shell script
mvn clean install
```


### Running the Services

For a full system startup, you generally need to start the services in the following order:

1.  **Service Discovery** (Start first)
```shell script
cd service-discovery
    mvn spring-boot:run
```

2. **Auth Service**
```shell script
cd auth-service
    mvn spring-boot:run
```

3.  **Finance Service** & **AI Service**
```shell script
cd finance-service
    mvn spring-boot:run
    # in a new terminal
    cd ai-service
    mvn spring-boot:run
```

4.  **API Gateway** (Start last)
```shell script
cd api-gateway
    mvn spring-boot:run
```


## API Endpoints (Auth Service)

The `auth-service` exposes several key endpoints for user management. Below are some examples based on the implementation:

### Authentication & Registration
*   `POST /api/auth/login`: Authenticate user and receive a JWT.
*   `POST /api/auth/register`: Register a new user (auto-generated password sent via email).
*   `POST /api/auth/register-admin`: Register a new admin user.
*   `POST /api/auth/register/invitation`: Register a user using a valid invitation token.

### User Management
*   `GET /api/auth/current-user`: Get details of the currently logged-in user.
*   `GET /api/auth/profile`: Retrieve user profile.
*   `PUT /api/auth/profile`: Update user profile details.
*   `POST /api/auth/change-password`: Change current user's password.

### Invitation System (Admin)
*   `POST /api/auth/invitations`: Create and send a new invitation.
*   `GET /api/auth/invitations`: List all invitations sent by the current user.
*   `POST /api/auth/invitations/resend`: Resend an existing invitation.
*   `DELETE /api/auth/invitations/{token}`: Cancel an invitation.
*   `GET /api/auth/invitations/validate/{token}`: Validate an invitation token.

## Development

*   **Code Style:** The project uses Lombok to reduce boilerplate code. Ensure your IDE supports Lombok annotation processing.
*   **Database:** By default, services may run with an in-memory H2 database for development. Check `application.properties` or `application.yml` in individual services for configuration.

## Contact

Created by Romeo Jerenyama.