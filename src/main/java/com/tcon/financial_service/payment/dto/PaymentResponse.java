package com.tcon.financial_service.payment.dto;

import com.tcon.financial_service.payment.entity.PaymentGateway;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private String bookingId;
    private String studentId;
    private String teacherId;

    private BigDecimal amount; // Total paid
    private String currency;

    private PaymentStatus status;
    private PaymentGateway gateway;

    private String gatewayPaymentId;
    private String clientSecret;
    private String checkoutUrl;
    private String nextAction;

    // ================= EXISTING =================
    private BigDecimal commissionAmount;   // (platform fee - backward compatibility)
    private BigDecimal teacherEarnings;

    // ================= NEW FIELDS =================

    /**
     * Payment gateway deduction (Stripe/Razorpay)
     */
    private BigDecimal gatewayFee;

    /**
     * Gateway fee percentage used
     */
    private BigDecimal gatewayFeePercentage;

    /**
     * Net amount after gateway deduction
     * netAmount = amount - gatewayFee
     */
    private BigDecimal netAmount;

    /**
     * Platform fee (same as commissionAmount but explicit)
     */
    private BigDecimal platformFee;

    /**
     * Platform fee percentage
     */
    private BigDecimal platformFeePercentage;

    private String description;
    private LocalDateTime createdAt;
}