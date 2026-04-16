package com.tcon.financial_service.payout.scheduler;

import com.tcon.financial_service.payout.dto.PayoutRequest;
import com.tcon.financial_service.payout.repository.TeacherPayoutRepository;
import com.tcon.financial_service.payout.service.EarningsCalculationService;
import com.tcon.financial_service.payout.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyPayoutScheduler {

    private final PayoutService payoutService;
    private final EarningsCalculationService earningsService;
    private final TeacherPayoutRepository earningsRepository;

    @Value("${payout.schedule.day-of-week}")
    private DayOfWeek payoutDay;

    @Value("${payout.minimum-amount}")
    private BigDecimal minimumPayoutAmount;

    // Run every Monday at 9:00 AM
    @Scheduled(cron = "0 0 9 * * MON")
    public void processWeeklyPayouts() {
        log.info("Starting weekly payout processing...");

        try {
            earningsRepository.findAll().forEach(earnings -> {
                try {
                    // Check if teacher has minimum payout amount
                    if (earnings.getPendingAmount().compareTo(minimumPayoutAmount) >= 0) {

                        log.info("Processing payout for teacher: {}, Amount: {}",
                                earnings.getTeacherId(), earnings.getPendingAmount());

                        // Create payout request
                        PayoutRequest request = PayoutRequest.builder()
                                .teacherId(earnings.getTeacherId())
                                .amount(earnings.getPendingAmount())
                                .currency(earnings.getCurrency())
                                .periodStart(LocalDate.now().minusWeeks(1))
                                .periodEnd(LocalDate.now())
                                .build();

                        // This would need teacher's bank details from user service
                        // For now, we just create the payout in PENDING status
                        payoutService.createPayout(request);

                    } else {
                        log.debug("Teacher {} has insufficient balance for payout: {}",
                                earnings.getTeacherId(), earnings.getPendingAmount());
                    }
                } catch (Exception e) {
                    log.error("Failed to process payout for teacher: {}",
                            earnings.getTeacherId(), e);
                }
            });

            log.info("Weekly payout processing completed");

        } catch (Exception e) {
            log.error("Weekly payout processing failed", e);
        }
    }

    // Update next payout date for all teachers
    @Scheduled(cron = "0 0 0 * * MON")
    public void updateNextPayoutDates() {
        log.info("Updating next payout dates...");

        LocalDateTime nextPayoutDate = LocalDateTime.now().plusWeeks(1);

        earningsRepository.findAll().forEach(earnings -> {
            earnings.setNextPayoutDate(nextPayoutDate);
            earningsRepository.save(earnings);
        });

        log.info("Next payout dates updated");
    }
}

