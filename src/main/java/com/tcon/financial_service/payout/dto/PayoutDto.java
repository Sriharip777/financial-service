package com.tcon.financial_service.payout.dto;

import com.tcon.financial_service.payout.entity.PayoutStatus; // ✅ Correct import
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutDto {

    private String id;
    private String teacherId;

    private BigDecimal amount;
    private String currency;

    private PayoutStatus status; // ✅ This should use payout.entity.PayoutStatus

    private String bankAccountNumber;
    private String bankIfscCode;
    private String accountHolderName;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private String transactionId;
    private String gatewayPayoutId;

    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime processedAt;
}
