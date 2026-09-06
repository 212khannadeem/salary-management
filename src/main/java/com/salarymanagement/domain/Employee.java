package com.salarymanagement.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "employee_number", nullable = false, unique = true, length = 30)
    private String employeeNumber;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false, length = 100)
    private String department;
    @Column(name = "job_title", nullable = false, length = 120)
    private String jobTitle;
    @Column(nullable = false, length = 80)
    private String country;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus;
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compensation> compensations = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String x) {
        employeeNumber = x;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String x) {
        firstName = x;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String x) {
        lastName = x;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String x) {
        email = x;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String x) {
        department = x;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String x) {
        jobTitle = x;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String x) {
        country = x;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String x) {
        currency = x;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus x) {
        employmentStatus = x;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate x) {
        hireDate = x;
    }

    public List<Compensation> getCompensations() {
        return compensations;
    }

    public void setCompensations(List<Compensation> compensations) {
        this.compensations = compensations;
    }
}
