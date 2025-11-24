# Project Architecture Overview

This document summarizes the architecture of the Ignitr backend and how components align with your design principles.

---

## 🌋 High-Level Structure

The backend is a clean, layered Spring Boot application with a strict separation of concerns:

### **1. Controller Layer**
- Receives HTTP requests.
- Validates input using `@Valid`.
- Calls service layer.
- Returns DTOs only — *never* entities.
- Does **no** business logic.
- No direct repository access.
- Uses ResponseEntity where useful, otherwise returns DTO directly.

### **2. Service Layer**
- Contains all business logic.
- Converts DTO → Entity and Entity → DTO (using mapper).
- Handles domain-specific validation.
- Interacts with repositories.
- Throws custom exceptions for error conditions.
- Writes domain logs (“Creating spark…”, “Spark updated…”, etc.)

### **3. Repository Layer**
- Pure persistence layer.
- Only extends Spring Data MongoDB interfaces.
- No business logic.
- No DTOs.

### **4. Mapper Layer**
- Maps:
  - Entities → DTOs
  - DTOs → Entities
  - Updates onto existing entities
- Centralized, consistent mapping logic.
- Contains:
  - `toSparkDto`
  - `toEntity`
  - `updateEntity`
  - `toTreeDto` (optional but recommended for all tree-building)

### **5. Exception Layer**
- Custom domain exceptions:
  - SparkNotFoundException
  - SparkAlreadyExistsException
- Enum of error codes for consistency.
- GlobalExceptionHandler converts exceptions → clean ApiError DTO.

### **6. DTO Layer**
- Pure immutable data:
  - Creation DTOs
  - Update DTOs
  - SparkDTO
  - SparkTreeDTO
- We use **records** for immutability and clarity.

### **7. Domain Model (Entities)**
- Plain Java classes stored in Mongo.
- Only business state — no API concerns.
- Fields:
  - id, title, description, parentId, timestamps.

---

## 🌲 Spark Tree Building

The tree is built recursively inside the service:

```
SparkTreeDTO buildTree(Spark spark)
```

Uses repository:
```
findByParentId(parentId)
```

Recursively maps children → SparkTreeDTO objects.

---

## 🧪 Testing Philosophy

You are writing **full test coverage**:

### Controller tests (WebMvcTest)
- Mock service responses.
- Validate:
  - status codes
  - JSON payload structure
  - error codes
  - validation behavior

### Service tests
- Mock repository.
- Validate:
  - branching logic
  - validation logic
  - correct exceptions
  - correct object mapping
  - correct repository interactions
- Use `ArgumentCaptor` only when necessary.

---

## 🧭 Design Principles You Follow

- No entities leak outside services.
- Clear layering.
- Code uniformity.
- Explicit logging.
- Every endpoint documented via OpenAPI YAML.
- Clean and reusable DTOs and mappers.
- Full CRUD + tree + child creation pattern.

---

## 🎯 Feature Summary Completed So Far

### ✔ Spark CRUD  
- Create top-level spark  
- Create child spark  
- Get spark by id  
- Get children  
- Spark update  
- Spark tree  
- Custom error handling  
- DTO mapping  
- Unit tests (controller + service)

### ❗ Still in progress  
- Delete spark:
  - leaf deletion
  - cascade deletion
  - reparent deletion
  - tests + docs

---

You can now easily onboard Copilot or any agent to understand your architecture using this file.
