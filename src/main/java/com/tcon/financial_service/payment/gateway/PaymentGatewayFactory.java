package com.tcon.financial_service.payment.gateway;

import com.tcon.financial_service.payment.entity.PaymentGateway; // ✅ Correct package
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final StripePaymentGateway stripePaymentGateway;
    private final RazorpayPaymentGateway razorpayPaymentGateway;

    public PaymentGatewayInterface getGateway(PaymentGateway gateway) {
        return switch (gateway) {
            case STRIPE -> stripePaymentGateway;
            case RAZORPAY -> razorpayPaymentGateway;
        };
    }
}