package com.salarymanagement.dto;

import java.math.BigDecimal;

public record SalaryRangeSummary(String currency, BigDecimal minimumSalary, BigDecimal maximumSalary,
        BigDecimal averageSalary, long employeeCount) {
}
