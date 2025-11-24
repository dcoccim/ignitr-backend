# **Ignitr – Copilot Instructions**

You are assisting with the **Ignitr backend**, a clean, professional Spring Boot 3 application using Java 21 and MongoDB.  
The project must remain **consistent, clean, testable, and predictable**.

Your job is to help implement features by following the project's established patterns **exactly**, without introducing new patterns unless explicitly requested.

---

## **1. General Architecture Rules**

### **Services**
- **Services must return DTOs**, never entities.
- Services must use `SparkMapper` (or future mappers) as the *only* location for entity ↔ DTO conversion.
- Services must contain **no controller-level logic** (e.g., no ResponseEntity).
- Services must log:
  - `info`: before and after major operations
  - `warn`: for expected exceptional states (duplicate titles, missing spark)
  - `error`: only for unexpected exceptions (caught by global handler)

### **Controllers**
- Must return proper `ResponseEntity<>`.
- Must rely exclusively on Service outputs (DTOs).
- Must not access repositories.
- Must follow existing naming conventions and URL patterns:
  - `/api/sparks`
  - `/api/sparks/{id}`
  - `/api/sparks/{parentId}/children`
  - `/api/sparks/{id}/tree`

### **Repositories**
- Standard Spring Data interfaces.
- No business logic allowed.

---

## **2. Data & DTO Rules**

### **DTO Characteristics**
- All request DTOs are **records**:  
  `public record CreateSparkRequestDTO(String title, String description) {}`
- All response DTOs are also records.
- DTOs must be minimal and flat; nested structures only when required (e.g., SparkTreeDTO).

### **Entity Rules**
- Entities represent DB structure only.
- Entities have setters for updates.
- Entities never leave the service layer.

---

## **3. Error Handling Rules**

We use a global handler:

- Every domain error is represented in `ApiErrorCode` enum.
- Controllers must return JSON via `ApiError`.
- Services must throw:
  - `SparkNotFoundException`
  - `SparkAlreadyExistsException`

### **ApiError Must Contain**
- timestamp
- status
- error (HTTP reason phrase)
- code (from enum)
- message
- path

### **No business logic in the exception handler.**

---

## **4. Logging Rules**

Every service method must follow this style:

```
log.debug("Creating Spark with title='{}'", title);
...
log.info("Created Spark with id='{}'", saved.getId());
```

- Never log the DTO directly.
- Never log stack traces manually (exception handler does this).
- `debug` is allowed only for detailed internal state.

---

## **5. Testing Rules**

### **Controller tests**
- Use `@WebMvcTest(Controller.class)`
- Use `@Import(GlobalExceptionHandler.class)`
- Use MockMvc, ObjectMapper
- Mock services with @MockBean
- Assertions must check:
  - HTTP status
  - JSON body
  - correct error code
  - correct content type

### **Service tests**
- Use `@ExtendWith(MockitoExtension.class)`
- Use repository mocks
- Use ArgumentCaptor to verify saved entities
- No Spring context

**Never write integration tests unless explicitly requested.**

---

## **6. Spark Package Rules**

The Spark package must implement:

### **Create Spark**
- Check title uniqueness
- Create top-level spark (no parent)

### **Create Child Spark**
- Validate parent exists
- Check title uniqueness
- Assign parentId

### **Get Spark**
- Fetch by id
- Throw SparkNotFoundException if missing

### **Get Children**
- Validate parent exists
- Return list of SparkDTO

### **Get Tree**
- Recursive tree builder `buildTree(Spark root)`
- Uses SparkTreeDTO
- Must avoid repeated DB queries other than parentId-based scans

---

## **7. Update Spark (rules Copilot must follow)**

- Validate spark exists
- If title changes → check uniqueness
- Only update fields that are present in `UpdateSparkRequestDTO`
- Update timestamps
- Return updated SparkDTO

---

## **8. Delete Spark (rules Copilot must follow)**

The delete functionality has **three modes**:

### **A. Delete leaf**  
- If spark has no children → simple delete

### **B. Delete cascade**  
- Delete spark and **all descendants**  
- Implement a recursive delete method

### **C. Delete and promote children**  
- If spark has parent:
  - Reassign children to the parent
- If spark is root:
  - Promote children → they become new roots (parentId = null)

### Additional rules:
- All deletes return no body response
- All operations logged

---

## **9. Documentation Rules (YAML)**

All API documentation is defined in `openapi.yaml`.

- Tags group operations (Health, Sparks).
- Schemas describe DTOs and error structure.
- Recursive trees use `$ref` to SparkTree for children.

No annotations in controllers should define documentation unless absolutely necessary.

---

## **10. How You Should Behave**

When generating code:
- Follow existing patterns exactly.
- Use the filename and folder structure already in place.
- Do not introduce new abstractions unless asked.
- Maintain formatting and naming conventions.
- Ask for clarification if anything is ambiguous.
- Never guess; follow the established architecture.

When generating tests:
- Follow the current test style (MockMvc + Mockito).
- Use descriptive method names:  
  `getSpark_returns200AndBody_onSuccess()`
- Mimic existing success/error patterns.

---
