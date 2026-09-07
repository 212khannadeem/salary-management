package com.salarymanagement.repository;

import com.salarymanagement.domain.Employee;
import org.springframework.data.jpa.repository.*;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    boolean existsByEmployeeNumber(String x);

    boolean existsByEmailIgnoreCase(String x);

    boolean existsByEmployeeNumberAndIdNot(String employeeNumber, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
