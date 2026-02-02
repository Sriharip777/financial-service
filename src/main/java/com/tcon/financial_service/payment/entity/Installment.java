package com.tcon.financial_service.payment.entity;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "installments")
public class Installment {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String studentId;

    @Indexed
    private String teacherId;

    private BigDecimal totalAmount;
    private String currency;

    private Integer totalInstallments;
    private BigDecimal installmentAmount;

    private LocalDate firstPaymentDate;
    private LocalDate nextPaymentDate;
    private PaymentFrequency paymentFrequency;

    private Integer paidInstallments;
    private Integer remainingInstallments;

    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    private Boolean isActive;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;
}
