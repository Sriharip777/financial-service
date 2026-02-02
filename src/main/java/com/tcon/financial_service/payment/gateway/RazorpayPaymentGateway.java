package com.tcon.financial_service.payment.gateway;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.tcon.financial_service.payment.dto.PaymentRequest;
import com.tcon.financial_service.payment.dto.PaymentResponse;
import com.tcon.financial_service.payment.entity.PaymentGateway;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayPaymentGateway implements PaymentGatewayInterface {

    private final RazorpayClient razorpayClient;

    @Value("${payment.razorpay.webhook-secret}")
    private String webhookSecret;

    @Value("${payment.razorpay.mock-mode:true}")
    private boolean mockMode;

    @Override
    public PaymentResponse createPaymentIntent(PaymentRequest request) throws RazorpayException {
        log.info("Creating Razorpay Order for booking: {}", request.getBookingId());

        if (mockMode) {
            return createMockOrder(request);
        }

        try {
            long amountInPaise = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", Long.valueOf(amountInPaise)); // ✅ Convert to Long
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", "rcpt_" + request.getBookingId());

            JSONObject notes = new JSONObject();
            notes.put("bookingId", request.getBookingId());
            notes.put("studentId", request.getStudentId());
            notes.put("teacherId", request.getTeacherId());
            if (request.getCourseId() != null) {
                notes.put("courseId", request.getCourseId());
            }
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            String orderId = order.get("id").toString();
            log.info("Razorpay Order created: {}", orderId);

            return PaymentResponse.builder()
                    .gatewayPaymentId(orderId)
                    .orderId(orderId)
                    .bookingId(request.getBookingId())
                    .studentId(request.getStudentId())
                    .teacherId(request.getTeacherId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .gateway(PaymentGateway.RAZORPAY)
                    .description(request.getDescription())
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed", e);
            throw e;
        }
    }

    @Override
    public PaymentResponse capturePayment(String paymentId) throws RazorpayException {
        log.info("Capturing Razorpay Payment: {}", paymentId);

        if (mockMode) {
            return mockCapturePayment(paymentId);
        }

        try {
            // Fetch payment details
            com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(paymentId);

            String status = razorpayPayment.get("status").toString();

            // Razorpay payments are auto-captured by default
            // Manual capture is only needed if you've disabled auto-capture
            if ("authorized".equals(status)) {
                log.info("Payment is authorized but not captured. Attempting capture...");

                // Get amount as Integer
                Integer amountInPaise = Integer.valueOf(razorpayPayment.get("amount").toString());

                // Create capture request as JSONObject
                JSONObject captureRequest = new JSONObject();
                captureRequest.put("amount", amountInPaise);
                captureRequest.put("currency", razorpayPayment.get("currency").toString());

                // Use Razorpay client to capture with JSONObject
                razorpayPayment = razorpayClient.payments.capture(paymentId, captureRequest);

                log.info("Payment captured successfully: {}", paymentId);
            }

            return PaymentResponse.builder()
                    .gatewayPaymentId(razorpayPayment.get("id").toString())
                    .amount(new BigDecimal(razorpayPayment.get("amount").toString())
                            .divide(BigDecimal.valueOf(100)))
                    .currency(razorpayPayment.get("currency").toString())
                    .status(mapRazorpayStatus(razorpayPayment.get("status")))
                    .gateway(PaymentGateway.RAZORPAY)
                    .build();

        } catch (RazorpayException e) {
            log.error("Payment capture failed", e);
            throw e;
        }
    }

    @Override
    public String refundPayment(String paymentId, BigDecimal amount) throws RazorpayException {
        log.info("Processing Razorpay refund for Payment: {}, Amount: {}", paymentId, amount);

        if (mockMode) {
            return mockRefundPayment(paymentId, amount);
        }

        try {
            long refundAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", Long.valueOf(refundAmount)); // ✅ Convert to Long
            refundRequest.put("speed", "normal"); // normal or optimum

            // Create refund using Razorpay client
            com.razorpay.Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);

            String refundId = refund.get("id").toString();
            log.info("Razorpay refund created: {}", refundId);

            return refundId;

        } catch (RazorpayException e) {
            log.error("Refund creation failed", e);
            throw e;
        }
    }

    @Override
    public PaymentResponse getPaymentStatus(String paymentId) throws RazorpayException {
        log.info("Fetching Razorpay Payment status: {}", paymentId);

        if (mockMode) {
            return mockGetPaymentStatus(paymentId);
        }

        try {
            com.razorpay.Payment razorpayPayment = razorpayClient.payments.fetch(paymentId);

            return PaymentResponse.builder()
                    .gatewayPaymentId(razorpayPayment.get("id").toString())
                    .amount(new BigDecimal(razorpayPayment.get("amount").toString())
                            .divide(BigDecimal.valueOf(100)))
                    .currency(razorpayPayment.get("currency").toString())
                    .status(mapRazorpayStatus(razorpayPayment.get("status")))
                    .gateway(PaymentGateway.RAZORPAY)
                    .build();

        } catch (RazorpayException e) {
            log.error("Fetch payment status failed", e);
            throw e;
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) throws Exception {
        log.debug("Verifying Razorpay webhook signature");
        try {
            return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (Exception e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    private PaymentStatus mapRazorpayStatus(Object status) {
        if (status == null) {
            return PaymentStatus.PENDING;
        }

        String statusStr = status.toString().toLowerCase();
        return switch (statusStr) {
            case "captured", "authorized" -> PaymentStatus.COMPLETED;
            case "created" -> PaymentStatus.PENDING;
            case "failed" -> PaymentStatus.FAILED;
            case "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }

    // ========== Mock Methods for Testing ==========

    private PaymentResponse createMockOrder(PaymentRequest request) {
        log.info("MOCK MODE: Creating mock Razorpay order");

        String mockOrderId = "order_mock_" + System.currentTimeMillis();

        return PaymentResponse.builder()
                .gatewayPaymentId(mockOrderId)
                .orderId(mockOrderId)
                .bookingId(request.getBookingId())
                .studentId(request.getStudentId())
                .teacherId(request.getTeacherId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .gateway(PaymentGateway.RAZORPAY)
                .description(request.getDescription())
                .build();
    }

    private PaymentResponse mockCapturePayment(String paymentId) {
        log.info("MOCK MODE: Capturing mock payment: {}", paymentId);

        return PaymentResponse.builder()
                .gatewayPaymentId(paymentId)
                .amount(BigDecimal.valueOf(1000.00))
                .currency("INR")
                .status(PaymentStatus.COMPLETED)
                .gateway(PaymentGateway.RAZORPAY)
                .build();
    }

    private String mockRefundPayment(String paymentId, BigDecimal amount) {
        log.info("MOCK MODE: Creating mock refund - paymentId: {}, amount: {}",
                paymentId, amount);

        String mockRefundId = "rfnd_mock_" + System.currentTimeMillis();
        log.info("MOCK: Refund created: {}", mockRefundId);

        return mockRefundId;
    }

    private PaymentResponse mockGetPaymentStatus(String paymentId) {
        log.info("MOCK MODE: Fetching mock payment status: {}", paymentId);

        return PaymentResponse.builder()
                .gatewayPaymentId(paymentId)
                .amount(BigDecimal.valueOf(1000.00))
                .currency("INR")
                .status(PaymentStatus.COMPLETED)
                .gateway(PaymentGateway.RAZORPAY)
                .build();
    }
}
