package com.example.bankcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.model.entity.TransactionsStatusEntity;

public interface TransactionsStatusRepository extends JpaRepository<TransactionsStatusEntity, Long> {
    
}
