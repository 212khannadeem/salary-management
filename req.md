# Backend Implementation Task — Employee Salary Management System

## 1. ROLE

You are acting as a senior backend engineer responsible for designing and implementing the backend of an employee salary management application.

The application is being developed as part of a software engineering assessment where engineering judgment, architecture, maintainability, correctness, testing, documentation, and intentional use of AI are important.

Your job is NOT to blindly generate code.

You must:

1. Understand the requirements.
2. Inspect the existing repository before making changes.
3. Identify assumptions and document them.
4. Design a simple, maintainable backend architecture.
5. Implement the backend end-to-end.
6. Add meaningful automated tests.
7. Add deterministic seed data for 10,000 employees.
8. Add API documentation.
9. Add appropriate validation and error handling.
10. Keep the implementation production-quality without unnecessary complexity.
11. Make incremental Git commits that clearly demonstrate development progression.
12. Produce/update engineering artifacts that explain important design decisions.

Do not introduce unnecessary microservices, distributed infrastructure, event buses, Kubernetes, CQRS, or other complexity unless there is a concrete requirement for it.

The goal is good engineering judgment, not maximum architectural complexity.

---

# 2. SOURCE OF TRUTH

The assessment requirements describe:

- Employee salary management software.
- Organization size: approximately 10,000 employees.
- Primary user persona: HR Manager.
- HR needs to manage salary data through a web-based application.
- HR should be able to answer questions about how the organization pays people.
- Backend should use the language/framework preferred by the job description or a suitable alternative.
- A relational database should be used.
- The application should be seeded with 10,000 employees.
- The solution should be fully functional and deployable.
- Meaningful unit tests are required.
- Tests should be fast, deterministic, and easy to understand.
- Code should have good structure, readability, and maintainability.
- Development should happen through incremental commits.
- Engineering artifacts such as requirements, planning/design notes, architecture diagrams, AI prompts/instructions, trade-offs, and performance considerations should be included where useful.

Do not claim that a requirement exists if it is not explicitly defined.

Where the assessment leaves a requirement open, make a reasonable engineering assumption and document it in:

`docs/ASSUMPTIONS.md`

---

# 3. FIRST STEP — INSPECT THE REPOSITORY

Before implementing anything:

1. Inspect the entire repository structure.
2. Determine:
    - Existing backend technology.
    - Java version.
    - Build system.
    - Existing Spring Boot version, if any.
    - Existing database configuration.
    - Existing frontend/application structure.
    - Existing tests.
    - Existing Docker configuration.
    - Existing CI configuration.
    - Existing README.
    - Existing coding conventions.
3. Reuse existing project structure where it is reasonable.
4. Do not overwrite working code unnecessarily.
5. Do not introduce a second framework when an appropriate framework already exists.
6. Do not create duplicate configuration.
7. Do not create duplicate domain models.
8. Before making architectural changes, explain the reason in a design artifact.

If the repository is empty or contains only scaffolding, establish a clean backend project structure.

---

# 4. TECHNOLOGY DIRECTION

Unless the existing repository or job description clearly requires something else, use:

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Hibernate
- Bean Validation
- SQLite for the default local database
- Flyway for database migrations
- JUnit 5
- Mockito where mocking provides real value
- Spring Boot Test
- MockMvc for HTTP/API tests
- Maven or the existing build tool
- OpenAPI/Swagger for API documentation

Prefer SQLite because the assessment explicitly allows a relational database such as SQLite.

However, structure the application so the persistence layer is not tightly coupled to SQLite-specific behavior.

If PostgreSQL is already configured in the repository and is clearly the intended database, retain it rather than switching unnecessarily.

---

# 5. ARCHITECTURAL PRINCIPLES

Use a clean layered architecture.

Preferred structure:

    controller/
    service/
    repository/
    domain/
    dto/
    mapper/
    exception/
    config/
    specification/        (only if needed)
    seed/
    util/

Do not create layers that have no meaningful responsibility.

Recommended dependency direction:

    Controller
        ↓
    Service
        ↓
    Repository
        ↓
    Database

