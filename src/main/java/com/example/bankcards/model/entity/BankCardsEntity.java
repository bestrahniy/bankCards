package com.example.bankcards.model.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bank_cards")
@Data
public class BankCardsEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "number", columnDefinition = "VARCHAR(19)", nullable = false, unique = true)
    private String number;

    @Column(name = "CVC2", columnDefinition = "smallint", unique = false, nullable = false)
    private Short cvc2;

    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", unique = false, nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", columnDefinition = "TIMESTAMPTZ", nullable = false, unique = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name = "is_active", columnDefinition = "boolean", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UsersEntity user;

    @OneToOne(
        mappedBy = "bankCardsEntity",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CardAccountEntity cardAccountEntity;

}
