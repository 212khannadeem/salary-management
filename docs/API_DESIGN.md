# API design

All endpoints are under `/api/v1`. Employee lists accept `page` (default 0), `size` (default 25, maximum 100), `sort`, `department`, `country`, `status`, `jobTitle`, `currency`, `minSalary`, `maxSalary`, and `q`.

| Endpoint | Purpose |
| --- | --- |
| `GET/POST /employees` | Search/list or create employees |
| `GET/PUT/DELETE /employees/{id}` | Read, update, delete employee |
| `GET/POST /employees/{id}/compensation` | Current compensation / add a record |
| `GET /employees/{id}/compensation/history` | Salary history |
| `PUT /employees/{id}/compensation/{compensationId}` | Update a salary record |
| `GET /analytics/salary/by-department` | Average/min/max/count grouped by department and currency |
| `GET /analytics/salary/by-country` | Same, grouped by country and currency |
| `GET /analytics/salary/by-job-title` | Same, grouped by role and currency |
| `GET /analytics/salary/bands` | Counts/percentages per configured annual salary band and currency |
| `GET /analytics/salary/currencies` | Current compensation counts by currency |

Errors contain `timestamp`, `status`, `code`, `message`, `path`, and optional field `details`.

Example request:

```http
GET /api/v1/employees?page=0&size=10&department=Engineering&status=ACTIVE
```

Example response:

```json
{
  "content": [
    {
      "id": 17,
      "employeeNumber": "EMP-00017",
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
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

Example analytics response:

```json
[
  {
    "group": "Engineering",
    "currency": "USD",
    "employeeCount": 2,
    "averageSalary": 105000.00,
    "minSalary": 90000.00,
    "maxSalary": 120000.00
  }
]
```
