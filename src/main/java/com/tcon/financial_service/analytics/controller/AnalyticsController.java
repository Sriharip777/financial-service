package com.tcon.financial_service.analytics.controller;

import com.tcon.financial_service.analytics.dto.FinancialSummaryDto;
import com.tcon.financial_service.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ✅ EXISTING API (UNCHANGED)
    @GetMapping("/summary")
    public FinancialSummaryDto getSummary() {
        return analyticsService.getFinancialSummary();
    }

    // ✅ NEW: WITH DATE FILTER
    @GetMapping("/summary/filter")
    public FinancialSummaryDto getSummaryWithFilter(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return analyticsService.getFinancialSummary(startDate, endDate);
    }

    // ✅ NEW: TEACHER EARNINGS
    @GetMapping("/teacher/{teacherId}")
    public FinancialSummaryDto getTeacherSummary(
            @PathVariable String teacherId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return analyticsService.getTeacherSummary(teacherId, startDate, endDate);
    }
}