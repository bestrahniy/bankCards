package com.example.bankcards.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bankcards.model.entity.BankCardsEntity;
import com.example.bankcards.model.entity.UsersEntity;
import java.util.List;
import java.util.Optional;


public interface BankCardsRepository extends JpaRepository<BankCardsEntity, UUID> {

    Optional<BankCardsEntity> findByNumber(String number);

    List<BankCardsEntity> findByUser(UsersEntity user);

    @Query("SELECT b FROM BankCardsEntity b WHERE b.user = :user AND b.isActive = true")
    Page<BankCardsEntity> findActiveCardsByUser(@Param("user") UsersEntity user, Pageable pageable);

}