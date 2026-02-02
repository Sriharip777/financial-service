package com.tcon.financial_service.payout.dto;

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
public class EarningsDto {

    private String teacherId;
    private String currency;

    private BigDecimal totalEarnings;
    private BigDecimal totalPaidOut;
    private BigDecimal pendingAmount;

    private BigDecimal currentPeriodEarnings;

    private LocalDateTime lastPayoutDate;
    private LocalDateTime nextPayoutDate;

    private Integer totalPayments;
    private Integer totalPayouts;
}
