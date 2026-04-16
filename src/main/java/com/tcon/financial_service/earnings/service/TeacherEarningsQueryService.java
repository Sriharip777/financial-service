package com.tcon.financial_service.earnings.service;

import com.tcon.financial_service.earnings.dto.TeacherEarningsResponseDto;
import com.tcon.financial_service.earnings.entity.TeacherEarnings;
import com.tcon.financial_service.earnings.repository.TeacherEarningsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherEarningsQueryService {

    private final TeacherEarningsRepository repository;

    public Page<TeacherEarningsResponseDto> getEarnings(
            String teacherId,
            String courseId,
            String studentId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // 🚀 FETCH FROM DB
        Page<TeacherEarnings> earningsPage = repository.findAll(pageable);

        // ✅ Convert to DTO list
        List<TeacherEarningsResponseDto> dtoList = earningsPage
                .map(this::mapToDto)
                .getContent();

        // ✅ Apply filters
        List<TeacherEarningsResponseDto> filteredList = dtoList.stream()
                .filter(e -> filter(e, teacherId, courseId, studentId, startDate, endDate))
                .toList();

        // ✅ Convert back to Page
        return new PageImpl<>(filteredList, pageable, filteredList.size());
    }

    // ================= FILTER =================
    private boolean filter(TeacherEarningsResponseDto e,
                           String teacherId,
                           String courseId,
                           String studentId,
                           LocalDate startDate,
                           LocalDate endDate) {

        if (teacherId != null && !teacherId.equals(e.getTeacherId())) return false;
        if (courseId != null && !courseId.equals(e.getCourseId())) return false;
        if (studentId != null && !studentId.equals(e.getStudentId())) return false;

        if (startDate != null && endDate != null) {
            if (e.getCreatedAt() == null) return false;

            LocalDate date = e.getCreatedAt().toLocalDate();
            if (date.isBefore(startDate) || date.isAfter(endDate)) return false;
        }

        return true;
    }

    // ================= MAPPER =================
    private TeacherEarningsResponseDto mapToDto(TeacherEarnings e) {
        return TeacherEarningsResponseDto.builder()
                .paymentId(e.getPaymentId())
                .teacherId(e.getTeacherId())
                .studentId(e.getStudentId())
                .bookingId(e.getBookingId())
                .courseId(e.getCourseId())
                .totalAmount(e.getTotalAmount())
                .gatewayFee(e.getGatewayFee())
                .platformFee(e.getPlatformFee())
                .netAmount(e.getNetAmount())
                .teacherEarning(e.getTeacherEarning())
                .isPaidOut(e.getIsPaidOut())
                .createdAt(e.getCreatedAt())
                .build();
    }
}