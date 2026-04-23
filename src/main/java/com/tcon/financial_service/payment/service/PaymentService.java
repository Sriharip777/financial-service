package com.tcon.financial_service.payment.service;

import com.tcon.financial_service.event.PaymentEventPublisher;
import com.tcon.financial_service.payment.dto.PaymentDto;
import com.tcon.financial_service.payment.dto.PaymentRequest;
import com.tcon.financial_service.payment.dto.PaymentResponse;
import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payment.entity.PaymentGateway;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import com.tcon.financial_service.payment.gateway.PaymentGatewayFactory;
import com.tcon.financial_service.payment.gateway.PaymentGatewayInterface;
import com.tcon.financial_service.payment.repository.PaymentRepository;
import com.tcon.financial_service.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String DEFAULT_CURRENCY = "INR";

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final CommissionCalculationService commissionService;
    private final InstallmentService installmentService;
    private final TransactionService transactionService;
    private final PaymentEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) throws Exception {
        log.info("==================== CREATE PAYMENT START ====================");

        validatePaymentRequest(request);

        PaymentGatewayInterface gateway = gatewayFactory.getGateway(request.getGateway());
        if (gateway == null) {
            throw new IllegalStateException("Payment gateway not found: " + request.getGateway());
        }
        log.info("✅ Payment gateway loaded: {}", request.getGateway());

        boolean isRecurring = request.getCourseId() != null && Boolean.TRUE.equals(request.getIsInstallment());

        BigDecimal commissionRate = commissionService.getCommissionRate(isRecurring);
        BigDecimal commissionAmount = commissionService.calculateCommission(request.getAmount(), isRecurring);
        BigDecimal teacherEarnings = commissionService.calculateTeacherEarnings(request.getAmount(), isRecurring);

        log.info("✅ Commission calculated - Rate: {}, Amount: {}, Teacher Earnings: {}",
                commissionRate, commissionAmount, teacherEarnings);

        PaymentResponse gatewayResponse = gateway.createPaymentIntent(request);
        if (gatewayResponse == null) {
            throw new IllegalStateException("Payment gateway returned null response");
        }
        if (!hasText(gatewayResponse.getOrderId())) {
            throw new IllegalStateException("Payment gateway did not return orderId");
        }

        log.info("✅ Payment intent created - Gateway Payment ID: {}", gatewayResponse.getGatewayPaymentId());

        Payment payment = buildPayment(request, gatewayResponse, commissionRate, commissionAmount, teacherEarnings);
        payment = paymentRepository.save(payment);

        log.info("✅ Payment saved - ID: {}, Order ID: {}", payment.getId(), payment.getOrderId());
        log.info("==================== CREATE PAYMENT SUCCESS ====================");

        return buildPaymentResponse(payment, gatewayResponse);
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + request.getAmount());
        }
        if (!hasText(request.getBookingId()) && !hasText(request.getCourseId())) {
            throw new IllegalArgumentException("Either Booking ID or Course ID is required");
        }
        if (!hasText(request.getStudentId())) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (!hasText(request.getTeacherId())) {
            throw new IllegalArgumentException("Teacher ID is required");
        }
        if (request.getGateway() == null) {
            throw new IllegalArgumentException("Payment gateway is required");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (Boolean.TRUE.equals(request.getIsInstallment())) {
            if (request.getTotalInstallments() == null || request.getTotalInstallments() <= 0) {
                throw new IllegalArgumentException("Total installments must be greater than zero");
            }
        }
        log.info("✅ Request validation passed");
    }

    private Payment buildPayment(PaymentRequest request,
                                 PaymentResponse gatewayResponse,
                                 BigDecimal commissionRate,
                                 BigDecimal commissionAmount,
                                 BigDecimal teacherEarnings) {

        return Payment.builder()
                .orderId(gatewayResponse.getOrderId())
                .bookingId(trimToNull(request.getBookingId()))
                .studentId(request.getStudentId())
                .teacherId(request.getTeacherId())
                .courseId(trimToNull(request.getCourseId()))
                .amount(request.getAmount())
                .currency(normalizeCurrency(request.getCurrency()))
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .gateway(request.getGateway())
                .gatewayPaymentId(trimToNull(gatewayResponse.getGatewayPaymentId()))
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmount)
                .teacherEarnings(teacherEarnings)
                .isInstallment(Boolean.TRUE.equals(request.getIsInstallment()))
                .installmentNumber(Boolean.TRUE.equals(request.getIsInstallment()) ? 1 : null)
                .totalInstallments(Boolean.TRUE.equals(request.getIsInstallment()) ? request.getTotalInstallments() : null)
                .description(trimToNull(request.getDescription()))
                .receiptEmail(trimToNull(request.getReceiptEmail()))
                .metadata(request.getMetadata())
                .isNegotiated(Boolean.TRUE.equals(request.getIsNegotiated()))
                .build();
    }

    private PaymentResponse buildPaymentResponse(Payment payment, PaymentResponse gatewayResponse) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .bookingId(payment.getBookingId())
                .studentId(payment.getStudentId())
                .teacherId(payment.getTeacherId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .gateway(payment.getGateway())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .clientSecret(gatewayResponse.getClientSecret())
                .checkoutUrl(gatewayResponse.getCheckoutUrl())
                .nextAction(gatewayResponse.getNextAction())
                .commissionAmount(payment.getCommissionAmount())
                .teacherEarnings(payment.getTeacherEarnings())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    @Transactional
    public PaymentDto confirmPayment(String paymentIdentifier) {
        log.info("Confirming payment: {}", paymentIdentifier);

        Payment payment = findPaymentByAnyId(paymentIdentifier);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.warn("Payment already completed: {}", paymentIdentifier);
            return mapToDto(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm payment with status: " + payment.getStatus());
        }

        BigDecimal amount = payment.getAmount();
        BigDecimal gatewayFee = BigDecimal.ZERO;
        BigDecimal netAmount = payment.getAmount();

        if (payment.getGateway() == PaymentGateway.RAZORPAY) {
            /*
             * Important:
             * Razorpay order creation does not mean the payment is completed.
             * This method should only be called after the application receives
             * verified success from frontend callback, signature verification,
             * or webhook flow.
             */
            log.info("Razorpay payment marked for completion using existing verified app flow. paymentId={}, orderId={}",
                    payment.getId(), payment.getOrderId());

            if (!hasText(payment.getOrderId())) {
                throw new IllegalStateException("Razorpay payment cannot be confirmed without orderId");
            }

        } else {
            PaymentGatewayInterface gateway = gatewayFactory.getGateway(payment.getGateway());
            if (gateway == null) {
                throw new IllegalStateException("Payment gateway not found: " + payment.getGateway());
            }

            PaymentResponse gatewayResponse;
            try {
                gatewayResponse = gateway.capturePayment(payment.getGatewayPaymentId());
            } catch (Exception e) {
                log.error("❌ Error while capturing payment from gateway", e);
                throw new RuntimeException("Payment capture failed: " + e.getMessage(), e);
            }

            if (gatewayResponse == null || gatewayResponse.getAmount() == null) {
                throw new IllegalStateException("Gateway capture returned invalid response");
            }

            amount = gatewayResponse.getAmount();
            gatewayFee = gatewayResponse.getGatewayFee() != null ? gatewayResponse.getGatewayFee() : BigDecimal.ZERO;
            netAmount = amount.subtract(gatewayFee);

            if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Net amount cannot be negative");
            }

            payment.setGatewayFee(gatewayFee);
            payment.setGatewayFeePercentage(gatewayResponse.getGatewayFeePercentage());
            payment.setNetAmount(netAmount);
        }

        boolean isNegotiated = Boolean.TRUE.equals(payment.getIsNegotiated());

        BigDecimal platformRate = commissionService.getPlatformFeeRate(isNegotiated);
        BigDecimal platformFee = commissionService.calculatePlatformFee(netAmount, isNegotiated);
        BigDecimal teacherEarnings = commissionService.calculateTeacherEarnings(netAmount, isNegotiated);

        payment.setPlatformFeePercentage(platformRate);
        payment.setPlatformFee(platformFee);

        payment.setCommissionRate(platformRate);
        payment.setCommissionAmount(platformFee);
        payment.setTeacherEarnings(teacherEarnings);

        payment.setAmount(amount);
        if (payment.getNetAmount() == null) {
            payment.setNetAmount(netAmount);
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        transactionService.createPaymentTransaction(payment);

        if (Boolean.TRUE.equals(payment.getIsInstallment())) {
            installmentService.updateInstallmentProgress(payment);
        }

        try {
            log.info("📤 About to publish Kafka event for payment: {}", payment.getId());
            eventPublisher.publishPaymentCompleted(payment);
            log.info("✅ Kafka event publish call completed");
        } catch (Exception e) {
            log.error("❌ Failed to publish Kafka event", e);
        }

        log.info("Payment confirmed successfully: {}", paymentIdentifier);
        return mapToDto(payment);
    }

    private Payment findPaymentByAnyId(String identifier) {
        log.debug("Looking up payment by identifier: {}", identifier);

        Optional<Payment> payment = paymentRepository.findById(identifier);
        if (payment.isPresent()) {
            log.debug("✅ Found payment by ID: {}", identifier);
            return payment.get();
        }

        payment = paymentRepository.findByGatewayPaymentId(identifier);
        if (payment.isPresent()) {
            log.debug("✅ Found payment by gateway payment ID: {}", identifier);
            return payment.get();
        }

        payment = paymentRepository.findByOrderId(identifier);
        if (payment.isPresent()) {
            log.debug("✅ Found payment by order ID: {}", identifier);
            return payment.get();
        }

        log.error("❌ Payment not found for identifier: {}", identifier);
        throw new IllegalArgumentException("Payment not found: " + identifier);
    }

    private Payment getPaymentById(String paymentId) {
        return findPaymentByAnyId(paymentId);
    }

    @Transactional
    public PaymentDto failPayment(String paymentId, String reason) {
        log.info("Failing payment: {}, Reason: {}", paymentId, reason);

        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Completed payment cannot be marked as failed");
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return mapToDto(payment);
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(hasText(reason) ? reason : "Payment failed");
        payment = paymentRepository.save(payment);

        try {
            eventPublisher.publishPaymentFailed(payment);
        } catch (Exception e) {
            log.error("❌ Failed to publish payment failed event", e);
        }

        log.info("Payment failed: {}", paymentId);
        return mapToDto(payment);
    }

    public PaymentDto getPayment(String paymentId) {
        return mapToDto(getPaymentById(paymentId));
    }

    public Page<PaymentDto> getPaymentsByStudent(String studentId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByStudentId(studentId, pageable);
        return payments.map(this::mapToDto);
    }

    public Page<PaymentDto> getPaymentsByTeacher(String teacherId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByTeacherId(teacherId, pageable);
        return payments.map(this::mapToDto);
    }

    private PaymentDto mapToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .bookingId(payment.getBookingId())
                .studentId(payment.getStudentId())
                .teacherId(payment.getTeacherId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .gateway(payment.getGateway())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .commissionRate(payment.getCommissionRate())
                .commissionAmount(payment.getCommissionAmount())
                .teacherEarnings(payment.getTeacherEarnings())
                .gatewayFee(payment.getGatewayFee())
                .netAmount(payment.getNetAmount())
                .platformFee(payment.getPlatformFee())
                .platformFeePercentage(payment.getPlatformFeePercentage())
                .isInstallment(payment.getIsInstallment())
                .installmentNumber(payment.getInstallmentNumber())
                .totalInstallments(payment.getTotalInstallments())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeCurrency(String currency) {
        if (!hasText(currency)) {
            return DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}