package com.tcon.financial_service.transaction.service;

import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payout.entity.Payout;
import com.tcon.financial_service.refund.entity.Refund;
import com.tcon.financial_service.transaction.entity.Transaction;
import com.tcon.financial_service.transaction.entity.TransactionType;
import com.tcon.financial_service.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public void createPaymentTransaction(Payment payment) {
        log.debug("Creating payment transaction for: {}", payment.getId());

        Transaction transaction = Transaction.builder()
                .referenceId(payment.getId())
                .type(TransactionType.PAYMENT)
                .fromUserId(payment.getStudentId())
                .toUserId(payment.getTeacherId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description("Payment for booking: " + payment.getBookingId())
                .build();

        transactionRepository.save(transaction);
    }

    @Transactional
    public void createPayoutTransaction(Payout payout) {
        log.debug("Creating payout transaction for: {}", payout.getId());

        Transaction transaction = Transaction.builder()
                .referenceId(payout.getId())
                .type(TransactionType.PAYOUT)
                .fromUserId("PLATFORM")
                .toUserId(payout.getTeacherId())
                .amount(payout.getAmount())
                .currency(payout.getCurrency())
                .description("Payout to teacher: " + payout.getTeacherId())
                .build();

        transactionRepository.save(transaction);
    }

    @Transactional
    public void createRefundTransaction(Refund refund, Payment payment) {
        log.debug("Creating refund transaction for: {}", refund.getId());

        Transaction transaction = Transaction.builder()
                .referenceId(refund.getId())
                .type(TransactionType.REFUND)
                .fromUserId(payment.getTeacherId())
                .toUserId(payment.getStudentId())
                .amount(refund.getRefundAmount())
                .currency(refund.getCurrency())
                .description("Refund for booking: " + refund.getBookingId())
                .build();

        transactionRepository.save(transaction);
    }

    public Page<Transaction> getUserTransactions(String userId, Pageable pageable) {
        return transactionRepository.findByFromUserId(userId, pageable);
    }
}

