package com.tcon.financial_service.payout.repository;

import com.tcon.financial_service.payout.entity.Payout;
import com.tcon.financial_service.payout.entity.PayoutStatus; // ✅ Correct import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PayoutRepository extends MongoRepository<Payout, String> {

    Page<Payout> findByTeacherId(String teacherId, Pageable pageable);

    List<Payout> findByStatus(PayoutStatus status);

    @Query("{ 'teacherId': ?0, 'periodStart': { $gte: ?1 }, 'periodEnd': { $lte: ?2 } }")
    List<Payout> findByTeacherAndPeriod(String teacherId, LocalDate start, LocalDate end);

    @Query("{ 'status': 'PENDING', 'scheduledAt': { $lte: ?0 } }")
    List<Payout> findPendingPayoutsScheduledBefore(LocalDateTime dateTime);
}
