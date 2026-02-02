package com.tcon.financial_service.transaction.repository;

import com.tcon.financial_service.transaction.entity.Transaction;
import com.tcon.financial_service.transaction.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    Page<Transaction> findByFromUserId(String fromUserId, Pageable pageable);

    Page<Transaction> findByToUserId(String toUserId, Pageable pageable);

    List<Transaction> findByType(TransactionType type);

    List<Transaction> findByReferenceId(String referenceId);
}
