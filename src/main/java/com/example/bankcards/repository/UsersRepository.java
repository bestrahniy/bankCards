package com.example.bankcards.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bankcards.model.entity.UsersEntity;
import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<UsersEntity, UUID> {

    @Query("SELECT DISTINCT u FROM UsersEntity u " +
        "LEFT JOIN FETCH u.roles r " +
        "WHERE u.login = :login")
    Optional<UsersEntity> findByLoginWithRoles(@Param("login") String login);

    @Query("SELECT DISTINCT u FROM UsersEntity u " +
        "WHERE u.login = :login")
    Optional<UsersEntity> findByLogin(@Param("login") String login);

    List<UsersEntity> findByEmail(String email);

    boolean existsByLogin(String login);

}
