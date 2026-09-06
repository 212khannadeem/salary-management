package com.salarymanagement.dto;

import com.salarymanagement.domain.EmploymentStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record EmployeeRequest(@NotBlank @Size(max = 30) String employeeNumber,
        @NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 120) String jobTitle, @NotBlank @Size(max = 80) String country,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency, @NotNull EmploymentStatus employmentStatus,
        @NotNull @PastOrPresent LocalDate hireDate) {
}
