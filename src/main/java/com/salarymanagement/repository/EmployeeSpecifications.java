package com.salarymanagement.repository;

import com.salarymanagement.domain.Compensation;
import com.salarymanagement.domain.Employee;
import com.salarymanagement.domain.EmploymentStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {
    private EmployeeSpecifications() {
    }

    public static Specification<Employee> filters(String department, String country, EmploymentStatus status,
            String jobTitle, String currency, BigDecimal minSalary, BigDecimal maxSalary, String q) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(department)) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase()));
            }
            if (hasText(country)) {
                predicates.add(cb.equal(cb.lower(root.get("country")), country.trim().toLowerCase()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("employmentStatus"), status));
            }
            if (hasText(jobTitle)) {
                predicates.add(cb.equal(cb.lower(root.get("jobTitle")), jobTitle.trim().toLowerCase()));
            }
            if (hasText(currency)) {
                predicates.add(cb.equal(cb.lower(root.get("currency")), currency.trim().toLowerCase()));
            }
            if (minSalary != null || maxSalary != null) {
                Join<Employee, Compensation> compensationJoin = root.join("compensations", JoinType.INNER);
                predicates.add(cb.lessThanOrEqualTo(compensationJoin.get("effectiveFrom"), LocalDate.now()));
                predicates.add(cb.or(cb.isNull(compensationJoin.get("effectiveTo")),
                        cb.greaterThanOrEqualTo(compensationJoin.get("effectiveTo"), LocalDate.now())));
                if (minSalary != null) {
                    predicates.add(cb.greaterThanOrEqualTo(compensationJoin.get("baseSalary"), minSalary));
                }
                if (maxSalary != null) {
                    predicates.add(cb.lessThanOrEqualTo(compensationJoin.get("baseSalary"), maxSalary));
                }
            }
            if (hasText(q)) {
                String normalized = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("employeeNumber")), normalized),
                        cb.like(cb.lower(root.get("firstName")), normalized),
                        cb.like(cb.lower(root.get("lastName")), normalized),
                        cb.like(cb.lower(root.get("email")), normalized),
                        cb.like(cb.lower(root.get("department")), normalized),
                        cb.like(cb.lower(root.get("jobTitle")), normalized),
                        cb.like(cb.lower(root.get("country")), normalized)));
            }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
