package com.tcon.financial_service.payment.service;
import com.tcon.financial_service.event.PaymentEventPublisher;
import com.tcon.financial_service.payment.dto.PaymentDto;
import com.tcon.financial_service.payment.dto.PaymentRequest;
import com.tcon.financial_service.payment.dto.PaymentResponse;
import com.tcon.financial_service.payment.entity.Payment;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final CommissionCalculationService commissionService;
    private final InstallmentService installmentService;
    private final TransactionService transactionService;
    private final PaymentEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("==================== CREATE PAYMENT START ====================");

        try {
            // Step 1: Validate request
            log.debug("Step 1: Validating request");
            validatePaymentRequest(request);

            // Step 2: Get payment gateway
            log.debug("Step 2: Getting payment gateway: {}", request.getGateway());
            PaymentGatewayInterface gateway = gatewayFactory.getGateway(request.getGateway());
            if (gateway == null) {
                throw new IllegalStateException("Payment gateway not found: " + request.getGateway());
            }
            log.info("✅ Payment gateway loaded: {}", request.getGateway());

            // Step 3: Calculate commission
            log.debug("Step 3: Calculating commission");
            boolean isRecurring = request.getCourseId() != null &&
                    Boolean.TRUE.equals(request.getIsInstallment());

            BigDecimal commissionRate = commissionService.getCommissionRate(isRecurring);
            BigDecimal commissionAmount = commissionService.calculateCommission(request.getAmount(), isRecurring);
            BigDecimal teacherEarnings = commissionService.calculateTeacherEarnings(request.getAmount(), isRecurring);

            log.info("✅ Commission calculated - Rate: {}, Amount: {}, Teacher Earnings: {}",
                    commissionRate, commissionAmount, teacherEarnings);

            // Step 4: Create payment intent at gateway
            log.debug("Step 4: Creating payment intent at gateway");
            PaymentResponse gatewayResponse = gateway.createPaymentIntent(request);
            log.info("✅ Payment intent created - Gateway Payment ID: {}", gatewayResponse.getGatewayPaymentId());

            // Step 5: Save payment to database
            log.debug("Step 5: Saving payment to database");
            Payment payment = buildPayment(request, gatewayResponse, commissionRate, commissionAmount, teacherEarnings);
            payment = paymentRepository.save(payment);
            log.info("✅ Payment saved - ID: {}, Order ID: {}", payment.getId(), payment.getOrderId());

            // Step 6: Build response
            log.debug("Step 6: Building response");
            PaymentResponse response = buildPaymentResponse(payment, gatewayResponse);

            log.info("==================== CREATE PAYMENT SUCCESS ====================");
            return response;

        } catch (Exception e) {
            log.error("==================== CREATE PAYMENT FAILED ====================");
            log.error("❌ Error Type: {}", e.getClass().getSimpleName());
            log.error("❌ Error Message: {}", e.getMessage());
            log.error("❌ Stack Trace:", e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + request.getAmount());
        }
        // CHANGE: Either bookingId OR courseId must be present
        if ((request.getBookingId() == null || request.getBookingId().isEmpty()) &&
                (request.getCourseId() == null || request.getCourseId().isEmpty())) {
            throw new IllegalArgumentException("Either Booking ID or Course ID is required");
        }
        if (request.getStudentId() == null || request.getStudentId().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (request.getTeacherId() == null || request.getTeacherId().isEmpty()) {
            throw new IllegalArgumentException("Teacher ID is required");
        }
        if (request.getGateway() == null) {
            throw new IllegalArgumentException("Payment gateway is required");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        log.info("✅ Request validation passed");
    }

    private Payment buildPayment(PaymentRequest request, PaymentResponse gatewayResponse,
                                 BigDecimal commissionRate, BigDecimal commissionAmount,
                                 BigDecimal teacherEarnings) {
        return Payment.builder()
                .orderId(gatewayResponse.getOrderId())
                .bookingId(request.getBookingId())
                .studentId(request.getStudentId())
                .teacherId(request.getTeacherId())
                .courseId(request.getCourseId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .gateway(request.getGateway())
                .gatewayPaymentId(gatewayResponse.getGatewayPaymentId())
                .commissionRate(commissionRate)
                .commissionAmount(commissionAmount)
                .teacherEarnings(teacherEarnings)
                .isInstallment(Boolean.TRUE.equals(request.getIsInstallment()))
                .installmentNumber(Boolean.TRUE.equals(request.getIsInstallment()) ? 1 : null)
                .totalInstallments(request.getTotalInstallments())
                .description(request.getDescription())
                .receiptEmail(request.getReceiptEmail())
                .metadata(request.getMetadata())
                .isNegotiated(request.getIsNegotiated()) // ✅ NEW FIELD
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
    public PaymentDto confirmPayment(String paymentId) {
        log.info("Confirming payment: {}", paymentId);

        Payment payment = findPaymentByAnyId(paymentId);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            log.warn("Payment already completed: {}", paymentId);
            return mapToDto(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm payment with status: " + payment.getStatus());
        }

        BigDecimal amount;
        BigDecimal gatewayFee = BigDecimal.ZERO;
        BigDecimal netAmount;

        // ========= GATEWAY-SPECIFIC HANDLING =========
        if (payment.getGateway() == com.tcon.financial_service.payment.entity.PaymentGateway.RAZORPAY) {
            // Razorpay: we only have order_..., do NOT call payments.fetch/capture with this.
            log.info("Razorpay payment confirmation without server-side capture. paymentId={}, gatewayPaymentId={}",
                    payment.getId(), payment.getGatewayPaymentId());

            amount = payment.getAmount();
            netAmount = amount;
        } else {
            PaymentGatewayInterface gateway = gatewayFactory.getGateway(payment.getGateway());

            PaymentResponse gatewayResponse;
            try {
                gatewayResponse = gateway.capturePayment(payment.getGatewayPaymentId());
            } catch (Exception e) {
                log.error("❌ Error while capturing payment from gateway", e);
                throw new RuntimeException("Payment capture failed: " + e.getMessage(), e);
            }

            amount = gatewayResponse.getAmount();

            gatewayFee = gatewayResponse.getGatewayFee() != null
                    ? gatewayResponse.getGatewayFee()
                    : BigDecimal.ZERO;

            netAmount = amount.subtract(gatewayFee);

            payment.setGatewayFee(gatewayFee);
            payment.setGatewayFeePercentage(gatewayResponse.getGatewayFeePercentage());
            payment.setNetAmount(netAmount);
        }
        // ========= END GATEWAY-SPECIFIC HANDLING =========

        boolean isRecurring = payment.getCourseId() != null &&
                Boolean.TRUE.equals(payment.getIsInstallment());

        BigDecimal commissionRate = commissionService.getCommissionRate(isRecurring);
        BigDecimal platformFee = commissionService.calculateCommission(netAmount, isRecurring);
        BigDecimal teacherEarnings = netAmount.subtract(platformFee);

        Boolean isNegotiated = payment.getIsNegotiated();

        BigDecimal newPlatformRate = commissionService.getPlatformFeeRate(isNegotiated);
        BigDecimal newPlatformFee = commissionService.calculatePlatformFee(
                netAmount,
                isNegotiated
        );
        BigDecimal newTeacherEarnings = commissionService.calculateTeacherEarnings(
                netAmount,
                isNegotiated
        );

        commissionRate = newPlatformRate;
        platformFee = newPlatformFee;
        teacherEarnings = newTeacherEarnings;

        payment.setPlatformFee(platformFee);
        payment.setPlatformFeePercentage(commissionRate);

        payment.setCommissionRate(commissionRate);
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

        log.info("Payment confirmed successfully: {}", paymentId);
        return mapToDto(payment);
    }

    // ✅ Add this new method
    private Payment findPaymentByAnyId(String identifier) {
        log.debug("Looking up payment by identifier: {}", identifier);

        // Try by MongoDB ID first
        Optional<Payment> payment = paymentRepository.findById(identifier);
        if (payment.isPresent()) {
            log.debug("✅ Found payment by ID: {}", identifier);
            return payment.get();
        }

        // Try by gateway payment ID (Stripe PaymentIntent ID or Razorpay Order ID)
        payment = paymentRepository.findByGatewayPaymentId(identifier);
        if (payment.isPresent()) {
            log.debug("✅ Found payment by gateway payment ID: {}", identifier);
            return payment.get();
        }

        // Try by order ID
        payment = paymentRepository.findByOrderId(identifier);
        if (payment.isPresent()) {
            log.debug("✅ Found payment by order ID: {}", identifier);
            return payment.get();
        }

        // Not found by any identifier
        log.error("❌ Payment not found for identifier: {}", identifier);
        throw new IllegalArgumentException("Payment not found: " + identifier);
    }

    // ✅ Update getPaymentById to use the new method
    private Payment getPaymentById(String paymentId) {
        return findPaymentByAnyId(paymentId);
    }

    @Transactional
    public PaymentDto failPayment(String paymentId, String reason) {
        log.info("Failing payment: {}, Reason: {}", paymentId, reason);

        Payment payment = getPaymentById(paymentId);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment = paymentRepository.save(payment);

        eventPublisher.publishPaymentFailed(payment);

        log.info("Payment failed: {}", paymentId);
        return mapToDto(payment);
    }

    public PaymentDto getPayment(String paymentId) {
        Payment payment = getPaymentById(paymentId);
        return mapToDto(payment);
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
                .isInstallment(payment.getIsInstallment())
                .installmentNumber(payment.getInstallmentNumber())
                .totalInstallments(payment.getTotalInstallments())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}