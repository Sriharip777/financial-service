package com.tcon.financial_service.payment.dto;
import com.tcon.financial_service.payment.entity.PaymentFrequency;
import com.tcon.financial_service.payment.entity.PaymentGateway;
import com.tcon.financial_service.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private String bookingId;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Teacher ID is required")
    private String teacherId;

    private String courseId; // Optional: for recurring courses

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Payment gateway is required")
    private PaymentGateway gateway;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String description;
    private String receiptEmail;

    // Installment fields
    private Boolean isInstallment;
    private Integer totalInstallments;
    private PaymentFrequency paymentFrequency; // ✅ Added this field

    private Map<String, String> metadata;

    /**
     * Indicates if platform fee is negotiated (admin approved)
     */
    private Boolean isNegotiated;
}