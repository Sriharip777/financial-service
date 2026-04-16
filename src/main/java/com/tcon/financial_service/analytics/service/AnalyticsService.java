package com.tcon.financial_service.analytics.service;

import com.tcon.financial_service.analytics.dto.FinancialSummaryDto;
import com.tcon.financial_service.earnings.entity.TeacherEarnings;
import com.tcon.financial_service.earnings.repository.TeacherEarningsRepository;
import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payment.repository.PaymentRepository;
import com.tcon.financial_service.payout.entity.Payout;
import com.tcon.financial_service.payout.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PaymentRepository paymentRepository;
    private final TeacherEarningsRepository earningsRepository;
    private final PayoutRepository payoutRepository;

    // ✅ EXISTING METHOD (UNCHANGED)
    public FinancialSummaryDto getFinancialSummary() {

        List<Payment> payments = paymentRepository.findAll();
        List<TeacherEarnings> earnings = earningsRepository.findAll(); // kept (no removal)
        List<Payout> payouts = payoutRepository.findAll();

        BigDecimal totalRevenue = payments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGatewayFees = payments.stream()
                .map(p -> p.getGatewayFee() != null ? p.getGatewayFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPlatformFees = payments.stream()
                .map(p -> p.getPlatformFee() != null ? p.getPlatformFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTeacherPayouts = payouts.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinancialSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .totalGatewayFees(totalGatewayFees)
                .totalPlatformFees(totalPlatformFees)
                .totalTeacherPayouts(totalTeacherPayouts)
                .build();
    }

    // ================= NEW METHODS =================

    // ✅ FILTERED SUMMARY (DATE RANGE)
    public FinancialSummaryDto getFinancialSummary(LocalDate startDate, LocalDate endDate) {

        List<Payment> payments = paymentRepository.findAll();
        List<Payout> payouts = payoutRepository.findAll();

        if (startDate != null && endDate != null) {
            payments = payments.stream()
                    .filter(p -> isWithinRange(p.getCreatedAt(), startDate, endDate))
                    .toList();
        }

        BigDecimal totalRevenue = payments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGatewayFees = payments.stream()
                .map(p -> p.getGatewayFee() != null ? p.getGatewayFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPlatformFees = payments.stream()
                .map(p -> p.getPlatformFee() != null ? p.getPlatformFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTeacherPayouts = payouts.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinancialSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .totalGatewayFees(totalGatewayFees)
                .totalPlatformFees(totalPlatformFees)
                .totalTeacherPayouts(totalTeacherPayouts)
                .build();
    }

    // ✅ TEACHER SUMMARY
    public FinancialSummaryDto getTeacherSummary(String teacherId, LocalDate startDate, LocalDate endDate) {

        List<TeacherEarnings> earnings = earningsRepository.findByTeacherId(teacherId);

        if (startDate != null && endDate != null) {
            earnings = earnings.stream()
                    .filter(e -> isWithinRange(e.getCreatedAt(), startDate, endDate))
                    .toList();
        }

        BigDecimal totalEarnings = earnings.stream()
                .map(e -> e.getTeacherEarning() != null ? e.getTeacherEarning() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGatewayFees = earnings.stream()
                .map(e -> e.getGatewayFee() != null ? e.getGatewayFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPlatformFees = earnings.stream()
                .map(e -> e.getPlatformFee() != null ? e.getPlatformFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinancialSummaryDto.builder()
                .totalRevenue(totalEarnings)
                .totalGatewayFees(totalGatewayFees)
                .totalPlatformFees(totalPlatformFees)
                .totalTeacherPayouts(totalEarnings)
                .build();
    }

    // ================= HELPER =================

    private boolean isWithinRange(LocalDateTime dateTime, LocalDate start, LocalDate end) {
        if (dateTime == null) return false;

        LocalDate date = dateTime.toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }
}