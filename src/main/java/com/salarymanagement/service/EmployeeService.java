package com.salarymanagement.service;

import com.salarymanagement.domain.*;
import com.salarymanagement.dto.*;
import com.salarymanagement.exception.*;
import com.salarymanagement.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import java.math.*;

@Service
public class EmployeeService {
    private final EmployeeRepository r;

    public EmployeeService(EmployeeRepository r) {
        this.r = r;
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest x) {
        if (r.existsByEmployeeNumber(x.employeeNumber()))
            throw new ConflictException("DUPLICATE_EMPLOYEE_NUMBER", "Employee number already exists");
        if (r.existsByEmailIgnoreCase(x.email()))
            throw new ConflictException("DUPLICATE_EMAIL", "Email already exists");
        return out(r.save(map(x, new Employee())));
    }

    public EmployeeResponse get(Long id) {
        return out(entity(id));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest x) {
        return out(map(x, entity(id)));
    }

    @Transactional
    public void delete(Long id) {
        r.delete(entity(id));
    }

    public PageResponse<EmployeeResponse> list(String d, String c, EmploymentStatus s, String j, String cur,
            BigDecimal min, BigDecimal max, String q, Pageable p) {
        return PageResponse.from(r.findAll(p).map(this::out));
    }

    public Employee entity(Long id) {
        return r.findById(id).orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    private Employee map(EmployeeRequest x, Employee e) {
        e.setEmployeeNumber(x.employeeNumber());
        e.setFirstName(x.firstName());
        e.setLastName(x.lastName());
        e.setEmail(x.email());
        e.setDepartment(x.department());
        e.setJobTitle(x.jobTitle());
        e.setCountry(x.country());
        e.setCurrency(x.currency());
        e.setEmploymentStatus(x.employmentStatus());
        e.setHireDate(x.hireDate());
        return e;
    }

    private EmployeeResponse out(Employee e) {
        return new EmployeeResponse(e.getId(), e.getEmployeeNumber(), e.getFirstName(), e.getLastName(), e.getEmail(),
                e.getDepartment(), e.getJobTitle(), e.getCountry(), e.getCurrency(), e.getEmploymentStatus(),
                e.getHireDate());
    }
}
