package com.tcon.financial_service.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderId;

    @Indexed
    private String bookingId;

    @Indexed
    private String studentId;

    @Indexed
    private String teacherId;

    private String courseId;

    // ================= EXISTING =================
    private BigDecimal amount;              // Total amount paid by user
    private String currency;

    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private PaymentGateway gateway;

    @Indexed
    private String gatewayPaymentId;

    @Indexed
    private String parentPaymentId;

    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;   // (kept for backward compatibility)

    private BigDecimal teacherEarnings;

    private Boolean isInstallment;
    private Integer installmentNumber;
    private Integer totalInstallments;

    private String description;
    private String receiptEmail;
    private String failureReason;

    private Map<String, String> metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    // ================= NEW FIELDS (ADDED) =================

    /**
     * Payment Gateway Fee (Stripe/Razorpay deduction)
     */
    private BigDecimal gatewayFee;

    /**
     * Gateway fee percentage used for this transaction
     */
    private BigDecimal gatewayFeePercentage;

    /**
     * Net amount after gateway deduction
     * netAmount = amount - gatewayFee
     */
    private BigDecimal netAmount;

    /**
     * Platform fee (same as commissionAmount but explicitly named for clarity)
     */
    private BigDecimal platformFee;

    /**
     * Platform fee percentage used
     */
    private BigDecimal platformFeePercentage;

    // ================= NEGOTIATION FIELD =================

    /**
     * Indicates if platform fee was negotiated (admin approved)
     */
    private Boolean isNegotiated;
}