package com.salarymanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyticsQueryRequest(@NotBlank String question) {
}