Domain/business logic must not live inside controllers.

Controllers should primarily:

- Validate input.
- Delegate to services.
- Return appropriate HTTP responses.

Services should contain:

- Business rules.
- Salary calculations.
- Filtering/search orchestration.
- Aggregation logic.
- Business validation.

Repositories should contain persistence concerns.

DTOs should be used for API contracts instead of exposing JPA entities directly.

---

# 6. CORE DOMAIN

Design a sensible domain model around employee salary management.

At minimum, support:

## Employee

Suggested fields:

- id
- employeeNumber
- firstName
- lastName
- email
- department
- jobTitle
- country
- currency
- employmentStatus
- hireDate
- manager/reference if appropriate
- createdAt
- updatedAt

## Compensation / Salary

Do not assume that salary is simply a field on Employee if that would make salary history difficult.

Prefer a separate compensation/salary entity.

Suggested fields:

- id
- employeeId
- baseSalary
- currency
- effectiveFrom
- effectiveTo
- payFrequency
- bonus
- allowances
- createdAt
- updatedAt

The model should allow an employee to have salary history.

Do not over-engineer this into a full payroll system.

This is a salary management system, not a payroll processing system.

---

# 7. BUSINESS CONCEPTS

The system should support the following core capabilities.

## 7.1 Employee Management

HR should be able to:

- List employees.
- View an employee.
- Search employees.
- Filter employees.
- View an employee's compensation.
- View salary history.

Supported filtering should include useful dimensions such as:

- Department.
- Country.
- Employment status.
- Job title.
- Salary range.
- Currency.

Use pagination.

Do NOT load all 10,000 employees into memory for normal list requests.

---

# 8. SALARY MANAGEMENT

HR should be able to:

- View current salary.
- Create/update compensation information.
- Add a new salary record.
- View salary history.
- Retrieve current compensation.
- Identify the effective salary for a given date.

Implement salary history correctly.

For example:

An employee may have:

    2024-01-01 → 50000
    2025-01-01 → 55000
    2026-01-01 → 60000

A query for 2025-06-01 should return 55000.

Do not use floating-point types for money.

Use:

    BigDecimal

and an appropriate database representation.

Clearly define:

- Scale.
- Rounding behavior.
- Currency handling.

Do not perform currency conversion unless explicitly required.

If multiple currencies exist, salary analytics must not incorrectly add amounts from different currencies.

---

# 9. SALARY ANALYTICS / BUSINESS QUESTIONS

One of the assessment goals is that the HR Manager should be able to answer questions about how the organization pays people.

Implement a clean backend capability for salary insights.

The first version should focus on deterministic, explainable analytics rather than introducing an external LLM dependency.

Expose APIs that can answer questions such as:

- What is the average salary by department?
- What is the median salary by department if practical?
- What is the salary range for a department?
- How many employees are in each salary band?
- What is the average salary by country?
- What is the average salary by job title?
- How many employees are paid in each currency?
- What percentage of employees fall into each salary band?
- What are the highest/lowest paid roles?
- How many employees have had salary changes during a period?
- What is the total compensation cost by department, where currencies are the same?

Design these as explicit, typed backend capabilities rather than an uncontrolled "execute arbitrary SQL" endpoint.

Do not allow user input to become raw SQL.

---

# 10. ANALYTICS API

Create an appropriate API namespace, for example:

    /api/v1/analytics

Potential endpoints:

    GET /api/v1/analytics/salary/by-department
    GET /api/v1/analytics/salary/by-country
    GET /api/v1/analytics/salary/by-job-title
    GET /api/v1/analytics/salary/bands
    GET /api/v1/analytics/salary/currencies
    GET /api/v1/analytics/salary/range

Exact endpoint names may be adjusted based on the final API design.

The API contract should be:

- Consistent.
- RESTful.
- Versioned.
- Paginated where appropriate.
- Explicit about currency.
- Easy for the frontend to consume.

Document the final API design.

---

# 11. OPTIONAL QUERY/QUESTION ENDPOINT

If useful, introduce a controlled endpoint such as:

    POST /api/v1/analytics/query

