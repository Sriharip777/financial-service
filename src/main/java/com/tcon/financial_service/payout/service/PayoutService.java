package com.tcon.financial_service.payout.service;

import com.tcon.financial_service.event.PaymentEventPublisher;
import com.tcon.financial_service.payout.entity.PayoutStatus;
import com.tcon.financial_service.payout.dto.PayoutDto;
import com.tcon.financial_service.payout.dto.PayoutRequest;
import com.tcon.financial_service.payout.entity.Payout;
import com.tcon.financial_service.payout.repository.PayoutRepository;
import com.tcon.financial_service.transaction.service.TransactionService;
import com.tcon.financial_service.earnings.entity.TeacherEarnings;
import com.tcon.financial_service.earnings.repository.TeacherEarningsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final EarningsCalculationService earningsService;
    private final BankTransferService bankTransferService;
    private final TransactionService transactionService;
    private final PaymentEventPublisher eventPublisher;

    // ✅ NEW
    private final TeacherEarningsRepository teacherEarningsRepository;

    @Transactional
    public PayoutDto createPayout(PayoutRequest request) throws Exception {
        log.info("Creating payout for teacher: {}", request.getTeacherId());

        BigDecimal availableEarnings = earningsService.getPendingAmount(request.getTeacherId());

        if (availableEarnings.compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient earnings. Available: " + availableEarnings +
                            ", Requested: " + request.getAmount());
        }

        // ✅ NEW: fetch unpaid earnings
        List<TeacherEarnings> earningsList =
                teacherEarningsRepository.findByTeacherIdAndIsPaidOutFalse(request.getTeacherId());

        List<String> earningIds = earningsList.stream()
                .map(TeacherEarnings::getId)
                .collect(Collectors.toList());

        Payout payout = Payout.builder()
                .teacherId(request.getTeacherId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PayoutStatus.PENDING)
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfscCode(request.getBankIfscCode())
                .accountHolderName(request.getAccountHolderName())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .transactionId("TXN_" + UUID.randomUUID().toString())
                .scheduledAt(LocalDateTime.now())

                // ✅ NEW FIELDS
                .earningIds(earningIds)
                .totalTransactions(earningIds.size())

                .build();

        payout = payoutRepository.save(payout);

        log.info("Payout created: {}", payout.getId());

        return mapToDto(payout);
    }

    @Transactional
    public PayoutDto processPayout(String payoutId) throws Exception {
        log.info("Processing payout: {}", payoutId);

        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found: " + payoutId));

        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new IllegalStateException("Payout already processed: " + payoutId);
        }

        payout.setStatus(PayoutStatus.PROCESSING);
        payout = payoutRepository.save(payout);

        try {
            String gatewayPayoutId = bankTransferService.transferToBank(payout);

            payout.setGatewayPayoutId(gatewayPayoutId);
            payout.setStatus(PayoutStatus.COMPLETED);
            payout.setProcessedAt(LocalDateTime.now());

            // ✅ EXISTING
            earningsService.processPayout(payout);

            // ✅ NEW: mark earnings as paid (SAFE)
            if (payout.getEarningIds() != null) {
                List<TeacherEarnings> earningsList =
                        teacherEarningsRepository.findAllById(payout.getEarningIds());

                earningsList.forEach(e -> e.setIsPaidOut(true));
                teacherEarningsRepository.saveAll(earningsList);
            }

            transactionService.createPayoutTransaction(payout);
            eventPublisher.publishPayoutCompleted(payout);

            log.info("Payout processed successfully: {}", payoutId);

        } catch (Exception e) {
            log.error("Payout processing failed: {}", e.getMessage(), e);
            payout.setStatus(PayoutStatus.FAILED);
            payout.setFailureReason(e.getMessage());
            throw e;
        }

        payout = payoutRepository.save(payout);

        return mapToDto(payout);
    }

    public PayoutDto getPayout(String payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found: " + payoutId));
        return mapToDto(payout);
    }

    public Page<PayoutDto> getPayoutsByTeacher(String teacherId, Pageable pageable) {
        Page<Payout> payouts = payoutRepository.findByTeacherId(teacherId, pageable);
        return payouts.map(this::mapToDto);
    }

    public List<PayoutDto> getPendingPayouts() {
        List<Payout> payouts = payoutRepository.findByStatus(PayoutStatus.PENDING);
        return payouts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PayoutDto mapToDto(Payout payout) {
        return PayoutDto.builder()
                .id(payout.getId())
                .teacherId(payout.getTeacherId())
                .amount(payout.getAmount())
                .currency(payout.getCurrency())
                .status(payout.getStatus())
                .bankAccountNumber(maskBankAccount(payout.getBankAccountNumber()))
                .bankIfscCode(payout.getBankIfscCode())
                .accountHolderName(payout.getAccountHolderName())
                .periodStart(payout.getPeriodStart())
                .periodEnd(payout.getPeriodEnd())
                .transactionId(payout.getTransactionId())
                .gatewayPayoutId(payout.getGatewayPayoutId())
                .failureReason(payout.getFailureReason())
                .createdAt(payout.getCreatedAt())
                .scheduledAt(payout.getScheduledAt())
                .processedAt(payout.getProcessedAt())
                .build();
    }

    private String maskBankAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}