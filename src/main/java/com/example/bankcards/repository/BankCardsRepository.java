package com.example.bankcards.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.UsersEntity;

import java.util.List;
import java.util.Optional;


public interface BankCardsRepository extends JpaRepository<BankCardsEntity, UUID> {

    Optional<BankCardsEntity> findByNumber(String number);

    List<BankCardsEntity> findByUser(UsersEntity user);

}