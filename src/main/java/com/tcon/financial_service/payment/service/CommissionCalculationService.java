package com.tcon.financial_service.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class CommissionCalculationService {

    // ================= OLD (KEEP) =================
    @Value("${commission.one-time:0.20}")
    private BigDecimal oneTimeRate;

    @Value("${commission.recurring:0.10}")
    private BigDecimal recurringRate;

    // ================= NEW (PLATFORM FEE) =================
    @Value("${platform.fee.default-rate:0.25}")
    private BigDecimal defaultPlatformRate;

    @Value("${platform.fee.negotiated-rate:0.20}")
    private BigDecimal negotiatedPlatformRate;

    /**
     * OLD: Commission (kept for backward compatibility)
     */
    public BigDecimal getCommissionRate(boolean isRecurring) {
        return isRecurring ? recurringRate : oneTimeRate;
    }

    public BigDecimal calculateCommission(BigDecimal amount, boolean isRecurring) {
        BigDecimal rate = getCommissionRate(isRecurring);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * ================= NEW LOGIC =================
     */

    public BigDecimal getPlatformFeeRate(Boolean isNegotiated) {
        return Boolean.TRUE.equals(isNegotiated)
                ? negotiatedPlatformRate   // 20%
                : defaultPlatformRate;     // 25%
    }

    public BigDecimal calculatePlatformFee(BigDecimal amount, Boolean isNegotiated) {
        BigDecimal rate = getPlatformFeeRate(isNegotiated);

        BigDecimal fee = amount.multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("Platform fee calculated: Amount={}, Rate={}, Fee={}",
                amount, rate, fee);

        return fee;
    }

    public BigDecimal calculateTeacherEarnings(BigDecimal amount, Boolean isNegotiated) {
        BigDecimal platformFee = calculatePlatformFee(amount, isNegotiated);

        BigDecimal earnings = amount.subtract(platformFee)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("Teacher earnings calculated: Amount={}, PlatformFee={}, Earnings={}",
                amount, platformFee, earnings);

        return earnings;
    }

    /**
     * Getters
     */
    public BigDecimal getOneTimeRate() {
        return oneTimeRate;
    }

    public BigDecimal getRecurringRate() {
        return recurringRate;
    }
}