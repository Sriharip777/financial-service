package com.tcon.financial_service.payment.gateway;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.tcon.financial_service.payment.dto.PaymentRequest;
import com.tcon.financial_service.payment.dto.PaymentResponse;
import com.tcon.financial_service.payment.entity.PaymentGateway;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGatewayInterface {

    private final RequestOptions stripeRequestOptions;

    @Value("${payment.stripe.fee-percentage:2.9}")
    private BigDecimal feePercentage;

    @Value("${payment.stripe.fixed-fee:0.30}")
    private BigDecimal fixedFee;

    @Value("${payment.stripe.webhook-secret}")
    private String webhookSecret;

    @Override
    public PaymentResponse createPaymentIntent(PaymentRequest request) throws StripeException {
        log.info("Creating Stripe Payment Intent for booking: {}", request.getBookingId());

        try {
            // Convert amount to smallest currency unit
            long amountInSmallestUnit = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();

            // Build metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("bookingId", request.getBookingId());
            metadata.put("studentId", request.getStudentId());
            metadata.put("teacherId", request.getTeacherId());

            if (request.getCourseId() != null && !request.getCourseId().isEmpty()) {
                metadata.put("courseId", request.getCourseId());
            }

            if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
                metadata.putAll(request.getMetadata());
            }

            // Build payment intent parameters
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInSmallestUnit)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setDescription(request.getDescription() != null ?
                            request.getDescription() : "Payment for booking " + request.getBookingId())
                    .putAllMetadata(metadata)
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC);

            // Add receipt email if provided
            if (request.getReceiptEmail() != null && !request.getReceiptEmail().isEmpty()) {
                paramsBuilder.setReceiptEmail(request.getReceiptEmail());
            }

            // ✅ Only add card payment method (UPI not supported)
            paramsBuilder.addPaymentMethodType("card");

            PaymentIntentCreateParams params = paramsBuilder.build();
            PaymentIntent paymentIntent = PaymentIntent.create(params, stripeRequestOptions);

            log.info("Stripe Payment Intent created successfully: {}", paymentIntent.getId());

            BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

// ✅ Stripe fee calculation
            BigDecimal percentageFee = amount.multiply(feePercentage)
                    .divide(BigDecimal.valueOf(100));

            BigDecimal gatewayFee = percentageFee
                    .add(fixedFee)
                    .setScale(2, RoundingMode.HALF_UP);

            // ✅ Use PaymentIntent ID as orderId instead of bookingId
            return PaymentResponse.builder()
                    .gatewayPaymentId(paymentIntent.getId())
                    .orderId(paymentIntent.getId())  // ✅ Use unique Stripe PI ID
                    .bookingId(request.getBookingId())
                    .studentId(request.getStudentId())
                    .teacherId(request.getTeacherId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency().toUpperCase())
                    .status(mapStripeStatus(paymentIntent.getStatus()))
                    .gateway(PaymentGateway.STRIPE)
                    .clientSecret(paymentIntent.getClientSecret())
                    .nextAction(paymentIntent.getNextAction() != null ?
                            paymentIntent.getNextAction().getType() : null)
                    .description(request.getDescription())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe Payment Intent creation failed for booking: {}",
                    request.getBookingId(), e);
            throw e;
        }
    }


    @Override
    public PaymentResponse capturePayment(String paymentIntentId) throws StripeException {
        log.info("Capturing Stripe Payment: {}", paymentIntentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId, stripeRequestOptions);

            if ("requires_capture".equals(paymentIntent.getStatus())) {
                PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder().build();
                paymentIntent = paymentIntent.capture(params, stripeRequestOptions);
            }

            // ✅ Convert amount
            BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // ✅ Stripe fee calculation (2.9% + 0.30)
            BigDecimal percentageFee = amount.multiply(feePercentage)
                    .divide(BigDecimal.valueOf(100));

            BigDecimal gatewayFee = percentageFee
                    .add(fixedFee)
                    .setScale(2, RoundingMode.HALF_UP);

            return PaymentResponse.builder()
                    .gatewayPaymentId(paymentIntent.getId())
                    .amount(amount)
                    .currency(paymentIntent.getCurrency().toUpperCase())
                    .status(mapStripeStatus(paymentIntent.getStatus()))
                    .gateway(PaymentGateway.STRIPE)
                    .gatewayFee(gatewayFee)                 // ✅ CORRECT PLACE
                    .gatewayFeePercentage(feePercentage)
                    .build();


        } catch (StripeException e) {
            log.error("Payment capture failed for: {}", paymentIntentId, e);
            throw e;
        }
    }

    @Override
    public String refundPayment(String paymentIntentId, BigDecimal amount) throws StripeException {
        log.info("Processing Stripe refund for Payment: {}, Amount: {}", paymentIntentId, amount);

        try {
            // Convert amount to smallest unit
            long refundAmount = amount
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();

            // Build refund parameters
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(refundAmount)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();

            // Create refund
            Refund refund = Refund.create(params, stripeRequestOptions);

            log.info("Stripe refund created successfully: {}", refund.getId());
            return refund.getId();

        } catch (StripeException e) {
            log.error("Refund creation failed for: {}", paymentIntentId, e);
            throw e;
        }
    }

    @Override
    public PaymentResponse getPaymentStatus(String paymentIntentId) throws StripeException {
        log.info("Fetching Stripe Payment status: {}", paymentIntentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId, stripeRequestOptions);

            return PaymentResponse.builder()
                    .gatewayPaymentId(paymentIntent.getId())
                    .amount(BigDecimal.valueOf(paymentIntent.getAmount())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .currency(paymentIntent.getCurrency().toUpperCase())
                    .status(mapStripeStatus(paymentIntent.getStatus()))
                    .gateway(PaymentGateway.STRIPE)
                    .build();

        } catch (StripeException e) {
            log.error("Fetch payment status failed for: {}", paymentIntentId, e);
            throw e;
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) throws Exception {
        log.debug("Verifying Stripe webhook signature");

        try {
            // Verify webhook signature
            Webhook.constructEvent(payload, signature, webhookSecret);
            log.debug("Stripe webhook signature verified successfully");
            return true;

        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage());
            throw e;
        }
    }



    /**
     * Map Stripe payment status to internal PaymentStatus enum
     */
    private PaymentStatus mapStripeStatus(String stripeStatus) {
        if (stripeStatus == null) {
            log.warn("Stripe status is null, defaulting to FAILED");
            return PaymentStatus.FAILED;
        }

        return switch (stripeStatus.toLowerCase()) {
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "processing" -> PaymentStatus.PROCESSING;
            case "requires_payment_method", "requires_confirmation", "requires_action" ->
                    PaymentStatus.PENDING;
            case "canceled" -> PaymentStatus.CANCELLED;
            case "requires_capture" -> PaymentStatus.PROCESSING;
            default -> {
                log.warn("Unknown Stripe status: {}, defaulting to FAILED", stripeStatus);
                yield PaymentStatus.FAILED;
            }
        };
    }
}