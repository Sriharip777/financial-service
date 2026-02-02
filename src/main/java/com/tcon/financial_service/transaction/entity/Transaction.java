package com.tcon.financial_service.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Indexed
    private String referenceId;  // Payment/Payout/Refund ID

    private TransactionType type;

    @Indexed
    private String fromUserId;

    @Indexed
    private String toUserId;

    private BigDecimal amount;
    private String currency;

    private String description;

    @CreatedDate
    private LocalDateTime timestamp;
}