Example request:

    {
      "question": "What is the average salary by department?"
    }

However, this endpoint MUST NOT dynamically execute arbitrary SQL.

Use a controlled intent/query mapping.

For example:

    QUESTION
       ↓
    Intent detection
       ↓
    Supported query type
       ↓
    Typed analytics service
       ↓
    Repository query
       ↓
    Structured response

If natural-language parsing is implemented, keep it deterministic and transparent for the assessment.

Do not introduce an external AI API unless the repository/task explicitly requires it.

If the implementation does not need a question endpoint because explicit analytics APIs provide a better engineering solution, document that decision.

---

# 12. REST API DESIGN

Create versioned APIs.

Use:

    /api/v1/...

Suggested APIs:

## Employees

    GET    /api/v1/employees
    GET    /api/v1/employees/{id}
    POST   /api/v1/employees
    PUT    /api/v1/employees/{id}
    DELETE /api/v1/employees/{id}

## Compensation

    GET    /api/v1/employees/{id}/compensation
    GET    /api/v1/employees/{id}/compensation/history
    POST   /api/v1/employees/{id}/compensation
    PUT    /api/v1/employees/{id}/compensation/{compensationId}

## Analytics

    GET /api/v1/analytics/salary/by-department
    GET /api/v1/analytics/salary/by-country
    GET /api/v1/analytics/salary/by-job-title
    GET /api/v1/analytics/salary/bands
    GET /api/v1/analytics/salary/currencies

Adjust these APIs if the final domain design requires it.

Do not implement endpoints merely because they appear in this prompt if they do not make sense after inspecting the repository.

---

# 13. PAGINATION

All employee collection APIs must support pagination.

Use a standard structure such as:

    page
    size
    totalElements
    totalPages
    content

Example:

    GET /api/v1/employees?page=0&size=25

Define reasonable defaults and maximum page sizes.

Do not allow unbounded requests.

---

# 14. SEARCH AND FILTERING

Employee search should support combinations of filters.

Example:

    GET /api/v1/employees
        ?department=Engineering
        &country=India
        &status=ACTIVE
        &page=0
        &size=25

Support sorting where useful.

Example:

    sort=lastName,asc

Do not build a huge custom filtering framework unless it is genuinely necessary.

Use Spring Data Specifications or another clean mechanism if it improves maintainability.

---

# 15. VALIDATION

Use Jakarta Bean Validation.

Validate:

- Required fields.
- Email format.
- Salary > 0 where applicable.
- Currency format.
- Valid dates.
- Effective date rules.
- Pagination values.
- Enum values.
- String lengths.

Business validation belongs in services, not only DTO annotations.

Examples:

- Employee email must be unique.
- Employee number must be unique.
- Salary history must not contain overlapping effective periods.
- Effective dates must be logically valid.
- An employee must exist before compensation can be created.

---

# 16. ERROR HANDLING

Implement centralized exception handling using:

    @RestControllerAdvice

Return consistent error responses.

Recommended structure:

    {
      "timestamp": "...",
      "status": 400,
      "code": "VALIDATION_ERROR",
      "message": "Request validation failed",
      "path": "/api/v1/employees",
      "details": [...]
    }

Handle at minimum:

- Validation errors.
- Resource not found.
- Duplicate employee.
- Duplicate email.
- Invalid salary history.
- Invalid date range.
- Unsupported analytics query.
- Illegal arguments.
- Unexpected server errors.

Do not expose stack traces or internal implementation details to API consumers.

Log unexpected errors appropriately.

---

# 17. HTTP STATUS CODES

Use meaningful HTTP statuses.

Examples:

    GET successful        → 200
    POST successful       → 201
    PUT successful        → 200
    DELETE successful     → 204
    Invalid request       → 400
    Resource missing     → 404
    Duplicate resource   → 409
    Unexpected error     → 500

Do not return 200 for every situation.

---

# 18. DATABASE DESIGN

Use migrations.

Do not rely on Hibernate auto-creating production schema.

Preferred approach:

    Flyway migrations

