package com.tcon.financial_service.earnings.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TeacherEarningsResponseDto {

    private String paymentId;
    private String teacherId;
    private String studentId;
    private String bookingId;
    private String courseId;

    private BigDecimal totalAmount;
    private BigDecimal gatewayFee;
    private BigDecimal platformFee;
    private BigDecimal netAmount;
    private BigDecimal teacherEarning;

    private Boolean isPaidOut;

    private LocalDateTime createdAt;
}