package com.tcon.financial_service.refund.service;

import com.tcon.financial_service.event.PaymentEventPublisher;
import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import com.tcon.financial_service.payment.gateway.PaymentGatewayFactory;
import com.tcon.financial_service.payment.gateway.PaymentGatewayInterface;
import com.tcon.financial_service.payment.repository.PaymentRepository;
import com.tcon.financial_service.refund.dto.RefundDto;
import com.tcon.financial_service.refund.dto.RefundRequest;
import com.tcon.financial_service.refund.entity.Refund;
import com.tcon.financial_service.refund.entity.RefundStatus;
import com.tcon.financial_service.refund.repository.RefundRepository;
import com.tcon.financial_service.transaction.service.TransactionService; // ✅ CORRECT
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundProcessingService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final TransactionService transactionService; // ✅ Your class
    private final PaymentEventPublisher eventPublisher;

    @Value("${refund.cancellation-hours-threshold}")
    private Integer cancellationHoursThreshold;

    @Value("${refund.processing-fee-percentage}")
    private BigDecimal processingFeePercentage;

    @Transactional
    public RefundDto processRefund(RefundRequest request) throws Exception {
        log.info("Processing refund for payment: {}", request.getPaymentId());

        Refund existingRefund = refundRepository.findByPaymentId(request.getPaymentId())
                .orElse(null);

        if (existingRefund != null) {
            throw new IllegalStateException("Refund already processed for this payment");
        }

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment not completed, cannot refund");
        }

        BigDecimal refundAmount;
        BigDecimal processingFee;
        boolean isFullRefund;

        if (request.getHoursBeforeClass() >= cancellationHoursThreshold) {
            refundAmount = payment.getAmount();
            processingFee = BigDecimal.ZERO;
            isFullRefund = true;
            log.info("Full refund - cancelled {} hours before class", request.getHoursBeforeClass());
        } else {
            refundAmount = BigDecimal.ZERO;
            processingFee = BigDecimal.ZERO;
            isFullRefund = false;
            log.info("No refund - cancelled {} hours before class", request.getHoursBeforeClass());
        }

        Refund refund = Refund.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .studentId(payment.getStudentId())
                .teacherId(payment.getTeacherId())
                .originalAmount(payment.getAmount())
                .refundAmount(refundAmount)
                .processingFee(processingFee)
                .currency(payment.getCurrency())
                .status(RefundStatus.PENDING)
                .reason(request.getReason())
                .initiatedBy(request.getInitiatedBy())
                .hoursBeforeClass(request.getHoursBeforeClass())
                .isFullRefund(isFullRefund)
                .build();

        refund = refundRepository.save(refund);

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                PaymentGatewayInterface gateway = gatewayFactory.getGateway(payment.getGateway());
                String gatewayRefundId = gateway.refundPayment(
                        payment.getGatewayPaymentId(),
                        refundAmount
                );

                refund.setGatewayRefundId(gatewayRefundId);
                refund.setStatus(RefundStatus.COMPLETED);
                refund.setProcessedAt(LocalDateTime.now());

                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);

                log.info("Refund completed successfully: {}", refund.getId());

            } catch (Exception e) {
                log.error("Refund processing failed: {}", e.getMessage(), e);
                refund.setStatus(RefundStatus.FAILED);
                refund.setFailureReason(e.getMessage());
                throw e;
            }
        } else {
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setProcessedAt(LocalDateTime.now());
            log.info("Refund request completed with zero amount: {}", refund.getId());
        }

        refund = refundRepository.save(refund);

        // Create transaction record
        transactionService.createRefundTransaction(refund, payment); // ✅ Now works

        eventPublisher.publishRefundCompleted(refund, payment);

        return mapToDto(refund);
    }

    public RefundDto getRefund(String refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));
        return mapToDto(refund);
    }

    public RefundDto getRefundByPaymentId(String paymentId) {
        Refund refund = refundRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Refund not found for payment: " + paymentId));
        return mapToDto(refund);
    }

    public Page<RefundDto> getRefundsByStudent(String studentId, Pageable pageable) {
        Page<Refund> refunds = refundRepository.findByStudentId(studentId, pageable);
        return refunds.map(this::mapToDto);
    }

    private RefundDto mapToDto(Refund refund) {
        return RefundDto.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .bookingId(refund.getBookingId())
                .studentId(refund.getStudentId())
                .teacherId(refund.getTeacherId())
                .originalAmount(refund.getOriginalAmount())
                .refundAmount(refund.getRefundAmount())
                .processingFee(refund.getProcessingFee())
                .currency(refund.getCurrency())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .initiatedBy(refund.getInitiatedBy())
                .gatewayRefundId(refund.getGatewayRefundId())
                .hoursBeforeClass(refund.getHoursBeforeClass())
                .isFullRefund(refund.getIsFullRefund())
                .failureReason(refund.getFailureReason())
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}
