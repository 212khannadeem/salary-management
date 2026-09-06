package com.salarymanagement.service;

import com.salarymanagement.domain.Compensation;
import com.salarymanagement.domain.Employee;
import com.salarymanagement.dto.CompensationRequest;
import com.salarymanagement.dto.CompensationResponse;
import com.salarymanagement.exception.ApiException;
import com.salarymanagement.exception.ConflictException;
import com.salarymanagement.exception.NotFoundException;
import com.salarymanagement.repository.CompensationRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompensationService {

    private final EmployeeService employeeService;
    private final CompensationRepository compensations;

    public CompensationService(EmployeeService employeeService, CompensationRepository compensations) {
        this.employeeService = employeeService;
        this.compensations = compensations;
    }

    @Transactional
    public CompensationResponse create(Long employeeId, CompensationRequest request) {
        validateDates(request);
        Employee employee = employeeService.entity(employeeId);
        List<Compensation> history = compensations.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);

        for (Compensation existing : history) {
            if (!overlaps(existing, request.effectiveFrom(), request.effectiveTo())) {
                continue;
            }

            if (existing.getEffectiveTo() == null && existing.getEffectiveFrom().isBefore(request.effectiveFrom())) {
                existing.setEffectiveTo(request.effectiveFrom().minusDays(1));
            } else {
                throw new ConflictException("OVERLAPPING_COMPENSATION",
                        "Compensation effective period overlaps an existing record");
            }
        }

        Compensation compensation = map(request, new Compensation(), employee);
        return toResponse(compensations.save(compensation));
    }

    @Transactional
    public CompensationResponse update(Long employeeId, Long compensationId, CompensationRequest request) {
        validateDates(request);
        Employee employee = employeeService.entity(employeeId);
        Compensation compensation = compensations.findById(compensationId)
                .filter(existing -> existing.getEmployee().getId().equals(employeeId))
                .orElseThrow(() -> new NotFoundException("Compensation " + compensationId + " was not found"));

        for (Compensation other : compensations.findByEmployeeIdOrderByEffectiveFromDesc(employeeId)) {
            if (!other.getId().equals(compensationId)
                    && overlaps(other, request.effectiveFrom(), request.effectiveTo())) {
                throw new ConflictException("OVERLAPPING_COMPENSATION",
                        "Compensation effective period overlaps an existing record");
            }
        }

        return toResponse(map(request, compensation, employee));
    }

    public CompensationResponse current(Long employeeId) {
        employeeService.entity(employeeId);
        return effective(employeeId, LocalDate.now());
    }

    public CompensationResponse effective(Long employeeId, LocalDate effectiveDate) {
        List<Compensation> effectiveCompensations = compensations.effective(employeeId, effectiveDate);
        if (effectiveCompensations.isEmpty()) {
            throw new NotFoundException("No compensation is effective on " + effectiveDate);
        }
        return toResponse(effectiveCompensations.getFirst());
    }

    public List<CompensationResponse> history(Long employeeId) {
        employeeService.entity(employeeId);
        return compensations.findByEmployeeIdOrderByEffectiveFromDesc(employeeId).stream().map(this::toResponse)
                .toList();
    }

    private boolean overlaps(Compensation compensation, LocalDate proposedStart, LocalDate proposedEnd) {
        LocalDate existingEnd = compensation.getEffectiveTo();
        return (proposedEnd == null || !compensation.getEffectiveFrom().isAfter(proposedEnd))
                && (existingEnd == null || !existingEnd.isBefore(proposedStart));
    }

    private void validateDates(CompensationRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new ApiException("INVALID_DATE_RANGE", "effectiveTo must not be before effectiveFrom");
        }
    }

    private Compensation map(CompensationRequest request, Compensation compensation, Employee employee) {
        compensation.setEmployee(employee);
        compensation.setBaseSalary(request.baseSalary().setScale(2, RoundingMode.HALF_UP));
        compensation.setCurrency(request.currency());
        compensation.setBonus(
                (request.bonus() == null ? BigDecimal.ZERO : request.bonus()).setScale(2, RoundingMode.HALF_UP));
        compensation.setAllowances((request.allowances() == null ? BigDecimal.ZERO : request.allowances()).setScale(2,
                RoundingMode.HALF_UP));
        compensation.setPayFrequency(request.payFrequency());
        compensation.setEffectiveFrom(request.effectiveFrom());
        compensation.setEffectiveTo(request.effectiveTo());
        return compensation;
    }

    public CompensationResponse toResponse(Compensation compensation) {
        return new CompensationResponse(compensation.getId(), compensation.getBaseSalary(), compensation.getCurrency(),
                compensation.getBonus(), compensation.getAllowances(), compensation.getPayFrequency(),
                compensation.getEffectiveFrom(), compensation.getEffectiveTo());
    }
}
