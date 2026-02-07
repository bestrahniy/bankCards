package com.example.bankcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankcards.model.entity.NotificationEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findAllByIsActiveTrue(Pageable pageable);

}
