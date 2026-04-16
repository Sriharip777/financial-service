package com.tcon.financial_service.earnings.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "teacher_earnings")
public class TeacherEarnings {

    @Id
    private String id;

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