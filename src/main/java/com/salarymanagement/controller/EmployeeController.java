package com.salarymanagement.controller;

import com.salarymanagement.domain.EmploymentStatus;
import com.salarymanagement.dto.*;
import com.salarymanagement.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService employees;
    private final CompensationService compensation;

    public EmployeeController(EmployeeService e, CompensationService c) {
        employees = e;
        compensation = c;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> list(@RequestParam(required = false) String department,
            @RequestParam(required = false) String country, @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) String jobTitle, @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minSalary, @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size, @RequestParam(defaultValue = "lastName,asc") String[] sort) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and 100");
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new IllegalArgumentException("minSalary must not be greater than maxSalary");
        }
        return employees.list(department, country, status, jobTitle, currency, minSalary, maxSalary, q,
                PageRequest.of(page, size, Sort.by(parseSort(sort))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest r) {
        return employees.create(r);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return employees.get(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest r) {
        return employees.update(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employees.delete(id);
    }

    @GetMapping("/{id}/compensation")
    public CompensationResponse current(@PathVariable Long id) {
        return compensation.current(id);
    }

    @GetMapping("/{id}/compensation/history")
    public List<CompensationResponse> history(@PathVariable Long id) {
        return compensation.history(id);
    }

    @GetMapping("/{id}/compensation/effective")
    public CompensationResponse effective(@PathVariable Long id, @RequestParam LocalDate date) {
        return compensation.effective(id, date);
    }

    @PostMapping("/{id}/compensation")
    @ResponseStatus(HttpStatus.CREATED)
    public CompensationResponse add(@PathVariable Long id, @Valid @RequestBody CompensationRequest r) {
        return compensation.create(id, r);
    }

    @PutMapping("/{id}/compensation/{compensationId}")
    public CompensationResponse updateCompensation(@PathVariable Long id, @PathVariable Long compensationId,
            @Valid @RequestBody CompensationRequest r) {
        return compensation.update(id, compensationId, r);
    }

    private Sort.Order parseSort(String[] s) {
        if (s == null || s.length == 0 || s[0].isBlank()) {
            throw new IllegalArgumentException("sort must not be blank");
        }
        String[] p = s[0].split(",");
        Set<String> allowed = Set.of("employeeNumber", "firstName", "lastName", "department", "jobTitle",
                "country", "currency", "employmentStatus", "hireDate");
        if (!allowed.contains(p[0])) {
            throw new IllegalArgumentException("Unsupported sort field: " + p[0]);
        }
        if (p.length > 2 || (p.length == 2 && !p[1].equalsIgnoreCase("asc")
                && !p[1].equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("sort direction must be asc or desc");
        }
        return new Sort.Order(p.length > 1 && p[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                p[0]);
    }
}