Example:

    V1__create_employee_tables.sql
    V2__create_compensation_tables.sql
    V3__add_indexes.sql

Use appropriate indexes.

At minimum consider indexes for:

- employeeNumber
- email
- department
- country
- employmentStatus
- compensation.employeeId
- compensation.effectiveFrom

Use unique constraints where appropriate.

Use foreign keys.

Avoid unnecessary indexes.

Document important indexing decisions.

---

# 19. DATABASE PERFORMANCE

The system must support approximately 10,000 employees comfortably.

This is not a massive-scale distributed system.

Do not prematurely optimize.

However:

- Avoid N+1 queries.
- Use pagination.
- Avoid loading the entire employee dataset.
- Use database aggregation for analytics.
- Use indexes for common filters.
- Avoid unnecessary entity relationships that trigger large object graphs.
- Avoid exposing JPA entities directly.
- Use projections/native queries only where they provide a clear benefit.
- Measure before adding complexity.

Document important performance considerations in:

    docs/PERFORMANCE.md

---

# 20. SEED DATA — 10,000 EMPLOYEES

Implement deterministic seed data for exactly 10,000 employees.

The seed data should be realistic enough to demonstrate the application.

Generate combinations of:

- Names.
- Departments.
- Job titles.
- Countries.
- Currencies.
- Employment statuses.
- Hire dates.
- Salary ranges.

Use realistic relationships between:

    department
    job title
    salary

For example, executive/management roles should generally not have the same salary distribution as entry-level roles.

Do not use completely random data that produces unrealistic analytics.

The seed process must be deterministic.

Running it twice should not create duplicate employees.

Preferred options:

- Dedicated seed profile.
- CommandLineRunner/ApplicationRunner guarded by configuration.
- Database seed migration if practical.
- Dedicated development/test seed mechanism.

Separate production startup from development seed behavior.

Example:

    app.seed.enabled=true

Default should be safe.

Do not unexpectedly insert 10,000 records every time the application starts.

---

# 21. SEED PERFORMANCE

The 10,000 employee seed should be reasonably fast.

Do not execute 10,000 individual expensive transactions unnecessarily.

Use batching where appropriate.

Measure seed execution time.

Document:

- How seeding works.
- How to enable it.
- How long it approximately takes.
- How duplicates are prevented.

---

# 22. SECURITY

The assessment describes an HR Manager persona.

Do not build an elaborate authentication system unless required by the existing project/JD.

However, structure the application so authentication/authorization can be introduced cleanly.

If authentication already exists:

- Preserve it.
- Apply authorization correctly.

If authentication does not exist:

- Document that authentication is deliberately out of scope for the current backend iteration.
- Do not fake security with hardcoded credentials.

Never log:

- Passwords.
- Tokens.
- Sensitive authentication data.

---

# 23. AUDITING

Salary changes are business-sensitive.

Where reasonable, maintain audit information such as:

- createdAt
- updatedAt
- effectiveFrom
- effectiveTo

If a full audit trail is implemented, keep it simple and explain why.

Do not introduce a large audit/event architecture without need.

---

# 24. TRANSACTION MANAGEMENT

Use transactions deliberately.

Examples:

- Creating compensation.
- Updating compensation.
- Salary history changes.
- Employee creation where multiple records are affected.

Use:

    @Transactional

at service boundaries where appropriate.

Do not annotate every method with @Transactional blindly.

---

# 25. ENTITY DESIGN

Follow JPA best practices.

Consider:

- Proper ID generation.
- Explicit table/column names where useful.
- Unique constraints.
- Lazy relationships by default where appropriate.
- Avoiding bidirectional relationships unless needed.
- Avoiding Lombok-generated equals/hashCode that can cause JPA problems.
- Avoiding exposing entities from REST controllers.

Do not make entities giant mutable data containers.

---

# 26. DTO DESIGN

Create request/response DTOs.

For example:

    EmployeeCreateRequest
    EmployeeUpdateRequest
    EmployeeResponse

    CompensationCreateRequest
    CompensationUpdateRequest
    CompensationResponse

    SalaryHistoryResponse

    SalaryByDepartmentResponse
    SalaryByCountryResponse

