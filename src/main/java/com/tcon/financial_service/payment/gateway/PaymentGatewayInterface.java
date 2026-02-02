package com.tcon.financial_service.payment.gateway;



import com.tcon.financial_service.payment.dto.PaymentRequest;
import com.tcon.financial_service.payment.dto.PaymentResponse;

import java.math.BigDecimal;

public interface PaymentGatewayInterface {

    PaymentResponse createPaymentIntent(PaymentRequest request) throws Exception;

    PaymentResponse capturePayment(String paymentId) throws Exception;

    String refundPayment(String paymentId, BigDecimal amount) throws Exception;

    PaymentResponse getPaymentStatus(String paymentId) throws Exception;

    boolean verifyWebhookSignature(String payload, String signature) throws Exception;
}
