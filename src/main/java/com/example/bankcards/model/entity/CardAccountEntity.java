package com.example.bankcards.model.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "card_accaunt")
@Builder
public class CardAccountEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "current_balance", columnDefinition="DECIMAL(15,2)", nullable = false, unique = false)
    private Double currentBalance;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ", nullable = false, unique = false)
    private Instant updatedAt;

    @OneToOne
    @JoinColumn(name = "card_id")
    private BankCardsEntity bankCardsEntity;

    @OneToMany(
        mappedBy = "senderCardAccountId",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<PaymentTransactionsEntity> paymentTransactionsEntities;

}
