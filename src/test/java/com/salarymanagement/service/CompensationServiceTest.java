package com.salarymanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.salarymanagement.domain.*;
import com.salarymanagement.dto.CompensationRequest;
import com.salarymanagement.exception.ConflictException;
import com.salarymanagement.exception.NotFoundException;
import com.salarymanagement.repository.CompensationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CompensationServiceTest {
    private CompensationRepository repository;
    private EmployeeService employees;
    private CompensationService service;
    private final Employee employee = new Employee();

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CompensationRepository.class);
        employees = Mockito.mock(EmployeeService.class);
        service = new CompensationService(employees, repository);
        when(employees.entity(1L)).thenReturn(employee);
    }

    @Test
    void addsNewSalaryAndClosesPriorOpenEndedRecord() {
        Compensation current = compensation("50000", LocalDate.of(2024, 1, 1), null);
        when(repository.findByEmployeeIdOrderByEffectiveFromDesc(1L)).thenReturn(List.of(current));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(1L, request("55000", LocalDate.of(2025, 1, 1), null));

        assertThat(current.getEffectiveTo()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(result.baseSalary()).isEqualByComparingTo("55000.00");
        assertThat(result.bonus()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsOverlappingClosedPeriods() {
        Compensation existing = compensation("50000", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        when(repository.findByEmployeeIdOrderByEffectiveFromDesc(1L)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.create(1L, request("55000", LocalDate.of(2024, 6, 1), null)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("overlaps");
        verify(repository, never()).save(any());
    }

    @Test
    void resolvesSalaryEffectiveOnRequestedDate() {
        Compensation salary = compensation("55000", LocalDate.of(2025, 1, 1), null);
        when(repository.effective(eq(1L), eq(LocalDate.of(2025, 6, 1)))).thenReturn(List.of(salary));

        assertThat(service.effective(1L, LocalDate.of(2025, 6, 1)).baseSalary()).isEqualByComparingTo("55000");
    }

    @Test
    void returnsNotFoundWhenNoSalaryIsEffective() {
        when(repository.effective(anyLong(), any())).thenReturn(List.of());
        assertThatThrownBy(() -> service.effective(1L, LocalDate.of(2023, 1, 1))).isInstanceOf(NotFoundException.class);
    }

    private CompensationRequest request(String salary, LocalDate from, LocalDate to) {
        return new CompensationRequest(new BigDecimal(salary), "USD", null, null, PayFrequency.ANNUAL, from, to);
    }

    private Compensation compensation(String salary, LocalDate from, LocalDate to) {
        Compensation compensation = new Compensation();
        compensation.setEmployee(employee);
        compensation.setBaseSalary(new BigDecimal(salary));
        compensation.setCurrency("USD");
        compensation.setBonus(BigDecimal.ZERO);
        compensation.setAllowances(BigDecimal.ZERO);
        compensation.setPayFrequency(PayFrequency.ANNUAL);
        compensation.setEffectiveFrom(from);
        compensation.setEffectiveTo(to);
        return compensation;
    }
}
