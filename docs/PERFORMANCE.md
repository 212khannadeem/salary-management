# Performance

List APIs are paginated (maximum 100 records per request). For a 10,000-person organization, database-side paging and unique employee identifiers are sufficient initially. Production migration should use SQLite/PostgreSQL with filter and compensation-date indexes.
