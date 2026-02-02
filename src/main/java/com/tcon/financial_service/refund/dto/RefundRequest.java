package com.tcon.financial_service.refund.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Initiated by is required")
    private String initiatedBy;  // STUDENT, TEACHER, ADMIN, SYSTEM

    private Integer hoursBeforeClass;
}

