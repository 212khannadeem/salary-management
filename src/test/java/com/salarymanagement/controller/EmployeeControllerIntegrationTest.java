package com.salarymanagement.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.salarymanagement.domain.Compensation;
import com.salarymanagement.domain.Employee;
import com.salarymanagement.domain.EmploymentStatus;
import com.salarymanagement.domain.PayFrequency;
import com.salarymanagement.repository.CompensationRepository;
import com.salarymanagement.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @BeforeEach
    void setUp() {
        compensationRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void createsEmployeeSuccessfully() throws Exception {
        String payload = """
                {
                  "employeeNumber": "EMP-1001",
                  "firstName": "Ava",
                  "lastName": "Jones",
                  "email": "ava.jones@example.com",
                  "department": "Engineering",
                  "jobTitle": "Senior Developer",
                  "country": "India",
                  "currency": "INR",
                  "employmentStatus": "ACTIVE",
                  "hireDate": "2022-01-15"
                }
                """;

        mockMvc.perform(post("/api/v1/employees").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeNumber").value("EMP-1001"))
                .andExpect(jsonPath("$.email").value("ava.jones@example.com"))
                .andExpect(jsonPath("$.currency").value("INR"));
    }

    @Test
    void listsEmployeesWithFiltersAndPagination() throws Exception {
        employeeRepository.saveAll(List.of(
                employee("EMP-2001", "Engineering", "India", "ACTIVE", "Aisha", "Patel", "aisha.patel@example.com"),
                employee("EMP-2002", "Engineering", "India", "ACTIVE", "Aiden", "Smith", "aiden.smith@example.com"),
                employee("EMP-2003", "Finance", "United States", "ON_LEAVE", "Maya", "Brown", "maya.brown@example.com")));

        mockMvc.perform(get("/api/v1/employees")
                        .param("department", "Engineering")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void rejectsInvalidFilterAndSortParameters() throws Exception {
        mockMvc.perform(get("/api/v1/employees").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));

        mockMvc.perform(get("/api/v1/employees").param("sort", "createdAt,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void allowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/employees")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void resolvesEffectiveCompensationForRequestedDate() throws Exception {
        Employee employee = employeeRepository.save(employee("EMP-3001", "Engineering", "India", "ACTIVE",
                "Priya", "Kumar", "priya.kumar@example.com"));
        compensationRepository.save(compensation(employee, new BigDecimal("50000.00"), "INR",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));
        compensationRepository.save(compensation(employee, new BigDecimal("55000.00"), "INR",
                LocalDate.of(2025, 1, 1), null));

        mockMvc.perform(get("/api/v1/employees/{id}/compensation/effective", employee.getId())
                        .param("date", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseSalary").value(55000.00));
    }

    @Test
    void returnsDepartmentAnalyticsGroupedByCurrency() throws Exception {
        Employee engineeringOne = employeeRepository.save(employee("EMP-4001", "Engineering", "India", "ACTIVE",
                "Daniel", "Lee", "daniel.lee@example.com"));
        Employee engineeringTwo = employeeRepository.save(employee("EMP-4002", "Engineering", "India", "ACTIVE",
                "Sara", "Ng", "sara.ng@example.com"));
        Employee finance = employeeRepository.save(employee("EMP-4003", "Finance", "United States", "ACTIVE",
                "Emma", "Clark", "emma.clark@example.com"));

        compensationRepository.save(compensation(engineeringOne, new BigDecimal("90000.00"), "USD",
                LocalDate.of(2024, 1, 1), null));
        compensationRepository.save(compensation(engineeringTwo, new BigDecimal("120000.00"), "USD",
                LocalDate.of(2024, 1, 1), null));
        compensationRepository.save(compensation(finance, new BigDecimal("60000.00"), "EUR",
                LocalDate.of(2024, 1, 1), null));

        mockMvc.perform(get("/api/v1/analytics/salary/by-department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.group == 'Engineering' && @.currency == 'USD')].employeeCount").value(2))
                .andExpect(jsonPath("$[?(@.group == 'Finance' && @.currency == 'EUR')].averageSalary").value(60000.00));
    }

    private Employee employee(String employeeNumber, String department, String country, String status,
            String firstName, String lastName, String email) {
        Employee employee = new Employee();
        employee.setEmployeeNumber(employeeNumber);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setJobTitle("Engineer");
        employee.setCountry(country);
        employee.setCurrency("USD");
        employee.setEmploymentStatus(EmploymentStatus.valueOf(status));
        employee.setHireDate(LocalDate.of(2020, 1, 15));
        return employee;
    }

    private Compensation compensation(Employee employee, BigDecimal baseSalary, String currency,
            LocalDate effectiveFrom, LocalDate effectiveTo) {
        Compensation compensation = new Compensation();
        compensation.setEmployee(employee);
        compensation.setBaseSalary(baseSalary);
        compensation.setCurrency(currency);
        compensation.setBonus(BigDecimal.ZERO);
        compensation.setAllowances(BigDecimal.ZERO);
        compensation.setPayFrequency(PayFrequency.ANNUAL);
        compensation.setEffectiveFrom(effectiveFrom);
        compensation.setEffectiveTo(effectiveTo);
        return compensation;
    }
}
