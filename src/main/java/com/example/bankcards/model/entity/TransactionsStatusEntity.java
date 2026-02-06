package com.example.bankcards.model.entity;

import java.util.List;

import com.example.bankcards.model.enums.TransactionsStatusType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Table(name = "status_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TransactionsStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, unique = true)
    private TransactionsStatusType transactionsStatus;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(
        mappedBy = "transactionsStatus",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = false
    )
    private List<PaymentTransactionsEntity> paymentTransactionsEntities;

}
