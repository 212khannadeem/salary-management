package com.salarymanagement.dto;

import java.math.BigDecimal;

public record SalaryBandSummary(String currency, String band, long employeeCount, BigDecimal percentage) {
}
