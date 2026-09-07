package com.salarymanagement.dto;

import com.salarymanagement.domain.PayFrequency;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;

public record CompensationRequest(@NotNull @DecimalMin("0.01") BigDecimal baseSalary,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @DecimalMin("0.00") BigDecimal bonus, @DecimalMin("0.00") BigDecimal allowances,
        @NotNull PayFrequency payFrequency, @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo) {
}
