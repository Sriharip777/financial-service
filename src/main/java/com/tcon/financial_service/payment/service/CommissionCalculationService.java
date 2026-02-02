package com.tcon.financial_service.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class CommissionCalculationService {

    @Value("${commission.one-time:0.20}")  // ✅ Changed from non-recurring
    private BigDecimal oneTimeRate;

    @Value("${commission.recurring:0.10}")
    private BigDecimal recurringRate;

    /**
     * Get commission rate based on booking type
     */
    public BigDecimal getCommissionRate(boolean isRecurring) {
        return isRecurring ? recurringRate : oneTimeRate;
    }

    /**
     * Calculate commission amount
     */
    public BigDecimal calculateCommission(BigDecimal amount, boolean isRecurring) {
        BigDecimal rate = getCommissionRate(isRecurring);
        BigDecimal commission = amount.multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("Commission calculated: Amount={}, Rate={}, Commission={}",
                amount, rate, commission);

        return commission;
    }

    /**
     * Calculate teacher earnings (amount after commission)
     */
    public BigDecimal calculateTeacherEarnings(BigDecimal amount, boolean isRecurring) {
        BigDecimal commission = calculateCommission(amount, isRecurring);
        BigDecimal earnings = amount.subtract(commission)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("Teacher earnings calculated: Amount={}, Commission={}, Earnings={}",
                amount, commission, earnings);

        return earnings;
    }

    /**
     * Get one-time commission rate
     */
    public BigDecimal getOneTimeRate() {
        return oneTimeRate;
    }

    /**
     * Get recurring commission rate
     */
    public BigDecimal getRecurringRate() {
        return recurringRate;
    }
}
