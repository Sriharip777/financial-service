package com.tcon.financial_service.payout.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.tcon.financial_service.payout.entity.Payout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankTransferService {

    private final RazorpayClient razorpayClient;

    @Value("${payment.razorpay.account-number:2323230041234967}")
    private String razorpayAccountNumber;

    @Value("${payout.mock-mode:true}")
    private boolean mockMode;

    public String transferToBank(Payout payout) throws RazorpayException {
        log.info("Initiating bank transfer for payout: {}", payout.getId());

        if (mockMode) {
            return mockBankTransfer(payout);
        }

        // Real implementation (requires Razorpay X Payouts to be enabled)
        return realBankTransfer(payout);
    }

    private String mockBankTransfer(Payout payout) {
        log.info("MOCK MODE: Simulating bank transfer");
        log.info("Teacher: {}, Amount: {} {}",
                payout.getTeacherId(),
                payout.getAmount(),
                payout.getCurrency());
        log.info("Bank Account: {}, IFSC: {}, Name: {}",
                maskBankAccount(payout.getBankAccountNumber()),
                payout.getBankIfscCode(),
                payout.getAccountHolderName());

        // Generate mock payout ID
        String mockPayoutId = "pout_mock_" + UUID.randomUUID().toString().substring(0, 14);

        log.info("MOCK: Bank transfer successful. Payout ID: {}", mockPayoutId);

        return mockPayoutId;
    }

    private String realBankTransfer(Payout payout) throws RazorpayException {
        log.warn("Real Razorpay Payouts not implemented yet!");
        log.warn("To use Razorpay Payouts:");
        log.warn("1. Contact Razorpay to enable Payouts (Razorpay X)");
        log.warn("2. Complete business KYC");
        log.warn("3. Link current account");
        log.warn("4. Get CA account number from Razorpay dashboard");

        throw new RazorpayException(
                "Razorpay Payouts not enabled. Please contact Razorpay support to activate Razorpay X."
        );
    }

    public String transferViaStripe(Payout payout) throws Exception {
        log.info("Initiating Stripe transfer for payout: {}", payout.getId());

        if (mockMode) {
            return mockStripeTransfer(payout);
        }

        throw new UnsupportedOperationException(
                "Stripe Connect Transfers not implemented yet. " +
                        "Please implement using Stripe Transfer API."
        );
    }

    private String mockStripeTransfer(Payout payout) {
        log.info("MOCK MODE: Simulating Stripe transfer");
        log.info("Teacher: {}, Amount: {} {}",
                payout.getTeacherId(),
                payout.getAmount(),
                payout.getCurrency());

        String mockTransferId = "tr_mock_" + UUID.randomUUID().toString().substring(0, 14);

        log.info("MOCK: Stripe transfer successful. Transfer ID: {}", mockTransferId);

        return mockTransferId;
    }

    private String maskBankAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
