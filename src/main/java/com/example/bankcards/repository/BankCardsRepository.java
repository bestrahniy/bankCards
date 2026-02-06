package com.example.bankcards.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.model.entity.BankCardsEntity;

public interface BankCardsRepository extends JpaRepository<BankCardsEntity, UUID> {

}