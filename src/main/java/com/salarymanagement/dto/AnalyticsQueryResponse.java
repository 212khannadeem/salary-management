package com.salarymanagement.dto;

public record AnalyticsQueryResponse(String question, String intent, String summary, String answer, Object data) {
}
