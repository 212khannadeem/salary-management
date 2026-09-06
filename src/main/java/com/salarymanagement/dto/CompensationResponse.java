package com.salarymanagement.dto;

import com.salarymanagement.domain.PayFrequency;
import java.math.*;
import java.time.*;

public record CompensationResponse(Long id, BigDecimal baseSalary, String currency, BigDecimal bonus,
        BigDecimal allowances, PayFrequency payFrequency, LocalDate effectiveFrom, LocalDate effectiveTo) {
}
