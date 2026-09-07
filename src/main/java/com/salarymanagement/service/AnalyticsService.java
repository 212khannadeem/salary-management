package com.salarymanagement.service;

import com.salarymanagement.domain.Compensation;
import com.salarymanagement.dto.CurrencySummary;
import com.salarymanagement.dto.AnalyticsQueryResponse;
import com.salarymanagement.dto.SalaryBandSummary;
import com.salarymanagement.dto.SalaryGroupSummary;
import com.salarymanagement.dto.SalaryRangeSummary;
import com.salarymanagement.repository.CompensationRepository;
import com.salarymanagement.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Locale;
import com.salarymanagement.exception.ApiException;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final EmployeeRepository employees;
    private final CompensationRepository compensations;

    public AnalyticsService(EmployeeRepository employees, CompensationRepository compensations) {
        this.employees = employees;
        this.compensations = compensations;
    }

    public List<SalaryGroupSummary> byDepartment() {
        return summarizeBy("department");
    }

    public List<SalaryGroupSummary> byCountry() {
        return summarizeBy("country");
    }

    public List<SalaryGroupSummary> byJobTitle() {
        return summarizeBy("jobTitle");
    }

    public List<SalaryBandSummary> bands() {
        Map<String, Map<String, Long>> bandTotals = new TreeMap<>();
        Map<String, Long> currencyTotals = new HashMap<>();
        for (Compensation compensation : currentCompensations()) {
            String currency = compensation.getCurrency();
            String band = salaryBand(annualSalary(compensation));
            bandTotals.computeIfAbsent(currency, key -> new TreeMap<>()).merge(band, 1L, Long::sum);
            currencyTotals.merge(currency, 1L, Long::sum);
        }

        List<SalaryBandSummary> results = new ArrayList<>();
        for (String currency : bandTotals.keySet()) {
            long total = currencyTotals.getOrDefault(currency, 0L);
            for (Map.Entry<String, Long> entry : bandTotals.get(currency).entrySet()) {
                BigDecimal percentage = total == 0 ? BigDecimal.ZERO
                        : BigDecimal.valueOf(entry.getValue()).multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
                results.add(new SalaryBandSummary(currency, entry.getKey(), entry.getValue(), percentage));
            }
        }
        return results;
    }

    public List<CurrencySummary> currencies() {
        Map<String, CurrencyAccumulator> totals = new TreeMap<>();
        for (Compensation compensation : currentCompensations()) {
            totals.computeIfAbsent(compensation.getCurrency(), key -> new CurrencyAccumulator())
                    .add(annualSalary(compensation));
        }

        return totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CurrencySummary(entry.getKey(), entry.getValue().count,
                        entry.getValue().total))
                .toList();
    }

    public List<SalaryRangeSummary> range() {
        Map<String, SalaryAggregate> aggregateByCurrency = new TreeMap<>();
        for (Compensation compensation : currentCompensations()) {
            aggregateByCurrency.computeIfAbsent(compensation.getCurrency(), key -> new SalaryAggregate())
                    .add(annualSalary(compensation));
        }

        List<SalaryRangeSummary> result = new ArrayList<>();
        for (Map.Entry<String, SalaryAggregate> entry : aggregateByCurrency.entrySet()) {
            SalaryAggregate aggregate = entry.getValue();
            result.add(new SalaryRangeSummary(entry.getKey(), aggregate.min(), aggregate.max(),
                    aggregate.average(), aggregate.count()));
        }
        return result;
    }

    public AnalyticsQueryResponse answer(String question) {
        String normalized = question.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("department")) {
            return new AnalyticsQueryResponse("SALARY_BY_DEPARTMENT", byDepartment());
        }
        if (normalized.contains("country")) {
            return new AnalyticsQueryResponse("SALARY_BY_COUNTRY", byCountry());
        }
        if (normalized.contains("job title") || normalized.contains("role")) {
            return new AnalyticsQueryResponse("SALARY_BY_JOB_TITLE", byJobTitle());
        }
        if (normalized.contains("band")) {
            return new AnalyticsQueryResponse("SALARY_BANDS", bands());
        }
        if (normalized.contains("currency") || normalized.contains("currencies")) {
            return new AnalyticsQueryResponse("SALARY_BY_CURRENCY", currencies());
        }
        if (normalized.contains("range") || normalized.contains("highest") || normalized.contains("lowest")) {
            return new AnalyticsQueryResponse("SALARY_RANGE", range());
        }
        throw new ApiException("UNSUPPORTED_ANALYTICS_QUERY",
                "Supported questions cover salary by department, country, job title, bands, currencies, and range");
    }

    private List<SalaryGroupSummary> summarizeBy(String fieldName) {
        Map<String, Map<String, SalaryAggregate>> grouped = new HashMap<>();
        for (Compensation compensation : currentCompensations()) {
            String group = employeeGroup(compensation.getEmployee(), fieldName);
            String currency = compensation.getCurrency();
            grouped.computeIfAbsent(group, key -> new TreeMap<>())
                    .computeIfAbsent(currency, key -> new SalaryAggregate())
                    .add(compensation.getBaseSalary());
        }

        List<SalaryGroupSummary> results = new ArrayList<>();
        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> entry.getValue().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(currencyEntry -> results.add(new SalaryGroupSummary(entry.getKey(), currencyEntry.getKey(),
                                currencyEntry.getValue().count(), currencyEntry.getValue().average(),
                                currencyEntry.getValue().min(), currencyEntry.getValue().max()))));
        return results;
    }

    private String employeeGroup(com.salarymanagement.domain.Employee employee, String fieldName) {
        return switch (fieldName) {
            case "department" -> employee.getDepartment();
            case "country" -> employee.getCountry();
            case "jobTitle" -> employee.getJobTitle();
            default -> throw new IllegalArgumentException("Unsupported analytics group: " + fieldName);
        };
    }

    private List<Compensation> currentCompensations() {
        return compensations.findAllWithEmployee().stream()
                .filter(this::isEffectiveToday)
                .sorted(Comparator.comparing(Compensation::getCurrency)
                        .thenComparing(Compensation::getBaseSalary))
                .toList();
    }

    private boolean isEffectiveToday(Compensation compensation) {
        LocalDate effectiveFrom = compensation.getEffectiveFrom();
        LocalDate effectiveTo = compensation.getEffectiveTo();
        LocalDate today = LocalDate.now();
        return !effectiveFrom.isAfter(today) && (effectiveTo == null || !effectiveTo.isBefore(today));
    }

    private BigDecimal annualSalary(Compensation compensation) {
        if (compensation.getPayFrequency() == com.salarymanagement.domain.PayFrequency.MONTHLY) {
            return compensation.getBaseSalary().multiply(BigDecimal.valueOf(12));
        }
        return compensation.getBaseSalary();
    }

    private String salaryBand(BigDecimal salary) {
        if (salary.compareTo(new BigDecimal("50000")) < 0) {
            return "UNDER_50K";
        }
        if (salary.compareTo(new BigDecimal("75000")) < 0) {
            return "50K_75K";
        }
        if (salary.compareTo(new BigDecimal("100000")) < 0) {
            return "75K_100K";
        }
        if (salary.compareTo(new BigDecimal("150000")) < 0) {
            return "100K_150K";
        }
        return "150K_PLUS";
    }

    private static final class CurrencyAccumulator {
        private long count;
        private BigDecimal total = BigDecimal.ZERO;

        void add(BigDecimal value) {
            count++;
            total = total.add(value);
        }
    }

    private static final class SalaryAggregate {
        private long count;
        private BigDecimal total = BigDecimal.ZERO;
        private BigDecimal min = null;
        private BigDecimal max = null;

        void add(BigDecimal value) {
            count++;
            total = total.add(value);
            min = min == null ? value : min.min(value);
            max = max == null ? value : max.max(value);
        }

        long count() {
            return count;
        }

        BigDecimal average() {
            if (count == 0) {
                return BigDecimal.ZERO;
            }
            return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }

        BigDecimal min() {
            return min == null ? BigDecimal.ZERO : min;
        }

        BigDecimal max() {
            return max == null ? BigDecimal.ZERO : max;
        }
    }
}
