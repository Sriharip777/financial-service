package com.tcon.financial_service.payment.controller;

import com.tcon.financial_service.payment.dto.PaymentDto;
import com.tcon.financial_service.payment.dto.PaymentRequest;
import com.tcon.financial_service.payment.dto.PaymentResponse;
import com.tcon.financial_service.payment.entity.PaymentGateway;
import com.tcon.financial_service.payment.gateway.PaymentGatewayFactory;
import com.tcon.financial_service.payment.gateway.PaymentGatewayInterface;
import com.tcon.financial_service.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentGatewayFactory gatewayFactory;

    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            log.info("=== CREATE PAYMENT REQUEST ===");
            log.info("Booking: {}, Student: {}, Teacher: {}",
                    request.getBookingId(), request.getStudentId(), request.getTeacherId());
            log.info("Amount: {} {}, Gateway: {}",
                    request.getAmount(), request.getCurrency(), request.getGateway());

            PaymentResponse response = paymentService.createPayment(request);

            log.info("=== PAYMENT CREATED SUCCESSFULLY ===");
            log.info("Payment ID: {}, Order ID: {}", response.getPaymentId(), response.getOrderId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException e) {
            log.error("❌ Payment creation failed - IllegalState: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Payment Creation Failed");
            error.put("message", e.getMessage());
            error.put("status", 400);
            error.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.badRequest().body(error);

        } catch (IllegalArgumentException e) {
            log.error("❌ Payment creation failed - InvalidArgument: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid Request");
            error.put("message", e.getMessage());
            error.put("status", 400);
            error.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("❌ Payment creation failed - Unexpected Error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getSimpleName());
            error.put("status", 500);
            error.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable String paymentId) {
        try {
            PaymentDto payment = paymentService.getPayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            log.error("Payment not found: {}", paymentId);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not Found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Page<PaymentDto>> getStudentPayments(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PaymentDto> payments = paymentService.getPaymentsByStudent(studentId, pageable);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Page<PaymentDto>> getTeacherPayments(
            @PathVariable String teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PaymentDto> payments = paymentService.getPaymentsByTeacher(teacherId, pageable);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<?> confirmPayment(@PathVariable String paymentId) {
        try {
            log.info("Confirming payment: {}", paymentId);
            PaymentDto payment = paymentService.confirmPayment(paymentId);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            log.error("Payment not found: {}", paymentId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Payment confirmation failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Confirmation Failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<?> failPayment(
            @PathVariable String paymentId,
            @RequestParam String reason
    ) {
        try {
            log.info("Failing payment: {}, Reason: {}", paymentId, reason);
            PaymentDto payment = paymentService.failPayment(paymentId, reason);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            log.error("Payment not found: {}", paymentId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        try {
            log.info("Received Stripe webhook");
            PaymentGatewayInterface gateway = gatewayFactory.getGateway(PaymentGateway.STRIPE);
            boolean verified = gateway.verifyWebhookSignature(payload, signature);

            if (!verified) {
                log.error("Stripe webhook signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Stripe webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/webhook/razorpay")
    public ResponseEntity<Map<String, String>> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        try {
            log.info("Received Razorpay webhook");
            PaymentGatewayInterface gateway = gatewayFactory.getGateway(PaymentGateway.RAZORPAY);
            boolean verified = gateway.verifyWebhookSignature(payload, signature);

            if (!verified) {
                log.error("Razorpay webhook signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Razorpay webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "financial-service");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
