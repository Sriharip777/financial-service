package com.tcon.financial_service.earnings.controller;

import com.tcon.financial_service.earnings.dto.TeacherEarningsResponseDto;
import com.tcon.financial_service.earnings.service.TeacherEarningsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/earnings")
@RequiredArgsConstructor
public class TeacherEarningsController {

    private final TeacherEarningsQueryService service;

    @GetMapping
    public Page<TeacherEarningsResponseDto> getEarnings(

            @RequestParam(required = false) String teacherId,
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
                teacherId,
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