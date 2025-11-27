# Todo API - Spring Boot REST API

A RESTful API for managing todos, built with Spring Boot as part of the Web Service course at Jeonbuk National University (JBNU).

## Author

- **Name:** Christian Rakotoarison
- **Email:** chriostianmickaler@gmail.com
- **Course:** Web Service
- **University:** Jeonbuk National University (JBNU)

## Tech Stack

- Java 21
- Spring Boot 3.2.0
- Spring Security (JWT Authentication)
- Jakarta Validation
- Lombok
- Springdoc OpenAPI 2.6.0 (Swagger UI)
- JJWT 0.12.3 (JSON Web Token)
- In-memory storage (HashMap)

## Features

- User registration and authentication with JWT
- CRUD operations for todos
- Batch creation of todos
- Mark todos as completed
- Delete all completed todos
- JWT Authentication middleware (Filter)
- Global exception handling
- RFC 9457 compliant error responses
- Swagger UI documentation

## Important Notes

- **In-memory storage:** This application uses HashMap for data storage. All data (users and todos) will be lost when the server is restarted. This is intentional for educational purposes.

- **Token expiration:** JWT tokens expire after 1 hour (3600000 ms). After expiration, you will receive a 401 Unauthorized response and must login again to obtain a new token.

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.x or higher

### Installation

1. Clone the repository

```bash
git clone https://github.com/MickaelCR/todo-api.git
cd todo-api
```

2. Build the project

```bash
./gradlew clean build
```

On Windows:

```bash
gradlew.bat clean build
```

3. Run the application

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat bootRun
```

The API will be available at `http://localhost:8080`

## Running Tests

To execute the unit tests:

```bash
./gradlew test
```

On Windows:

```bash
gradlew.bat test
```

## Testing the API with Swagger UI

Swagger UI provides an interactive interface to test all API endpoints directly from your browser.

### Step 1: Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui.html
```

You will see the API documentation with all available endpoints grouped by category: Authentication and Todo.

### Step 2: Register a New User

Since the Todo endpoints are protected by JWT authentication, you must first create a user account.

1. In Swagger UI, expand the **Authentication** section
2. Click on `POST /auth/register`
3. Click the **Try it out** button
4. In the request body, enter your user information:

```json
{
  "username": "testuser",
  "password": "password123",
  "email": "testuser@example.com"
}
```

5. Click **Execute**
6. You should receive a `201 Created` response confirming the user was created

### Step 3: Login to Get JWT Token

1. Click on `POST /auth/login`
2. Click **Try it out**
3. Enter your credentials:

```json
{
  "username": "testuser",
  "password": "password123"
}
```

4. Click **Execute**
5. In the response, you will see a JSON object containing your JWT token
6. **Copy the token value** (the long string starting with "eyJ...")

### Step 4: Authorize Swagger UI with Your Token

1. At the top right of the Swagger UI page, click the **Authorize** button (lock icon)
2. In the popup window, paste your token into the **Value** field
3. Click **Authorize**
4. Click **Close**

You are now authenticated. All subsequent requests to protected endpoints will automatically include your JWT token.

### Step 5: Test Todo Endpoints

Now you can test all Todo endpoints:

1. **GET /todos** - Retrieve all your todos (initially empty)
2. **POST /todos** - Create a new todo by providing title, description, and dueDate
3. **GET /todos/{id}** - Retrieve a specific todo by its ID
4. **PUT /todos/{id}** - Update an existing todo
5. **PUT /todos/{id}/complete** - Mark a todo as completed
6. **DELETE /todos/{id}** - Delete a specific todo
7. **DELETE /todos/completed** - Delete all completed todos

### Step 6: Test Without Token (Optional)

To verify that authentication is working correctly:

1. Click the **Authorize** button again
2. Click **Logout**
3. Try to access `GET /todos`
4. You should receive a `401 Unauthorized` response

This confirms that the JWT middleware is properly protecting the endpoints.

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register a new user | No |
| POST | `/auth/login` | Login and get JWT token | No |
| GET | `/auth/me` | Get current user info | Yes |

### Todo Endpoints

All todo endpoints require authentication.

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/todos` | Retrieve all todos | 200 |
| GET | `/todos/{id}` | Retrieve a todo by ID | 200, 404 |
| POST | `/todos` | Create a new todo | 201, 400 |
| POST | `/todos/batch` | Create multiple todos | 201, 400 |
| PUT | `/todos/{id}` | Update a todo | 200, 400, 404 |
| PUT | `/todos/{id}/complete` | Mark as completed | 200, 404, 409 |
| DELETE | `/todos/{id}` | Delete a todo | 204, 404 |
| DELETE | `/todos/completed` | Delete all completed | 200 |

