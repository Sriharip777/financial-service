package com.tcon.financial_service.refund.repository;

import com.tcon.financial_service.refund.entity.Refund;
import com.tcon.financial_service.refund.entity.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends MongoRepository<Refund, String> {

    Optional<Refund> findByPaymentId(String paymentId);

    Page<Refund> findByStudentId(String studentId, Pageable pageable);

    List<Refund> findByStatus(RefundStatus status);

    Page<Refund> findByTeacherId(String teacherId, Pageable pageable);
}

