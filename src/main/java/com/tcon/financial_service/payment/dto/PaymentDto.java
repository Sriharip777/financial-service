package com.tcon.financial_service.payment.dto;

import com.tcon.financial_service.payment.entity.PaymentGateway;
import com.tcon.financial_service.payment.entity.PaymentMethod;
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
public class PaymentDto {

    private String id;
    private String orderId;
    private String bookingId;
    private String studentId;
    private String teacherId;

    private BigDecimal amount;
    private String currency;

    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private PaymentGateway gateway;

    private String gatewayPaymentId;

    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal teacherEarnings;

    private Boolean isInstallment;
    private Integer installmentNumber;
    private Integer totalInstallments;

    private String description;
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
