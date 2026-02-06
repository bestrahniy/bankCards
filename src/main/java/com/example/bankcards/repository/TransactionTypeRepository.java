package com.example.bankcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.model.entity.TransactionTypeEntity;

public interface TransactionTypeRepository extends JpaRepository<TransactionTypeEntity, Long> {
    
}
