package com.salarymanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.salarymanagement.domain.Compensation;
import com.salarymanagement.domain.Employee;
import com.salarymanagement.domain.EmploymentStatus;
import com.salarymanagement.domain.PayFrequency;
import com.salarymanagement.repository.CompensationRepository;
import com.salarymanagement.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalyticsServiceTest {
    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final CompensationRepository compensations = mock(CompensationRepository.class);
    private final AnalyticsService service = new AnalyticsService(employees, compensations);

    @Test
    void groupsSalariesByDepartmentAndKeepsCurrenciesSeparated() {
        when(compensations.findAllWithEmployee()).thenReturn(List.of(
                compensation(employee("Engineering", "India", "Senior Developer", "USD"), new BigDecimal("90000"), "USD"),
                compensation(employee("Engineering", "India", "Senior Developer", "USD"), new BigDecimal("120000"), "USD"),
                compensation(employee("Finance", "United States", "Analyst", "EUR"), new BigDecimal("60000"), "EUR")));

        var result = service.byDepartment();

        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(item -> {
            assertThat(item.group()).isEqualTo("Engineering");
            assertThat(item.currency()).isEqualTo("USD");
            assertThat(item.employeeCount()).isEqualTo(2);
            assertThat(item.averageSalary()).isEqualByComparingTo("105000.00");
        });
        assertThat(result).anySatisfy(item -> {
            assertThat(item.group()).isEqualTo("Finance");
            assertThat(item.currency()).isEqualTo("EUR");
            assertThat(item.employeeCount()).isEqualTo(1);
            assertThat(item.averageSalary()).isEqualByComparingTo("60000.00");
        });
    }

    @Test
    void calculatesBandPercentagesPerCurrency() {
        when(compensations.findAllWithEmployee()).thenReturn(List.of(
                compensation(employee("Engineering", "India", "Developer", "USD"), new BigDecimal("60000"), "USD"),
                compensation(employee("Engineering", "India", "Developer", "USD"), new BigDecimal("90000"), "USD"),
                compensation(employee("Finance", "United States", "Analyst", "USD"), new BigDecimal("110000"), "USD"),
                compensation(employee("Operations", "Canada", "Manager", "EUR"), new BigDecimal("50000"), "EUR")));

        var result = service.bands();

        assertThat(result).anySatisfy(item -> {
            assertThat(item.currency()).isEqualTo("USD");
            assertThat(item.band()).isEqualTo("50K_75K");
            assertThat(item.employeeCount()).isEqualTo(1);
        });
        assertThat(result).anySatisfy(item -> {
            assertThat(item.currency()).isEqualTo("USD");
            assertThat(item.band()).isEqualTo("75K_100K");
            assertThat(item.employeeCount()).isEqualTo(1);
        });
        assertThat(result).anySatisfy(item -> {
            assertThat(item.currency()).isEqualTo("EUR");
            assertThat(item.band()).isEqualTo("50K_75K");
            assertThat(item.employeeCount()).isEqualTo(1);
        });
    }

    private Employee employee(String department, String country, String jobTitle, String currency) {
        Employee employee = new Employee();
        employee.setDepartment(department);
        employee.setCountry(country);
        employee.setJobTitle(jobTitle);
        employee.setCurrency(currency);
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return employee;
    }

    private Compensation compensation(Employee employee, BigDecimal baseSalary, String currency) {
        Compensation compensation = new Compensation();
        compensation.setEmployee(employee);
        compensation.setBaseSalary(baseSalary);
        compensation.setCurrency(currency);
        compensation.setBonus(BigDecimal.ZERO);
        compensation.setAllowances(BigDecimal.ZERO);
        compensation.setPayFrequency(PayFrequency.ANNUAL);
        compensation.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        compensation.setEffectiveTo(null);
        return compensation;
    }
}
