package com.tcon.financial_service.payment.service;

import com.tcon.financial_service.payment.dto.MonthlyRevenueStatDto;
import com.tcon.financial_service.payment.dto.TeacherEarningsAnalyticsDto;
import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import com.tcon.financial_service.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternalFinancialAnalyticsService {

    private final PaymentRepository paymentRepository;

    public List<TeacherEarningsAnalyticsDto> getTeacherEarnings() {
        List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);

        return payments.stream()
                .filter(p -> p.getTeacherId() != null)
                .collect(Collectors.groupingBy(Payment::getTeacherId))
                .entrySet()
                .stream()
                .map(entry -> TeacherEarningsAnalyticsDto.builder()
                        .teacherId(entry.getKey())
                        .earnings(entry.getValue().stream()
                                .map(Payment::getTeacherEarnings)
                                .filter(Objects::nonNull)
                                .map(BigDecimal::doubleValue)
                                .reduce(0.0, Double::sum))
                        .build())
                .collect(Collectors.toList());
    }

    public List<MonthlyRevenueStatDto> getOverviewRevenue() {
        List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
        Map<String, Double> grouped = new LinkedHashMap<>();

        java.time.YearMonth now = java.time.YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = now.minusMonths(i);
            grouped.put(monthLabel(ym.getMonthValue()), 0.0);
        }

        for (Payment payment : payments) {
            if (payment.getCompletedAt() != null && payment.getAmount() != null) {
                String label = monthLabel(payment.getCompletedAt().getMonthValue());
                if (grouped.containsKey(label)) {
                    grouped.put(label, grouped.get(label) + payment.getAmount().doubleValue());
                }
            }
        }

        return grouped.entrySet().stream()
                .map(e -> MonthlyRevenueStatDto.builder()
                        .label(e.getKey())
                        .revenue(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private String monthLabel(int month) {
        return switch (month) {
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Aug";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> "N/A";
        };
    }
}