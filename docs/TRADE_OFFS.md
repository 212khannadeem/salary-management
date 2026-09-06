# Trade-offs

Compensation is a separate record to preserve salary history. PostgreSQL was selected over SQLite for concurrent application use and production-like constraints; Docker Compose keeps local setup reproducible. Currency conversion, payroll and authentication are out of scope. Explicit APIs are used instead of arbitrary natural-language/SQL queries.
