package com.tcon.financial_service.payment.dto;

import com.tcon.financial_service.payment.entity.PaymentFrequency;
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
public class InstallmentDto {

    private String id;
    private String bookingId;
    private String studentId;
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
    private LocalDateTime createdAt;
}
