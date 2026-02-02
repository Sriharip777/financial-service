package com.tcon.financial_service.event;


import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payout.entity.Payout;
import com.tcon.financial_service.refund.entity.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(Payment payment) {
        log.info("Publishing payment completed event: {}", payment.getId());

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PAYMENT_COMPLETED");
        event.put("paymentId", payment.getId());
        event.put("bookingId", payment.getBookingId());
        event.put("studentId", payment.getStudentId());
        event.put("teacherId", payment.getTeacherId());
        event.put("amount", payment.getAmount());
        event.put("currency", payment.getCurrency());
        event.put("teacherEarnings", payment.getTeacherEarnings());
        event.put("timestamp", payment.getCompletedAt());

        kafkaTemplate.send("payment-completed", payment.getId(), event);
    }

    public void publishPaymentFailed(Payment payment) {
        log.info("Publishing payment failed event: {}", payment.getId());

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PAYMENT_FAILED");
        event.put("paymentId", payment.getId());
        event.put("bookingId", payment.getBookingId());
        event.put("studentId", payment.getStudentId());
        event.put("teacherId", payment.getTeacherId());
        event.put("failureReason", payment.getFailureReason());
        event.put("timestamp", payment.getUpdatedAt());

        kafkaTemplate.send("payment-failed", payment.getId(), event);
    }

    public void publishRefundCompleted(Refund refund, Payment payment) {
        log.info("Publishing refund completed event: {}", refund.getId());

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "REFUND_COMPLETED");
        event.put("refundId", refund.getId());
        event.put("paymentId", refund.getPaymentId());
        event.put("bookingId", refund.getBookingId());
        event.put("studentId", refund.getStudentId());
        event.put("teacherId", refund.getTeacherId());
        event.put("refundAmount", refund.getRefundAmount());
        event.put("originalAmount", refund.getOriginalAmount());
        event.put("isFullRefund", refund.getIsFullRefund());
        event.put("timestamp", refund.getProcessedAt());

        kafkaTemplate.send("refund-completed", refund.getId(), event);
    }

    public void publishPayoutCompleted(Payout payout) {
        log.info("Publishing payout completed event: {}", payout.getId());

        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PAYOUT_COMPLETED");
        event.put("payoutId", payout.getId());
        event.put("teacherId", payout.getTeacherId());
        event.put("amount", payout.getAmount());
        event.put("currency", payout.getCurrency());
        event.put("timestamp", payout.getProcessedAt());

        kafkaTemplate.send("payout-completed", payout.getId(), event);
    }
}
