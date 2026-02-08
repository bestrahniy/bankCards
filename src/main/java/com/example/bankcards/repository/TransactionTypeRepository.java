package com.example.bankcards.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.model.entity.TransactionTypeEntity;
import com.example.bankcards.model.enums.TransactionsType;


public interface TransactionTypeRepository extends JpaRepository<TransactionTypeEntity, Long> {

    Optional<TransactionTypeEntity> findByTransactionsType(TransactionsType transactionsType);
}
