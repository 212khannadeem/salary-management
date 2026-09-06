package com.salarymanagement.dto;

import com.salarymanagement.domain.EmploymentStatus;
import java.time.*;

public record EmployeeResponse(Long id, String employeeNumber, String firstName, String lastName, String email,
        String department, String jobTitle, String country, String currency, EmploymentStatus employmentStatus,
        LocalDate hireDate) {
}
