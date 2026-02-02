package com.tcon.financial_service.payout.service;


import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import com.tcon.financial_service.payment.repository.PaymentRepository;
import com.tcon.financial_service.payout.dto.EarningsDto;
import com.tcon.financial_service.payout.entity.Payout;
import com.tcon.financial_service.payout.entity.TeacherEarnings;
import com.tcon.financial_service.payout.repository.TeacherEarningsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EarningsCalculationService {

    private final TeacherEarningsRepository earningsRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void updateEarningsAfterPayment(Payment payment) {
        log.info("Updating earnings for teacher: {}", payment.getTeacherId());

        TeacherEarnings earnings = earningsRepository.findByTeacherId(payment.getTeacherId())
                .orElse(createNewEarnings(payment.getTeacherId(), payment.getCurrency()));

        // Update earnings
        earnings.setTotalEarnings(earnings.getTotalEarnings().add(payment.getTeacherEarnings()));
        earnings.setPendingAmount(earnings.getPendingAmount().add(payment.getTeacherEarnings()));
        earnings.setCurrentPeriodEarnings(
                earnings.getCurrentPeriodEarnings().add(payment.getTeacherEarnings())
        );
        earnings.setTotalPayments(earnings.getTotalPayments() + 1);

        earningsRepository.save(earnings);

        log.info("Earnings updated for teacher: {}", payment.getTeacherId());
    }

    @Transactional
    public void processPayout(Payout payout) {
        log.info("Processing payout for earnings - Teacher: {}", payout.getTeacherId());

        TeacherEarnings earnings = earningsRepository.findByTeacherId(payout.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Earnings not found for teacher: " + payout.getTeacherId()));

        // Update earnings after payout
        earnings.setTotalPaidOut(earnings.getTotalPaidOut().add(payout.getAmount()));
        earnings.setPendingAmount(earnings.getPendingAmount().subtract(payout.getAmount()));
        earnings.setCurrentPeriodEarnings(BigDecimal.ZERO);
        earnings.setLastPayoutDate(LocalDateTime.now());
        earnings.setTotalPayouts(earnings.getTotalPayouts() + 1);

        earningsRepository.save(earnings);

        log.info("Payout processed for earnings - Teacher: {}", payout.getTeacherId());
    }

    public EarningsDto getTeacherEarnings(String teacherId) {
        TeacherEarnings earnings = earningsRepository.findByTeacherId(teacherId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Earnings not found for teacher: " + teacherId));

        return mapToDto(earnings);
    }

    public BigDecimal getPendingAmount(String teacherId) {
        TeacherEarnings earnings = earningsRepository.findByTeacherId(teacherId)
                .orElse(null);

        return earnings != null ? earnings.getPendingAmount() : BigDecimal.ZERO;
    }

    public BigDecimal calculatePeriodEarnings(String teacherId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = paymentRepository.findCompletedPaymentsByTeacherAndDateRange(
                teacherId, startDate, endDate
        );

        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getTeacherEarnings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TeacherEarnings createNewEarnings(String teacherId, String currency) {
        return TeacherEarnings.builder()
                .teacherId(teacherId)
                .currency(currency)
                .totalEarnings(BigDecimal.ZERO)
                .totalPaidOut(BigDecimal.ZERO)
                .pendingAmount(BigDecimal.ZERO)
                .currentPeriodEarnings(BigDecimal.ZERO)
                .totalPayments(0)
                .totalPayouts(0)
                .build();
    }

    private EarningsDto mapToDto(TeacherEarnings earnings) {
        return EarningsDto.builder()
                .teacherId(earnings.getTeacherId())
                .currency(earnings.getCurrency())
                .totalEarnings(earnings.getTotalEarnings())
                .totalPaidOut(earnings.getTotalPaidOut())
                .pendingAmount(earnings.getPendingAmount())
                .currentPeriodEarnings(earnings.getCurrentPeriodEarnings())
                .lastPayoutDate(earnings.getLastPayoutDate())
                .nextPayoutDate(earnings.getNextPayoutDate())
                .totalPayments(earnings.getTotalPayments())
                .totalPayouts(earnings.getTotalPayouts())
                .build();
    }
}
