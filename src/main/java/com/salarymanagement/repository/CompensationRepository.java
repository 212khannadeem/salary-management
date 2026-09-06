package com.salarymanagement.repository;

import com.salarymanagement.domain.Compensation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.*;
import java.util.*;

public interface CompensationRepository extends JpaRepository<Compensation, Long> {
    @Query("select c from Compensation c join fetch c.employee e where c.employee.id=:id and c.effectiveFrom<=:date and (c.effectiveTo is null or c.effectiveTo>=:date) order by c.effectiveFrom desc")
    List<Compensation> effective(@Param("id") Long id, @Param("date") LocalDate date);

    @Query("select c from Compensation c join fetch c.employee e where c.employee.id=:id order by c.effectiveFrom desc")
    List<Compensation> findByEmployeeIdOrderByEffectiveFromDesc(Long id);

    @Query("select c from Compensation c join fetch c.employee e where c.effectiveTo is null")
    List<Compensation> findCurrent();

    @Query("select c from Compensation c join fetch c.employee e")
    List<Compensation> findAllWithEmployee();
}
