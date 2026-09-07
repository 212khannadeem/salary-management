package com.salarymanagement.seed;

import com.salarymanagement.domain.Compensation;
import com.salarymanagement.domain.Employee;
import com.salarymanagement.domain.EmploymentStatus;
import com.salarymanagement.domain.PayFrequency;
import com.salarymanagement.repository.CompensationRepository;
import com.salarymanagement.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class EmployeeSeedRunner implements ApplicationRunner {
    private static final int TARGET_EMPLOYEES = 10_000;
    private static final List<String> FIRST_NAMES = List.of(
            "Aiden", "Aisha", "Amelia", "Aria", "Benjamin", "Charlotte", "Daniel", "David", "Ella", "Emma",
            "Ethan", "Grace", "Hannah", "Henry", "Isabella", "Jack", "James", "Jasmine", "John", "Julia",
            "Liam", "Lucas", "Maya", "Mia", "Noah", "Olivia", "Owen", "Priya", "Riley", "Ryan", "Sofia",
            "Sophia", "Thomas", "Victoria", "William", "Zoe");
    private static final List<String> LAST_NAMES = List.of(
            "Adams", "Brown", "Clark", "Davis", "Garcia", "Harris", "Jones", "Kim", "Lee", "Martin",
            "Nguyen", "Patel", "Roberts", "Smith", "Taylor", "Thomas", "Walker", "White", "Williams", "Young");
    private static final List<String> DEPARTMENTS = List.of(
            "Engineering", "Finance", "Human Resources", "Marketing", "Operations", "Sales", "Support");
    private static final List<String> JOB_TITLES = List.of(
            "Software Engineer", "Senior Developer", "Account Manager", "Analyst", "HR Manager", "Operations Lead",
            "Product Manager", "Sales Representative", "Customer Success Lead", "Finance Manager");
    private static final List<String> COUNTRIES = List.of("India", "United States", "United Kingdom", "Germany", "Canada", "Australia");
    private static final Map<String, String> COUNTRY_CURRENCIES = Map.of(
            "India", "INR",
            "United States", "USD",
            "United Kingdom", "GBP",
            "Germany", "EUR",
            "Canada", "CAD",
            "Australia", "AUD");

    private final EmployeeRepository employees;
    private final CompensationRepository compensations;

    public EmployeeSeedRunner(EmployeeRepository employees, CompensationRepository compensations) {
        this.employees = employees;
        this.compensations = compensations;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (employees.count() > 0) {
            return;
        }

        List<Employee> employeeBatch = new ArrayList<>(TARGET_EMPLOYEES);
        for (int i = 1; i <= TARGET_EMPLOYEES; i++) {
            Employee employee = new Employee();
            employee.setEmployeeNumber("EMP-" + String.format("%05d", i));
            employee.setFirstName(FIRST_NAMES.get((i - 1) % FIRST_NAMES.size()));
            employee.setLastName(LAST_NAMES.get((i * 7) % LAST_NAMES.size()));
            employee.setEmail("employee" + i + "@example.com");
            employee.setDepartment(DEPARTMENTS.get((i - 1) % DEPARTMENTS.size()));
            employee.setJobTitle(JOB_TITLES.get((i * 3) % JOB_TITLES.size()));
            employee.setCountry(COUNTRIES.get((i * 5) % COUNTRIES.size()));
            employee.setCurrency(COUNTRY_CURRENCIES.get(employee.getCountry()));
            employee.setEmploymentStatus(i % 10 == 0 ? EmploymentStatus.ON_LEAVE : EmploymentStatus.ACTIVE);
            employee.setHireDate(LocalDate.of(2014 + (i % 10), 1 + (i % 12), 1 + (i % 28)));
            employeeBatch.add(employee);
        }

        List<Employee> savedEmployees = employees.saveAll(employeeBatch);
        List<Compensation> compensationBatch = new ArrayList<>(TARGET_EMPLOYEES);
        for (int i = 0; i < savedEmployees.size(); i++) {
            Employee employee = savedEmployees.get(i);
            BigDecimal baseSalary = BigDecimal.valueOf(40000 + ((i * 173) % 180000));
            Compensation compensation = new Compensation();
            compensation.setEmployee(employee);
            compensation.setBaseSalary(baseSalary.setScale(2, RoundingMode.HALF_UP));
            compensation.setCurrency(employee.getCurrency());
            compensation.setBonus(BigDecimal.valueOf((i % 7L) * 1000L).setScale(2, RoundingMode.HALF_UP));
            compensation.setAllowances(BigDecimal.valueOf((i % 5L) * 750L).setScale(2, RoundingMode.HALF_UP));
            compensation.setPayFrequency(i % 2 == 0 ? PayFrequency.ANNUAL : PayFrequency.MONTHLY);
            compensation.setEffectiveFrom(LocalDate.of(2020 + (i % 6), 1 + (i % 12), 1 + (i % 28)));
            compensation.setEffectiveTo(null);
            compensationBatch.add(compensation);
        }

        compensations.saveAll(compensationBatch);
    }
}
