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
        if (normalized.contains("country")
                && (normalized.contains("most employees") || normalized.contains("highest number of employees"))) {
            return countryWithMostEmployees(question);
        }
        if (normalized.contains("department")) {
            List<SalaryGroupSummary> data = byDepartment();
            return response(question, "SALARY_BY_DEPARTMENT",
                    "Salary averages, minimums, maximums, and employee counts grouped by department and currency.",
                    groupAnswer("Average salary by department", data), data);
        }
        if (normalized.contains("country")) {
            List<SalaryGroupSummary> data = byCountry();
            return response(question, "SALARY_BY_COUNTRY",
                    "Salary averages, minimums, maximums, and employee counts grouped by country and currency.",
                    groupAnswer("Average salary by country", data), data);
        }
        if (normalized.contains("job title") || normalized.contains("role")) {
            List<SalaryGroupSummary> data = byJobTitle();
            return response(question, "SALARY_BY_JOB_TITLE",
                    "Salary averages, minimums, maximums, and employee counts grouped by job title and currency.",
                    groupAnswer("Average salary by job title", data), data);
        }
        if (normalized.contains("band")) {
            List<SalaryBandSummary> data = bands();
            return response(question, "SALARY_BANDS",
                    "Employee counts and percentages grouped into annual salary bands by currency.",
                    bandAnswer(data), data);
        }
        if (normalized.contains("currency") || normalized.contains("currencies")) {
            List<CurrencySummary> data = currencies();
            return response(question, "SALARY_BY_CURRENCY",
                    "Current employee counts and total annual base compensation grouped by currency.",
                    currencyAnswer(data), data);
        }
        if (normalized.contains("range") || normalized.contains("highest") || normalized.contains("lowest")) {
            List<SalaryRangeSummary> data = range();
            return response(question, "SALARY_RANGE",
                    "Minimum, maximum, average, and employee count for current annual salaries by currency.",
                    rangeAnswer(data), data);
        }
        throw new ApiException("UNSUPPORTED_ANALYTICS_QUERY",
                "Supported questions cover salary by department, country, job title, bands, currencies, and range");
    }

    private AnalyticsQueryResponse countryWithMostEmployees(String question) {
        List<SalaryGroupSummary> countryResults = byCountry();
        Map<String, Long> employeeCounts = new TreeMap<>();
        for (SalaryGroupSummary result : countryResults) {
            employeeCounts.merge(result.group(), result.employeeCount(), Long::sum);
        }
        Map.Entry<String, Long> highest = employeeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        String answer = highest == null
                ? "No current employee compensation data is available."
                : highest.getKey() + " has the most employees with " + highest.getValue() + " employees.";
        return response(question, "EMPLOYEE_COUNT_BY_COUNTRY",
                "Employee counts grouped by country.", answer, employeeCounts);
    }

    private AnalyticsQueryResponse response(String question, String intent, String summary, String answer,
            Object data) {
        return new AnalyticsQueryResponse(question, intent, summary, answer, data);
    }

    private String groupAnswer(String label, List<SalaryGroupSummary> data) {
        if (data.isEmpty()) {
            return "No current salary data is available.";
        }
        return label + ": " + data.stream()
                .map(item -> item.group() + " (" + item.currency() + " "
                        + item.averageSalary().toPlainString() + " average, "
                        + item.employeeCount() + " employees)")
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private String bandAnswer(List<SalaryBandSummary> data) {
        if (data.isEmpty()) {
            return "No current salary-band data is available.";
        }
        return "Salary-band distribution: " + data.stream()
                .map(item -> item.currency() + " " + item.band() + " "
                        + item.employeeCount() + " employees (" + item.percentage().toPlainString() + "%)")
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private String currencyAnswer(List<CurrencySummary> data) {
        if (data.isEmpty()) {
            return "No current currency data is available.";
        }
        return "Currency distribution: " + data.stream()
                .map(item -> item.currency() + " " + item.employeeCount() + " employees")
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private String rangeAnswer(List<SalaryRangeSummary> data) {
        if (data.isEmpty()) {
            return "No current salary-range data is available.";
        }
        return "Salary ranges: " + data.stream()
                .map(item -> item.currency() + " average " + item.averageSalary().toPlainString()
                        + ", minimum " + item.minimumSalary().toPlainString()
                        + ", maximum " + item.maximumSalary().toPlainString())
                .collect(java.util.stream.Collectors.joining("; "));
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
