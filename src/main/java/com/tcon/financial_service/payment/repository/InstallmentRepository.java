package com.tcon.financial_service.payment.repository;

import com.tcon.financial_service.payment.entity.Installment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstallmentRepository extends MongoRepository<Installment, String> {

    Optional<Installment> findByBookingId(String bookingId);

    List<Installment> findByStudentId(String studentId);

    List<Installment> findByTeacherId(String teacherId);

    List<Installment> findByIsActiveTrue();
}
