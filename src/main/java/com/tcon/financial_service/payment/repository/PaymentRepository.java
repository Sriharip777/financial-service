package com.tcon.financial_service.payment.repository;

import com.tcon.financial_service.payment.entity.Payment;
import com.tcon.financial_service.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    // Find by unique identifiers
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByBookingId(String bookingId);

    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    // Find by user
    Page<Payment> findByStudentId(String studentId, Pageable pageable);

    Page<Payment> findByTeacherId(String teacherId, Pageable pageable);

    List<Payment> findByStudentId(String studentId);

    List<Payment> findByTeacherId(String teacherId);

    // Find by status
    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByTeacherIdAndStatus(String teacherId, PaymentStatus status);

    List<Payment> findByStudentIdAndStatus(String studentId, PaymentStatus status);

    // Find by date range
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Payment> findByTeacherIdAndCreatedAtBetween(
            String teacherId,
            LocalDateTime start,
            LocalDateTime end
    );

    // ✅ Add this method for earnings calculation
    List<Payment> findByTeacherIdAndStatusAndCompletedAtBetween(
            String teacherId,
            PaymentStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    // Alternative using @Query annotation
    @Query("{ 'teacherId': ?0, 'status': 'COMPLETED', 'completedAt': { $gte: ?1, $lte: ?2 } }")
    List<Payment> findCompletedPaymentsByTeacherAndDateRange(
            String teacherId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Find by booking and installment
    Optional<Payment> findByBookingIdAndInstallmentNumber(
            String bookingId,
            Integer installmentNumber
    );

    List<Payment> findByBookingIdAndIsInstallmentTrue(String bookingId);

    // Count queries
    long countByTeacherIdAndStatus(String teacherId, PaymentStatus status);

    long countByStudentIdAndStatus(String studentId, PaymentStatus status);
}