Use DTOs as the public API contract.

Do not return:

    EmployeeEntity

directly from controllers.

---

# 27. MAPPING

Keep entity ↔ DTO conversion clean.

You may use:

- Manual mappers for simple mappings.
- MapStruct if already available or justified.

Do not introduce MapStruct just for a few trivial mappings if manual mapping is clearer.

---

# 28. LOGGING

Use structured, useful application logs.

Log:

- Important startup information.
- Seed start/end and counts.
- Unexpected exceptions.
- Important business failures where useful.

Do not log sensitive employee information unnecessarily.

Do not log entire request bodies by default.

---

# 29. CONFIGURATION

Externalize configuration.

Use:

    application.yml

and profiles where appropriate:

    application.yml
    application-dev.yml
    application-test.yml

Database configuration should be configurable.

Seed configuration should be configurable.

Do not hardcode environment-specific paths.

---

# 30. API DOCUMENTATION

Add OpenAPI/Swagger.

Document:

- Endpoints.
- Request fields.
- Response fields.
- Query parameters.
- Validation constraints.
- Error responses.
- Example requests/responses.

The API should be understandable without reading the implementation.

---

# 31. TESTING STRATEGY

Testing is a major assessment requirement.

Do not create tests merely to increase coverage percentage.

Tests must verify meaningful behavior.

Use a testing pyramid.

## Unit tests

Focus on:

- Salary calculation logic.
- Salary effective-date resolution.
- Salary history validation.
- Employee service behavior.
- Analytics/business rules.
- Question/intent parsing if implemented.

Unit tests must be:

- Fast.
- Deterministic.
- Independent.
- Easy to understand.

## Controller/API tests

Test:

- Successful requests.
- Validation failures.
- Not found.
- Conflict.
- Pagination.
- Filtering.
- Analytics endpoints.
- Error response structure.

## Repository/integration tests

Where valuable, test:

- Database mappings.
- Important queries.
- Salary history queries.
- Aggregation queries.
- Constraints.

Use an isolated test database.

Tests must not depend on an external developer database.

---

# 32. TEST CASES TO INCLUDE

At minimum, implement meaningful tests for:

### Employee

- Create employee successfully.
- Reject invalid employee.
- Reject duplicate employee number.
- Reject duplicate email.
- Retrieve employee.
- Return 404 for missing employee.
- Search employees.
- Filter employees.
- Pagination.

### Compensation

- Add salary.
- Retrieve current salary.
- Retrieve salary history.
- Resolve salary based on effective date.
- Reject overlapping salary periods.
- Reject invalid salary.
- Update salary correctly.

### Analytics

- Average salary by department.
- Salary distribution.
- Country aggregation.
- Job title aggregation.
- Currency grouping.
- Verify that different currencies are not incorrectly summed.

### Seed

- Correct number of employees.
- Deterministic output.
- No duplicates.

### Error handling

- Validation response.
- Not-found response.
- Conflict response.
- Unexpected exception response.

---

# 33. TEST DATA

Do not make tests dependent on the 10,000 production seed dataset.

Tests should use small, focused datasets.

Example:

    3 employees
    2 departments
    2 currencies

This makes tests fast and deterministic.

---

# 34. API CONTRACT EXAMPLES

Add example requests/responses to the README or API documentation.

Example:

    GET /api/v1/employees?page=0&size=20

Example response:

    {
      "content": [...],
      "page": 0,
      "size": 20,
      "totalElements": 10000,
      "totalPages": 500
    }

Do not hardcode these numbers in implementation.

---

# 35. OBSERVABILITY

Keep observability simple.

At minimum:

- Health endpoint.
- Useful startup logs.
- Database connectivity visibility where appropriate.

If Spring Boot Actuator is introduced, expose only appropriate endpoints.

Do not expose sensitive management endpoints publicly.

---

# 36. DEPLOYABILITY

The backend should be easy to run.

Provide:

    README.md

with:

