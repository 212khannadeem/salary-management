# Performance

List APIs are paginated (maximum 100 records per request). For a 10,000-person organization, database-side paging and unique employee identifiers are sufficient initially. PostgreSQL indexes cover the main employee dimensions and compensation effective dates. Analytics currently fetch current compensation rows with employees for deterministic in-process grouping; if the dataset grows materially, move these aggregations to database projections.
