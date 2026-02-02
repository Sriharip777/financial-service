package com.tcon.financial_service.refund.entity;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refunds")
public class Refund {

    @Id
    private String id;

    @Indexed
    private String paymentId;

    @Indexed
    private String bookingId;

    private String studentId;
    private String teacherId;

    private BigDecimal originalAmount;
    private BigDecimal refundAmount;
    private BigDecimal processingFee;
    private String currency;

    private RefundStatus status;

    private String reason;
    private String initiatedBy;  // STUDENT, TEACHER, ADMIN, SYSTEM

    // Gateway information
    private String gatewayRefundId;

    // Policy compliance
    private Integer hoursBeforeClass;
    private Boolean isFullRefund;

    private String failureReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime processedAt;
}

