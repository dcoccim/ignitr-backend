# Ignitr Backend

Ignitr is a personal idea tool built to help you capture, structure, and evolve “Sparks”, small ideas that can grow into bigger projects.

Right now the backend focuses on the **Spark** domain only, implemented end-to-end and thoroughly tested.

---

## ✨ Features

### Spark domain

A **Spark** is a basic idea unit. The current feature set includes:

- Create **top-level** Sparks  
- Create **child** Sparks (Sparks can form trees)  
- Get a single Spark by id  
- Get direct children of a Spark  
- Get the **full Spark tree** (root + all descendants)  
- Update Spark title/description  
- Delete Spark:
  - **CASCADE** (delete whole subtree)
  - **REPARENT** (delete this node, promote children to parent)
- Search & filter Sparks:
  - by title (case-insensitive)
  - by parentId (including `"ROOT"` for top-level)
  - with pagination and sorting (newest first)

### Health check

- Simple `/api/health` endpoint that returns `"OK"`.

### Error handling

Consistent, centralized error handling via `ApiError`, `ApiErrorCode`, and `GlobalExceptionHandler`.

### API documentation

OpenAPI 3.0 spec maintained by hand in `src/main/resources/static/openapi/ignitr.yaml`.

---

## 🧱 Tech Stack

- Java 21  
- Spring Boot 3  
- MongoDB  
- Maven  
- JUnit 5 + Mockito + Spring Boot Test  
- OpenAPI (YAML)

---

## ▶️ Running the Application

```bash
./mvnw spring-boot:run
```

Health check:  
`GET http://localhost:8080/api/health`

---

## 🧪 Running Tests

```bash
./mvnw test
```

---

## 🔭 Next Steps / Roadmap

- Auth & users  
- Idea grouping  
- Reason/vote system  
- AI suggestions  
- Frontend workspace (React or Angular)

