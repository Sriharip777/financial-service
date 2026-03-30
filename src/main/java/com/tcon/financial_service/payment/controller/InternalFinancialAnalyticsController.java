package com.tcon.financial_service.payment.controller;

import com.tcon.financial_service.payment.dto.MonthlyRevenueStatDto;
import com.tcon.financial_service.payment.dto.TeacherEarningsAnalyticsDto;
import com.tcon.financial_service.payment.service.InternalFinancialAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/analytics")
@RequiredArgsConstructor
public class InternalFinancialAnalyticsController {

    private final InternalFinancialAnalyticsService analyticsService;

    @GetMapping("/teachers")
    public List<TeacherEarningsAnalyticsDto> getTeacherEarnings() {
        return analyticsService.getTeacherEarnings();
    }

    @GetMapping("/overview")
    public List<MonthlyRevenueStatDto> getOverviewRevenue() {
        return analyticsService.getOverviewRevenue();
    }
}