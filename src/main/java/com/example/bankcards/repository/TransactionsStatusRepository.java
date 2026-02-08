package com.example.bankcards.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.model.entity.TransactionsStatusEntity;
import com.example.bankcards.model.enums.TransactionsStatusType;


public interface TransactionsStatusRepository extends JpaRepository<TransactionsStatusEntity, Long> {

    Optional<TransactionsStatusEntity> findByTransactionsStatus(TransactionsStatusType transactionsStatus);

}
