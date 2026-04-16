package com.tcon.financial_service.earnings.service;

import com.tcon.financial_service.earnings.entity.TeacherEarnings;
import com.tcon.financial_service.earnings.repository.TeacherEarningsRepository;
import com.tcon.financial_service.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherEarningsService {

    private final TeacherEarningsRepository repository;

    public void recordEarning(Payment payment) {

        // ✅ Safety check
        if (payment == null) {
            log.error("❌ Payment is null. Cannot record earnings.");
            return;
        }

        if (payment.getId() == null) {
            log.error("❌ Payment ID is null. Cannot record earnings.");
            return;
        }

        // ✅ Prevent duplicate entries (VERY IMPORTANT)
        boolean alreadyExists = repository.existsByPaymentId(payment.getId());
        if (alreadyExists) {
            log.warn("⚠️ Earnings already recorded for payment: {}", payment.getId());
            return;
        }

        // ✅ Null-safe values
        BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
        BigDecimal gatewayFee = payment.getGatewayFee() != null ? payment.getGatewayFee() : BigDecimal.ZERO;
        BigDecimal platformFee = payment.getPlatformFee() != null ? payment.getPlatformFee() : BigDecimal.ZERO;
        BigDecimal netAmount = payment.getNetAmount() != null ? payment.getNetAmount() : BigDecimal.ZERO;
        BigDecimal teacherEarning = payment.getTeacherEarnings() != null ? payment.getTeacherEarnings() : BigDecimal.ZERO;

        // ✅ Build earnings record
        TeacherEarnings earnings = TeacherEarnings.builder()
                .paymentId(payment.getId())
                .teacherId(payment.getTeacherId())
                .studentId(payment.getStudentId())
                .bookingId(payment.getBookingId())
                .courseId(payment.getCourseId())
                .totalAmount(amount)
                .gatewayFee(gatewayFee)
                .platformFee(platformFee)
                .netAmount(netAmount)
                .teacherEarning(teacherEarning)
                .isPaidOut(false)
                .createdAt(LocalDateTime.now())
                .build();

        // ✅ Save to DB
        repository.save(earnings);

        log.info("✅ Teacher earning recorded for payment: {}", payment.getId());
    }
}