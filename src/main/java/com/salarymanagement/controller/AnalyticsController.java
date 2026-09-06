package com.salarymanagement.controller;

import com.salarymanagement.dto.CurrencySummary;
import com.salarymanagement.dto.SalaryBandSummary;
import com.salarymanagement.dto.SalaryGroupSummary;
import com.salarymanagement.dto.SalaryRangeSummary;
import com.salarymanagement.service.AnalyticsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/salary/by-department")
    public List<SalaryGroupSummary> byDepartment() {
        return analyticsService.byDepartment();
    }

    @GetMapping("/salary/by-country")
    public List<SalaryGroupSummary> byCountry() {
        return analyticsService.byCountry();
    }

    @GetMapping("/salary/by-job-title")
    public List<SalaryGroupSummary> byJobTitle() {
        return analyticsService.byJobTitle();
    }

    @GetMapping("/salary/bands")
    public List<SalaryBandSummary> salariesByBand() {
        return analyticsService.bands();
    }

    @GetMapping("/salary/currencies")
    public List<CurrencySummary> byCurrency() {
        return analyticsService.currencies();
    }

    @GetMapping("/salary/range")
    public List<SalaryRangeSummary> range() {
        return analyticsService.range();
    }
}
