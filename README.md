# Employee Salary Management

Java 21 Spring Boot backend. Run with `mvn spring-boot:run`; test with `mvn test`; OpenAPI UI is at `/swagger-ui.html`.

The API supports employee CRUD, effective-dated compensation history, and typed salary analytics. See `docs/API_DESIGN.md` for endpoints and `docs/ASSUMPTIONS.md` for scope decisions.

Prerequisites:
- Java 21
- Maven
- Docker + Docker Compose for the local PostgreSQL dependency

Build and run:
- `mvn clean package`
- `docker compose up -d`
- `mvn spring-boot:run`

Optional deployment container:
- `docker build -t salary-management .`
- `docker run --rm -p 8080:8080 -e DB_URL=jdbc:postgresql://host.docker.internal:5432/salary_management -e DB_USERNAME=salary_app -e DB_PASSWORD=salary_app salary-management`

Seed data:
- Default: `app.seed.enabled=false`
- Local seed run: `APP_SEED_ENABLED=true mvn spring-boot:run`
- Or use the dedicated profile: `mvn spring-boot:run -Dspring.profiles.active=seed`

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