1. Prerequisites.
2. Build instructions.
3. Test instructions.
4. Run instructions.
5. Database configuration.
6. Seed instructions.
7. API documentation location.
8. Example API calls.
9. Architecture overview.

If Docker is appropriate for the existing project, add:

    Dockerfile

and optionally:

    docker-compose.yml

Do not add Docker purely for the sake of adding Docker if it complicates local development.

---

# 37. CODE QUALITY

Follow standard Java/Spring practices.

Requirements:

- Meaningful names.
- Small focused classes.
- Single responsibility.
- Avoid giant services.
- Avoid giant controllers.
- Avoid duplicated logic.
- Avoid magic numbers.
- Avoid unnecessary abstractions.
- Avoid unnecessary interfaces.
- Prefer composition over inheritance.
- Keep public methods focused.
- Keep API contracts stable.
- Keep business rules explicit.

Do not over-engineer.

A simple solution that is easy to understand is preferred over a complicated abstraction that has no real value.

---

# 38. DOCUMENTATION / ASSESSMENT ARTIFACTS

Create/update the following artifacts where applicable:

    docs/
        REQUIREMENTS.md
        ASSUMPTIONS.md
        ARCHITECTURE.md
        API_DESIGN.md
        TRADE_OFFS.md
        PERFORMANCE.md
        AI_USAGE.md

## REQUIREMENTS.md

Summarize:

- Goal.
- User persona.
- Functional scope.
- Non-functional requirements.
- Deliberately excluded features.

## ASSUMPTIONS.md

Record every significant assumption made because the assessment did not specify the behavior.

Examples:

- Authentication is out of scope.
- Currency conversion is out of scope.
- Payroll processing is out of scope.
- Salary analytics operate within a currency unless explicitly grouped.
- SQLite is used for local deployment.

Do not hide assumptions in code.

## ARCHITECTURE.md

Explain:

- Architecture.
- Layers.
- Domain model.
- Database design.
- Request flow.
- Important design decisions.

Include an architecture diagram if practical.

Mermaid is acceptable.

Example:

    ```mermaid
    flowchart LR
        Client --> Controller
        Controller --> Service
        Service --> Repository
        Repository --> Database
    ```

## API_DESIGN.md

Document:

- Endpoints.
- Request/response contracts.
- Pagination.
- Filtering.
- Errors.

## TRADE_OFFS.md

Document important decisions.

Example:

    SQLite vs PostgreSQL
    REST analytics vs generic query endpoint
    Separate compensation entity vs salary field on Employee
    Explicit analytics APIs vs LLM-based querying

For each decision explain:

- Decision.
- Alternatives.
- Why this approach was selected.
- Consequences.

## PERFORMANCE.md

Document:

- Expected scale: 10,000 employees.
- Pagination.
- Indexes.
- Query strategy.
- Seed performance.
- N+1 avoidance.
- Analytics query considerations.

## AI_USAGE.md

Document how AI/Codex was used.

Include:

- High-level prompts/instructions.
- Areas where AI generated code.
- Areas manually reviewed.
- Testing/validation performed.
- Important architectural decisions made by the developer.

The goal is to demonstrate intentional AI usage, not pretend AI was not involved.

---

# 39. GIT COMMIT STRATEGY

Do NOT make one giant commit.

The assessment explicitly values incremental commits that show how the solution evolved.

Use meaningful commits.

Suggested progression:

    1. docs: define backend requirements and assumptions

    2. build: initialize Spring Boot backend

    3. feat: add employee domain and persistence

    4. feat: add employee management APIs

    5. feat: add compensation and salary history

    6. feat: add salary validation and business rules

    7. feat: add salary analytics APIs

    8. feat: add deterministic employee seed data

    9. test: add employee service and API tests

    10. test: add compensation and analytics tests

    11. docs: add architecture and trade-off documentation

    12. chore: improve configuration and deployment

Do not create meaningless commits just to increase commit count.

Each commit should represent a logical unit of work.

Before committing, verify that the project still builds/tests successfully.

---

# 40. DEVELOPMENT PROCESS

Follow this sequence.

## Phase 1 — Discovery

Inspect repository.

