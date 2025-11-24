# Spark Package Overview

This document describes everything inside the `spark` feature module in your backend project. It acts as a complete reference for Copilot or future contributors.

---

# 🌟 Spark Feature Overview

A **Spark** is a unit of thought/idea in Ignitr.  
A Spark can be:

- A **top-level spark** (root)
- A **child spark** (nested)
- A node in a recursive **tree of sparks**

Each spark contains:

- id  
- title  
- description  
- parentId (nullable)
- createdAt  
- updatedAt  

Mongo persists all sparks in a single collection.

---

# 📦 Package Structure

```
spark/
├── controller/
│   └── SparkController.java
├── service/
│   ├── SparkService.java
│   └── SparkServiceImpl.java
├── dto/
│   ├── CreateSparkRequestDTO.java
│   ├── UpdateSparkRequestDTO.java
│   ├── SparkDTO.java
│   └── SparkTreeDTO.java
├── model/
│   └── Spark.java
│   └── SparkDeleteMode.java
├── repository/
│   └── SparkRepository.java
├── mapper/
│   └── SparkMapper.java
└── exception/
    ├── SparkNotFoundException.java
    └── SparkAlreadyExistsException.java
```

---

# 🧩 DTO Layer

### 1️⃣ CreateSparkRequestDTO  
- Used for creating both top-level and child sparks.

### 2️⃣ UpdateSparkRequestDTO  
- Used for updating spark title and description.

### 3️⃣ SparkDTO  
- Returned by all non-tree endpoints.

### 4️⃣ SparkTreeDTO  
- Returned only by `/api/sparks/{id}/tree`.
- Contains recursive children list.

---

# 🧬 Entity Layer

### Spark.java
Mongo document stored in the database.

Contains:
- id
- title
- description
- parentId
- createdAt
- updatedAt

No controller-specific info.

---

# 🔁 Mapper Layer

### SparkMapper  
Contains all mapping logic:

- `toEntity(CreateSparkRequestDTO)`
- `toChildEntity(CreateSparkRequestDTO, parentId)`
- `toSparkDto(Spark)`
- `updateEntity(Spark, UpdateDTO)`
- `toTreeDto(Spark, List<SparkTreeDTO>)`

This ensures:
- consistency
- testability
- maintainability

No logic inside services or controllers.

---

# ⚙ Service Layer

### SparkService
Defines operations:
- createSpark(dto)
- createChildSpark(parentId, dto)
- getSparkById(id)
- getChildren(id)
- getSparkTree(id)
- updateSpark(id, dto)
- deleteSparkCascade(id)
- deleteSparkReparent(id)
- deleteSparkLeaf(id)

### SparkServiceImpl

Implements all spark logic:

- Enforces title uniqueness
- Validates parent existence
- Retrieves children
- Builds recursive tree using repository
- Updates sparks
- Deletes with different strategies
- Maps to/from entities/DTOs
- Logs all important events

---

# 🗄 Repository Layer

### SparkRepository
Extends MongoRepository and provides:

- `existsByTitle(String title)`
- `findByParentId(String parentId)`
- Default CRUD methods

---

# 💥 Exception Layer

### SparkNotFoundException  
Thrown when spark id is missing.

### SparkAlreadyExistsException  
Thrown when title already exists.

### Handled globally  
GlobalExceptionHandler converts these into standardized ApiError objects.

---

# 🧪 Testing

You have both:

## Controller Tests (WebMvcTest)
- test HTTP layer only
- mock service
- verify JSON responses

## Service Tests (Mockito)
- mock repository
- verify business logic
- verify mapping correctness
- verify repository interactions

---

# 📘 OpenAPI Documentation

All spark endpoints defined in:

```
openapi.yaml
```

Contains schemas for:
- Spark
- SparkTree
- ApiError
- Create + Update request DTOs

Tags group everything under **Sparks**.

---

# 🎯 Completed Spark Features

✔ Create spark  
✔ Create child spark  
✔ Get spark by id  
✔ Get children  
✔ Get recursive spark tree  
✔ Update spark  
✔ Full DTO architecture  
✔ Clean mapper usage  
✔ Custom exceptions  
✔ Logging  
✔ Controller tests  
✔ Service tests  
✔ OpenAPI documentation

---

# 🚧 To Be Completed

❗ Delete spark (3 modes):
- delete leaf
- delete cascade
- delete reparent
- full test coverage
- OpenAPI documentation

---

This document is enough for Copilot or any agent to fully understand the Spark feature module.
