package com.salarymanagement.dto;

import java.math.BigDecimal;

public record CurrencySummary(String currency, long employeeCount, BigDecimal totalCompensation) {
}