Determine existing technology.

Do not modify code yet.

Create/update:

    docs/REQUIREMENTS.md
    docs/ASSUMPTIONS.md

## Phase 2 — Architecture

Design:

- Domain model.
- Database model.
- API structure.
- Service boundaries.
- Analytics approach.

Create:

    docs/ARCHITECTURE.md
    docs/API_DESIGN.md

## Phase 3 — Foundation

Implement:

- Spring Boot setup.
- Configuration.
- Database.
- Flyway.
- Error handling.
- Common API structures.

Run tests/build.

Commit.

## Phase 4 — Employee Management

Implement:

- Employee entity.
- Repository.
- DTOs.
- Mapper.
- Service.
- Controller.
- Validation.
- Search.
- Filtering.
- Pagination.

Write tests.

Run tests.

Commit.

## Phase 5 — Compensation

Implement:

- Compensation entity.
- Salary history.
- Effective-date logic.
- Validation.
- APIs.

Write tests.

Run tests.

Commit.

## Phase 6 — Analytics

Implement:

- Department salary analytics.
- Country salary analytics.
- Job-title analytics.
- Salary bands.
- Currency grouping.
- Other useful business questions.

Write tests.

Run tests.

Commit.

## Phase 7 — Seed Data

Implement deterministic 10,000 employee seed.

Verify:

    exactly 10,000 employees

Verify:

    no duplicate employee numbers
    no duplicate emails
    valid salary relationships

Commit.

## Phase 8 — Hardening

Review:

- Error handling.
- Validation.
- Logging.
- Transactions.
- Performance.
- N+1 queries.
- API consistency.
- Security considerations.
- Code duplication.

Run full test suite.

Commit improvements.

## Phase 9 — Documentation

Complete:

    README.md
    docs/ARCHITECTURE.md
    docs/API_DESIGN.md
    docs/ASSUMPTIONS.md
    docs/TRADE_OFFS.md
    docs/PERFORMANCE.md
    docs/AI_USAGE.md

Commit.

---

# 41. DEFINITION OF DONE

The backend is NOT complete until all of the following are true:

## Build

- [ ] Project builds successfully.
- [ ] No compilation errors.
- [ ] No unnecessary warnings.

## Database

- [ ] Relational database configured.
- [ ] Database migrations work.
- [ ] Constraints are defined.
- [ ] Important indexes are defined.

## Employee management

- [ ] CRUD APIs work.
- [ ] Search works.
- [ ] Filtering works.
- [ ] Pagination works.
- [ ] Validation works.
- [ ] Duplicate handling works.

## Compensation

- [ ] Salary records work.
- [ ] Salary history works.
- [ ] Effective dates work.
- [ ] Overlapping salary periods are rejected.
- [ ] Money uses BigDecimal.
- [ ] Currency handling is explicit.

## Analytics

- [ ] Salary analytics work.
- [ ] Aggregations are correct.
- [ ] Different currencies are not incorrectly combined.
- [ ] Business questions can be answered through APIs.

## Seed

- [ ] 10,000 employees can be seeded.
- [ ] Seed is deterministic.
- [ ] Seed does not create duplicates.
- [ ] Seed is not automatically executed in an unsafe production configuration.

## Testing

- [ ] Unit tests exist.
- [ ] API tests exist.
- [ ] Important repository/integration tests exist.
- [ ] Tests are deterministic.
- [ ] Tests run without external dependencies.
- [ ] All tests pass.

## Quality

- [ ] Controllers are thin.
- [ ] Business logic is in services.
- [ ] DTOs are used.
- [ ] Entities are not directly exposed.
- [ ] Exceptions are handled consistently.
- [ ] No obvious N+1 queries.
- [ ] No unnecessary complexity.

## Documentation

- [ ] README is complete.
- [ ] Architecture documented.
- [ ] Assumptions documented.
- [ ] Trade-offs documented.
- [ ] Performance considerations documented.
- [ ] AI usage documented.
- [ ] API documentation available.

## Git

- [ ] Changes are represented by logical incremental commits.
- [ ] No giant "initial implementation" commit containing everything.
- [ ] Commit messages clearly explain the evolution.

