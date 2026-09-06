package com.salarymanagement.dto;

import com.salarymanagement.domain.PayFrequency;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

public record CompensationRequest(@NotNull @DecimalMin("0.01") BigDecimal baseSalary, @NotBlank String currency,
        BigDecimal bonus, BigDecimal allowances, @NotNull PayFrequency payFrequency, @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo) {
}
