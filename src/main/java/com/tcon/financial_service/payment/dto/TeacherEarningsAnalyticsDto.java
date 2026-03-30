package com.tcon.financial_service.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEarningsAnalyticsDto {
    private String teacherId;
    private Double earnings;
}