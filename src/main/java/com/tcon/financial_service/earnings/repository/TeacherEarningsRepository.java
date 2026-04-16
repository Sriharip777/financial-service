package com.tcon.financial_service.earnings.repository;

import com.tcon.financial_service.earnings.entity.TeacherEarnings;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TeacherEarningsRepository extends MongoRepository<TeacherEarnings, String> {

    // ✅ EXISTING (KEEP)
    boolean existsByPaymentId(String paymentId);

    List<TeacherEarnings> findByTeacherId(String teacherId);

    List<TeacherEarnings> findByTeacherIdAndIsPaidOutFalse(String teacherId);

    // ================= NEW (FOR ANALYTICS) =================

    // ✅ Date range filter (admin dashboard)
    List<TeacherEarnings> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // ✅ Teacher + date filter
    List<TeacherEarnings> findByTeacherIdAndCreatedAtBetween(
            String teacherId,
            LocalDateTime start,
            LocalDateTime end
    );

    // ✅ Unpaid + date filter (for payouts optimization)
    List<TeacherEarnings> findByIsPaidOutFalseAndCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}