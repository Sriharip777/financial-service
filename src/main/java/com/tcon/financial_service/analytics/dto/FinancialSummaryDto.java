package com.tcon.financial_service.analytics.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryDto {

    private BigDecimal totalRevenue;
    private BigDecimal totalGatewayFees;
    private BigDecimal totalPlatformFees;
    private BigDecimal totalTeacherPayouts;
}