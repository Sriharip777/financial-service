package com.tcon.financial_service.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookDto {

    private String eventType;
    private String paymentId;
    private String status;
    private Map<String, Object> data;
    private String signature;
}

