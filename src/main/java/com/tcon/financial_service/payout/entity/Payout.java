package com.tcon.financial_service.payout.entity;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payouts")
public class Payout {

    @Id
    private String id;

    @Indexed
    private String teacherId;

    private BigDecimal amount;
    private String currency;

    private PayoutStatus status;

    // ================= NEW FIELD (IMPORTANT) =================

    /**
     * List of earnings included in this payout
     */
    private List<String> earningIds;

    /**
     * Total number of earnings included
     */
    private Integer totalTransactions;

    // ========================================================

    // Bank details
    private String bankAccountNumber;
    private String bankIfscCode;
    private String accountHolderName;

    // Period covered
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // Transaction references
    private String transactionId;
    private String gatewayPayoutId;

    // Failure information
    private String failureReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime scheduledAt;
    private LocalDateTime processedAt;
}