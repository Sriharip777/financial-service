package com.tcon.financial_service.payout.repository;

import com.tcon.financial_service.payout.entity.TeacherEarnings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherEarningsRepository extends MongoRepository<TeacherEarnings, String> {

    Optional<TeacherEarnings> findByTeacherId(String teacherId);
}

