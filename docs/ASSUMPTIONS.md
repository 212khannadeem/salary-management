# Assumptions

- Java 21 and Spring Boot are appropriate because the repository had no existing application.
- PostgreSQL is the local and production-oriented relational database. Docker Compose supplies a repeatable local PostgreSQL 16 instance.
- Amounts use `BigDecimal` rounded to two decimal places with `HALF_UP`; an amount is never aggregated across currencies.
- A compensation record is effective from its start date inclusively until its end date inclusively; a null end date means open-ended. Periods for one employee may not overlap.
- Compensation creation automatically closes a prior open-ended record on the day before the new record begins when the dates permit. Explicitly supplied periods are never silently altered.
- Authentication is deliberately out of scope, but controllers are isolated so Spring Security can be added at the HTTP boundary.
- The seed profile is off by default and creates exactly 10,000 employees only when the employee table is empty.