## Validation Rules

### User Registration

| Field | Constraints |
|-------|-------------|
| username | Required, 3-20 characters |
| password | Required, minimum 6 characters |
| email | Required, must be valid email format |

### Todo Creation

| Field | Constraints |
|-------|-------------|
| title | Required |
| description | Optional |
| dueDate | Optional, format: YYYY-MM-DD |

### Todo Update

| Field | Constraints |
|-------|-------------|
| title | Required |
| description | Optional |
| dueDate | Optional, format: YYYY-MM-DD |
| done | Required, boolean |

## Response Format

### Success Response

All successful responses follow a standardized format:

```json
{
  "data": { },
  "meta": {
    "requestId": "uuid",
    "servedAt": "ISO-8601 timestamp"
  },
  "links": {
    "self": "/resource/path"
  }
}
```

### Error Response (RFC 9457 Problem Details)

All error responses follow the RFC 9457 standard:

```json
{
  "type": "about:blank",
  "title": "Error Title",
  "status": 400,
  "detail": "Detailed error message",
  "instance": "/request/path",
  "requestId": "uuid"
}
```

## HTTP Status Codes

### Success Codes (2xx)

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST requests |
| 204 | No Content | Successful DELETE requests |

### Client Error Codes (4xx)

| Code | Description | Usage |
|------|-------------|-------|
| 400 | Bad Request | Validation errors, malformed JSON |
| 401 | Unauthorized | Missing or invalid JWT token |
| 404 | Not Found | Resource not found |
| 405 | Method Not Allowed | HTTP method not supported |
| 409 | Conflict | Resource conflict (e.g., todo already completed, username taken) |

### Server Error Codes (5xx)

| Code | Description | Usage |
|------|-------------|-------|
| 500 | Internal Server Error | Unexpected server errors |

## Middleware

This application implements a JWT Authentication Filter as middleware, following the pattern taught in the course. The filter:

1. Intercepts all incoming requests
2. Extracts the JWT token from the Authorization header
3. Validates the token using the secret key
4. Sets the authentication context if valid
5. Rejects unauthorized requests with 401 status

Implementation based on `OncePerRequestFilter` from Spring Security.

## Project Structure

```
todoapi/
├── src/main/java/kr/ac/jbnu/cr/todoapi/
│   ├── config/
│   │   ├── OpenApiConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── TodoController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateTodoRequest.java
│   │   │   ├── UpdateTodoRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── RegisterRequest.java
│   │   └── response/
│   │       ├── ApiResponse.java
│   │       └── ErrorResponse.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── TodoNotFoundException.java
│   │   └── TodoAlreadyCompletedException.java
│   ├── model/
│   │   ├── Todo.java
│   │   └── User.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtAuthentication.java
│   │   └── JwtService.java
│   ├── service/
│   │   ├── TodoService.java
│   │   └── UserService.java
│   └── TodoapiApplication.java
├── src/main/resources/
│   └── application.properties
├── build.gradle
└── README.md
```

## Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## Configuration

Application configuration is defined in `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# JWT
jwt.secret=YourSecretKeyHere
jwt.expiration=3600000
jwt.issuer=todoapi

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```