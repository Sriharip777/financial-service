package com.tcon.financial_service.payment.service;

import com.tcon.financial_service.payment.dto.InstallmentDto;
import com.tcon.financial_service.payment.dto.PaymentRequest;
import com.tcon.financial_service.payment.entity.Installment;
import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payment.entity.PaymentFrequency;
import com.tcon.financial_service.payment.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentService {

    private final InstallmentRepository installmentRepository;

    @Transactional
    public Installment createInstallmentPlan(Payment payment, PaymentRequest request) {
        log.info("Creating installment plan for booking: {}", payment.getBookingId());

        // Validate installment parameters
        if (request.getTotalInstallments() == null || request.getTotalInstallments() <= 0) {
            throw new IllegalArgumentException("Total installments must be greater than 0");
        }

        // Default payment frequency to MONTHLY if not provided
        PaymentFrequency frequency = request.getPaymentFrequency() != null
                ? request.getPaymentFrequency()
                : PaymentFrequency.MONTHLY;

        // Calculate installment amount
        BigDecimal installmentAmount = payment.getAmount()
                .divide(BigDecimal.valueOf(request.getTotalInstallments()), 2, RoundingMode.HALF_UP);

        Installment installment = Installment.builder()
                .bookingId(payment.getBookingId())
                .studentId(payment.getStudentId())
                .teacherId(payment.getTeacherId())
                .totalAmount(payment.getAmount())
                .currency(payment.getCurrency())
                .totalInstallments(request.getTotalInstallments())
                .installmentAmount(installmentAmount)
                .firstPaymentDate(LocalDate.now())
                .paymentFrequency(frequency) // ✅ Now using the frequency
                .paidInstallments(0)
                .remainingInstallments(request.getTotalInstallments())
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(payment.getAmount())
                .isActive(true)
                .build();

        installment = installmentRepository.save(installment);

        log.info("Installment plan created: {} with {} installments at {} frequency",
                installment.getId(),
                installment.getTotalInstallments(),
                frequency);

        return installment;
    }

    @Transactional
    public void updateInstallmentProgress(Payment payment) {
        log.info("Updating installment progress for booking: {}", payment.getBookingId());

        Installment installment = installmentRepository.findByBookingId(payment.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Installment not found for booking: " + payment.getBookingId()));

        // Update paid installments
        int paidInstallments = installment.getPaidInstallments() + 1;
        int remainingInstallments = installment.getTotalInstallments() - paidInstallments;
        BigDecimal paidAmount = installment.getPaidAmount().add(payment.getAmount());
        BigDecimal remainingAmount = installment.getTotalAmount().subtract(paidAmount);

        installment.setPaidInstallments(paidInstallments);
        installment.setRemainingInstallments(remainingInstallments);
        installment.setPaidAmount(paidAmount);
        installment.setRemainingAmount(remainingAmount);

        // Calculate next payment date
        if (remainingInstallments > 0) {
            LocalDate nextPaymentDate = calculateNextPaymentDate(
                    installment.getFirstPaymentDate(),
                    paidInstallments,
                    installment.getPaymentFrequency()
            );
            installment.setNextPaymentDate(nextPaymentDate);
        }

        // Check if all installments are paid
        if (remainingInstallments == 0) {
            installment.setIsActive(false);
            installment.setCompletedAt(LocalDateTime.now());
            log.info("Installment plan completed for booking: {}", payment.getBookingId());
        }

        installmentRepository.save(installment);
    }

    public InstallmentDto getInstallmentByBookingId(String bookingId) {
        Installment installment = installmentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Installment not found for booking: " + bookingId));

        return mapToDto(installment);
    }

    public List<InstallmentDto> getInstallmentsByStudent(String studentId) {
        List<Installment> installments = installmentRepository.findByStudentId(studentId);
        return installments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<InstallmentDto> getActiveInstallments() {
        List<Installment> installments = installmentRepository.findByIsActiveTrue();
        return installments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private LocalDate calculateNextPaymentDate(LocalDate firstPaymentDate,
                                               int paidInstallments,
                                               PaymentFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> firstPaymentDate.plusWeeks(paidInstallments);
            case BIWEEKLY -> firstPaymentDate.plusWeeks(paidInstallments * 2L);
            case MONTHLY -> firstPaymentDate.plusMonths(paidInstallments);
            case QUARTERLY -> firstPaymentDate.plusMonths(paidInstallments * 3L);
            case YEARLY -> firstPaymentDate.plusYears(paidInstallments);
        };
    }

    private InstallmentDto mapToDto(Installment installment) {
        return InstallmentDto.builder()
                .id(installment.getId())
                .bookingId(installment.getBookingId())
                .studentId(installment.getStudentId())
                .teacherId(installment.getTeacherId())
                .totalAmount(installment.getTotalAmount())
                .currency(installment.getCurrency())
                .totalInstallments(installment.getTotalInstallments())
                .installmentAmount(installment.getInstallmentAmount())
                .firstPaymentDate(installment.getFirstPaymentDate())
                .paymentFrequency(installment.getPaymentFrequency())
                .paidInstallments(installment.getPaidInstallments())
                .remainingInstallments(installment.getRemainingInstallments())
                .paidAmount(installment.getPaidAmount())
                .remainingAmount(installment.getRemainingAmount())
                .isActive(installment.getIsActive())
                .createdAt(installment.getCreatedAt())
                .build();
    }
}
