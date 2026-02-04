package com.example.bankcards.model.entity;

import java.security.Timestamp;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@Entity
@Table(name = "payment_transactions")
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransactionsEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_card_account_id")
    private CardAccountEntity senderCardAccountId;

    @Column(name = "recipient_account_id", columnDefinition = "UUID", unique = false, nullable = false)
    private UUID recipientAccountId;

    @Column(name = "comment", columnDefinition = "text", unique = false)
    private String comment;

    @Column(name = "amount", columnDefinition = "DECIMAL(15, 2)", nullable = false)
    private Double amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id")
    private TransactionTypeEntity transactionType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private TransactionsStatusEntity transactionsStatus;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", nullable = false)
    private Timestamp createdat;

}
