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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "teacher_earnings")
public class TeacherEarnings {

    @Id
    private String id;

    @Indexed(unique = true)
    private String teacherId;

    private String currency;

    // Lifetime earnings
    private BigDecimal totalEarnings;
    private BigDecimal totalPaidOut;
    private BigDecimal pendingAmount;

    // Current period
    private BigDecimal currentPeriodEarnings;
    private LocalDateTime lastPayoutDate;
    private LocalDateTime nextPayoutDate;

    // Statistics
    private Integer totalPayments;
    private Integer totalPayouts;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

