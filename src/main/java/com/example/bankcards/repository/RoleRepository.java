package com.example.bankcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankcards.model.entity.RoleEntity;
import java.util.List;
import com.example.bankcards.model.enums.RoleType;


public interface RoleRepository extends JpaRepository<RoleEntity, Long>{

    List<RoleEntity> findByRole(RoleType role);
    
}
