package com.salarymanagement.dto;

import java.math.BigDecimal;

public record SalaryGroupSummary(String group, String currency, long employeeCount, BigDecimal averageSalary,
        BigDecimal minSalary, BigDecimal maxSalary) {
}
