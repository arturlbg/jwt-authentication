---

# Clojure JWT Auth Service Template

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Clojure Version](https://img.shields.io/badge/clojure-1.12.2-blue.svg)](https://clojure.org/)

This project is a template for building a JWT (JSON Web Token) authentication microservice in Clojure. It implements a standard access/refresh token flow, designed to serve as a robust starting point for your own `auth-service`.

**PostgreSQL** for persistent user identity and **Redis** to store refresh tokens.


## Diagram

<img width="699" height="1152" alt="image" src="https://github.com/user-attachments/assets/6defe2d0-fab7-4d53-8014-20aba359cd39" />



## Key Features

*   **Clojure Stack**: Built with `http-kit`, `reitit` for routing, and `buddy` for security.
*   **In-Memory (for now)**: Uses a simple in-memory Clojure `atom` to store active refresh tokens. This makes the template easy to run and test out-of-the-box without any external database setup.
*   **Database Ready**: The in-memory store in `db.clj` is intentionally simple and can be trivially swapped for a persistent database like Redis or PostgreSQL by implementing the same simple functions (`add-token!`, `remove-token!`, `token-exists?`).

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

**a. Generate secure secrets:**
Open the newly created `.env` file. You need to generate two long, random, and **unique** strings for the secrets. You can use `openssl` for this:
```bash
# Generate the first secret
openssl rand -base64 32

# Generate the second secret
openssl rand -base64 32
```

**b. Update your `.env` file** with the generated values:
```
# .env
ACCESS_TOKEN_SECRET="your-first-generated-secret-here"
REFRESH_TOKEN_SECRET="your-second-generated-secret-here"
```

### 4. Run the Application

Start the two web servers:
```bash
lein run
```
You should see a message confirming the system has started:
`System started. API on port 3000, Auth on port 4000.`

### 5. Test the API

1.  **POST /login**: Get your initial tokens.
2.  **GET /posts**: Access a protected route using the access token.
3.  **POST /token**: Use your refresh token to get a new access token.
4.  **DELETE /logout**: Invalidate your refresh token.

## API Endpoints

The service runs two separate servers on different ports to simulate a microservice architecture.

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