---

# 42. IMPORTANT AI/CODEX BEHAVIOR

While implementing:

1. Do not make assumptions silently.
2. If a requirement is ambiguous, choose the simplest reasonable interpretation and document it.
3. Do not add functionality just because it sounds impressive.
4. Do not introduce an LLM when deterministic backend logic solves the problem.
5. Do not create microservices.
6. Do not create unnecessary design patterns.
7. Do not create generic frameworks inside the project.
8. Do not duplicate existing functionality.
9. Do not bypass tests.
10. Do not disable failing tests.
11. Do not use random non-deterministic test data.
12. Do not ignore compiler warnings if they indicate real problems.
13. Do not swallow exceptions.
14. Do not expose stack traces through REST APIs.
15. Do not return JPA entities directly.
16. Do not use floating point for monetary values.
17. Do not load all 10,000 employees into application memory for normal requests.
18. Do not use raw SQL generated from user input.
19. Do not hardcode secrets.
20. Do not commit generated build artifacts.

---

# 43. BEFORE EACH MAJOR CHANGE

Before implementing a major feature:

1. Inspect relevant existing code.
2. Explain the intended change briefly.
3. Implement the smallest clean solution.
4. Add/update tests.
5. Run relevant tests.
6. Run the full test suite when appropriate.
7. Review the resulting code.
8. Commit the logical change.

Do not continuously rewrite the entire architecture.

---

# 44. FINAL VERIFICATION

When implementation is complete, perform a final engineering review.

Check:

### Architecture

- Is the architecture understandable?
- Are responsibilities clear?
- Is there unnecessary complexity?

### Correctness

- Do salary calculations work?
- Does salary history work?
- Are effective dates correct?
- Are analytics mathematically correct?
- Are currencies handled safely?

### Performance

- Can 10,000 employees be handled comfortably?
- Are list APIs paginated?
- Are important queries indexed?
- Are there N+1 queries?

### Testing

- Do all tests pass?
- Are tests meaningful?
- Are business rules covered?

### API

- Are HTTP status codes correct?
- Are errors consistent?
- Are APIs documented?

### Maintainability

- Can another engineer understand the project quickly?
- Are assumptions documented?
- Are trade-offs documented?

### AI usage

- Is AI usage documented?
- Are architectural decisions explainable by the developer?
- Has generated code been reviewed and tested?

---

# 45. FINAL OUTPUT TO THE DEVELOPER

At the end of the implementation, provide a concise final report containing:

1. What was implemented.
2. Final architecture.
3. Main APIs.
4. Database design.
5. How 10,000 employees are seeded.
6. Test strategy.
7. Test results.
8. Important assumptions.
9. Important trade-offs.
10. Performance considerations.
11. Files/artifacts created.
12. Git commits created.
13. Any remaining limitations or intentionally out-of-scope features.
14. Exact commands to:
    - build
    - test
    - run
    - seed
    - access API documentation

Do not say "implemented successfully" unless the build and tests have actually been executed and passed.

---

# 46. QUALITY BAR

The final backend should look like something a professional engineer would be comfortable submitting for review.

Prioritize:

    Correctness
    Simplicity
    Maintainability
    Testability
    Clear API design
    Good database design
    Explicit assumptions
    Measured performance
    Good documentation
    Meaningful Git history

Do NOT optimize for:

    Number of classes
    Number of dependencies
    Number of design patterns
    Number of APIs
    Number of commits
    Artificially high test coverage

The reviewer should be able to understand WHY the system was designed this way.

The implementation should demonstrate sound engineering judgment.

---

# 47. START NOW

Start with repository discovery.

Do NOT immediately generate the entire application.

First:

1. Inspect the repository.
2. Identify the current technology stack.
3. Identify what already exists.
4. Compare the existing structure with the requirements.
5. Create/update the requirements and assumptions documentation.
6. Propose the backend architecture.
7. Then implement incrementally.

After every major phase, run the relevant tests and verify that the application remains buildable.

Do not skip the discovery and design phases.