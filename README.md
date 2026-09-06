# Employee Salary Management

Java 21 Spring Boot backend. Run with `mvn spring-boot:run`; test with `mvn test`; OpenAPI UI is at `/swagger-ui.html`.

The API supports employee CRUD, effective-dated compensation history, and typed salary analytics. See `docs/API_DESIGN.md` for endpoints and `docs/ASSUMPTIONS.md` for scope decisions.

Development database: PostgreSQL via Docker Compose (`docker compose up -d`).
To seed exactly 10,000 deterministic employees locally, enable `app.seed.enabled=true` before startup.

Example calls:

- List employees: `GET /api/v1/employees?page=0&size=25&department=Engineering&status=ACTIVE`
- Create employee: `POST /api/v1/employees` with body including `employeeNumber`, `email`, `department`, `jobTitle`, `country`, `currency`, `employmentStatus`, and `hireDate`
- Current compensation: `GET /api/v1/employees/1/compensation`
- Salary analytics: `GET /api/v1/analytics/salary/by-department`

Example response shape:

```json
{
  "content": [
    {
      "id": 1,
      "employeeNumber": "EMP-00001",
      "firstName": "Ava",
      "lastName": "Jones",
      "email": "ava.jones@example.com",
      "department": "Engineering",
      "jobTitle": "Senior Developer",
      "country": "India",
      "currency": "INR",
      "employmentStatus": "ACTIVE",
      "hireDate": "2022-01-15"
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 1,
  "totalPages": 1
}
```
