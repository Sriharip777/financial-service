    package com.tcon.financial_service.analytics.controller;


    import com.tcon.financial_service.analytics.dto.FinancialSummaryDto;
    import com.tcon.financial_service.analytics.service.AnalyticsService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDate;

    @Slf4j
    @RestController
    @RequestMapping("/api/teacher/analytics")
    @RequiredArgsConstructor
    public class TeacherSelfAnalyticsController {

        private final AnalyticsService analyticsService;

        @GetMapping("/summary")
        public FinancialSummaryDto getMySummary(
                @RequestHeader("X-User-Id") String teacherId,
                @RequestParam(required = false) LocalDate startDate,
                @RequestParam(required = false) LocalDate endDate
        ) {
            log.info("🔥 /api/teacher/analytics/summary called for teacherId={}", teacherId);
            try {
                return analyticsService.getTeacherSummary(teacherId, startDate, endDate);
            } catch (Exception ex) {
                log.error("🔥 Error in /api/teacher/analytics/summary", ex);
                throw ex;
            }
        }
    }