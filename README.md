---

# Clojure JWT Auth Service Template

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Clojure Version](https://img.shields.io/badge/clojure-1.12.2-blue.svg)](https://clojure.org/)

This project is a template for building a JWT (JSON Web Token) authentication microservice in Clojure. It implements a standard access/refresh token flow, designed to serve as a robust starting point for your own `auth-service`.

**PostgreSQL** for persistent user identity and **Redis** to store refresh tokens.


## Diagram

<img width="699" height="1152" alt="image" src="https://github.com/user-attachments/assets/6defe2d0-fab7-4d53-8014-20aba359cd39" />



## Key Features

*   **Docker**: A fully containerized setup using **Docker Compose**. The entire stack (Clojure App, PostgreSQL, Redis) starts with a `docker compose up`.
*   **Persistent User Storage**: User accounts, including usernames and securely hashed passwords (using `bcrypt`), are stored in a **PostgreSQL** database.
*   **Session Management**: Active refresh tokens are managed in a **Redis** set. This is an in-memory solution for validating tokens and enables instant, reliable session revocation upon user logout.
*   **Complete Authentication Flow**: Implements the full authentication lifecycle, including user signup, login, protected route access, token refreshing, and logout.

## Getting Started

This project is designed to run with Docker. All you need are Docker and Docker Compose installed.

### Prerequisites
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/) (or Docker Engine + Docker Compose for Linux)

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/jwt-authentication.git
cd jwt-authentication
```

### 2. Configure Environment Variables

The application uses a `.env` file for secrets and connection strings. A template is provided.

**a. Copy the example file:**
```bash
cp .env.example .env
```

**b. Generate secure JWT secrets:**
You need to generate two long, random, and unique strings. The `openssl` command is a great way to do this. Run it twice:
```bash
# Generate the first secret
openssl rand -base64 32

# Generate the second secret
openssl rand -base64 32
```

**c. Update your `.env` file:**
Open the newly created `.env` file and paste in the secrets you generated. **The `DATABASE_URL` and `REDIS_URL` are already correctly configured for the Docker network and should not be changed for local development.**

```
# .env
ACCESS_TOKEN_SECRET="your-first-generated-secret-here"
REFRESH_TOKEN_SECRET="your-second-generated-secret-here"
DATABASE_URL="jdbc:postgresql://db:5432/auth_db?user=user&password=password"
REDIS_URL="redis://redis:6379"
```

### 3. Build and Run the Application

With Docker Compose, starting the entire stack is a single command:
```bash
docker-compose up --build
```
This command will:
1.  Build the Docker image for your Clojure application.
2.  Download the official PostgreSQL and Redis images.
3.  Start all three containers and connect them in a dedicated network.
4.  Initialize the PostgreSQL database with the `users` table.

To stop all services, press `Ctrl+C` and then run:
```bash
docker-compose down
```

### 4. Test the API

The easiest way to test the endpoints is with the provided `requests.rest` file and the [REST Client extension for VS Code](https://marketplace.visualstudio.com/items?itemName=humao.rest-client).

The test file guides you through the entire flow:
1.  **POST /signup**: Create a new user account.
2.  **POST /login**: Get your initial access and refresh tokens.
3.  **GET /posts**: Access a protected route using the access token.
4.  **POST /token**: Use your refresh token to get a new access token.
5.  **DELETE /logout**: Invalidate your refresh token, effectively logging out.

## API Endpoints

The service runs two separate servers on different ports to simulate a microservice architecture.

#### Authentication Service (`http://localhost:4000`)

| Method   | Path      | Description                                          |
| :------- | :-------- | :--------------------------------------------------- |
| `POST`   | `/signup` | Creates a new user account.                          |
| `POST`   | `/login`  | Authenticates a user and returns tokens.             |
| `POST`   | `/token`  | Issues a new access token using a refresh token.     |
| `DELETE` | `/logout` | Invalidates a refresh token, logging the user out.   |

#### API Service (`http://localhost:3000`)

| Method | Path     | Description                                                                  |
| :----- | :------- | :--------------------------------------------------------------------------- |
| `GET`  | `/posts` | A protected route that returns data for the authenticated user. Requires a valid `Authorization: Bearer <token>` header. |

## License

Copyright © 2024

Distributed under the MIT License.
