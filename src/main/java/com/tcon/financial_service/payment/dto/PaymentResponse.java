package com.tcon.financial_service.payment.dto;

import com.tcon.financial_service.payment.entity.PaymentGateway; // ✅ Correct
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

    private BigDecimal amount;
    private String currency;

    private PaymentStatus status;
    private PaymentGateway gateway;
    private String gatewayPaymentId;
    private String clientSecret;
    private String checkoutUrl;
    private String nextAction;

    private BigDecimal commissionAmount;
    private BigDecimal teacherEarnings;

    private String description;
    private LocalDateTime createdAt;
}
