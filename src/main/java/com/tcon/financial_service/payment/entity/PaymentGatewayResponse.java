package com.tcon.financial_service.payment.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payment_gateway_responses")
public class PaymentGatewayResponse {

    @Id
    private String id;

    @Indexed
    private String paymentId;

    private PaymentGateway gateway;
    private String requestType;  // CREATE, CAPTURE, REFUND, etc.

    private String rawRequest;
    private String rawResponse;

    private Integer statusCode;
    private Boolean success;

    private Map<String, Object> responseData;

    private LocalDateTime timestamp;
}