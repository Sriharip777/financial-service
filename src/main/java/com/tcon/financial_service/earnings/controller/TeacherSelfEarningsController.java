package com.tcon.financial_service.earnings.controller;

import com.tcon.financial_service.earnings.dto.TeacherEarningsResponseDto;
import com.tcon.financial_service.earnings.service.TeacherEarningsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/teacher/earnings")
@RequiredArgsConstructor
public class TeacherSelfEarningsController {

    private final TeacherEarningsQueryService service;

    // ✅ 1. Get my earnings (paginated)
    @GetMapping
    public Page<TeacherEarningsResponseDto> getMyEarnings(

            @RequestHeader("X-User-Id") String teacherId,

            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String studentId,

            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return service.getEarnings(
                teacherId,   // 🔥 force teacherId from header
                courseId,
                studentId,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                direction
        );
    }
}