package com.tcon.financial_service.refund.dto;

import com.tcon.financial_service.refund.entity.RefundStatus;
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
public class RefundDto {

    private String id;
    private String paymentId;
    private String bookingId;
    private String studentId;
    private String teacherId;

    private BigDecimal originalAmount;
    private BigDecimal refundAmount;
    private BigDecimal processingFee;
    private String currency;

    private RefundStatus status;

    private String reason;
    private String initiatedBy;

    private String gatewayRefundId;

    private Integer hoursBeforeClass;
    private Boolean isFullRefund;

    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}

