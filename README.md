---

# Clojure JWT Auth Service Template

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Clojure Version](https://img.shields.io/badge/clojure-1.12.2-blue.svg)](https://clojure.org/)

This project is a clean, modern template for building a JWT (JSON Web Token) authentication microservice in Clojure. It implements a standard and secure access/refresh token flow, designed to serve as a robust starting point for your own `auth-service`.

The code is organized with a clear separation of concerns, is REPL-friendly, and uses a minimal, best-in-class set of libraries to get the job done without unnecessary complexity.

## Diagram

<img width="1853" height="1242" alt="image" src="https://github.com/user-attachments/assets/2dfee85e-58f0-4f64-be63-0b036c7067f5" />



## Key Features

*   **Modern Clojure Stack**: Built with `http-kit` for performance, `reitit` for data-driven routing, and `buddy` for rock-solid security.
*   **Standard JWT Flow**: Implements a complete authentication lifecycle with short-lived access tokens and long-lived refresh tokens.
*   **Separation of Concerns**: The codebase is logically divided into namespaces for configuration (`config`), state management (`db`), JWT logic (`auth`), and request handling (`handler`).
*   **In-Memory (for now)**: Uses a simple in-memory Clojure `atom` to store active refresh tokens. This makes the template easy to run and test out-of-the-box without any external database setup.
*   **Database Ready**: The in-memory store in `db.clj` is intentionally simple and can be trivially swapped for a persistent database like Redis (for speed) or PostgreSQL (for durability) by implementing the same simple functions (`add-token!`, `remove-token!`, `token-exists?`).

## Installation

This project is built with [Leiningen](https://leiningen.org/).

Add the following dependency to your `project.clj` file if you were to use it as a library (though it's intended as a template):

```clojure
[jwt-auth-clj "1.0.0"]
```

## Getting Started

Follow these steps to get the authentication service running locally.

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/jwt-auth-clj.git
cd jwt-auth-clj
```

### 2. Install Dependencies

Leiningen will handle this automatically on the first run, but you can fetch them manually:
```bash
lein deps
```

### 3. Configure Environment Variables

This project requires secret keys for signing tokens. A template file is provided.

**a. Copy the example file:**
```bash
cp .env.example .env
```

**b. Generate secure secrets:**
Open the newly created `.env` file. You need to generate two long, random, and **unique** strings for the secrets. You can use `openssl` for this:
```bash
# Generate the first secret
openssl rand -base64 32

# Generate the second secret
openssl rand -base64 32
```

**c. Update your `.env` file** with the generated values:
```
# .env
ACCESS_TOKEN_SECRET="your-first-generated-secret-here"
REFRESH_TOKEN_SECRET="your-second-generated-secret-here"
```

### 4. Run the Application

Start the two web servers (the API service and the Auth service):
```bash
lein run
```
You should see a message confirming the system has started:
`System started. API on port 3000, Auth on port 4000.`

### 5. Test the API

The easiest way to test the endpoints is with the provided `requests.rest` file and the [REST Client extension for VS Code](https://marketplace.visualstudio.com/items?itemName=humao.rest-client).

The test file guides you through the entire flow:
1.  **POST /login**: Get your initial tokens.
2.  **GET /posts**: Access a protected route using the access token.
3.  **POST /token**: Use your refresh token to get a new access token.
4.  **DELETE /logout**: Invalidate your refresh token.

## API Endpoints

The service runs two separate servers on different ports to simulate a real-world microservice architecture.

#### Authentication Service (`http://localhost:4000`)

| Method | Path        | Description                                       |
| :----- | :---------- | :------------------------------------------------ |
| `POST` | `/login`    | Authenticates a user and returns tokens.          |
| `POST` | `/token`    | Issues a new access token using a refresh token.  |
| `DELETE`| `/logout`   | Invalidates a refresh token, logging the user out.|

#### API Service (`http://localhost:3000`)

| Method | Path        | Description                                       |
| :----- | :---------- | :------------------------------------------------ |
| `GET`  | `/posts`    | A protected route that returns data for the authenticated user. Requires a valid `Authorization: Bearer <token>` header. |


## License

Copyright © 2024

Distributed under the MIT License. See `LICENSE` for more information.
