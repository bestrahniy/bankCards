package com.example.bankcards.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankcards.model.entity.UsersEntity;
import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<UsersEntity, UUID> {

    Optional<UsersEntity> findByLogin(String login);

    List<UsersEntity> findByEmail(String email);

}
