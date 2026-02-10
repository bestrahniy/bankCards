package com.example.bankcards.config;

import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.bankcards.model.entity.RoleEntity;
import com.example.bankcards.model.entity.UsersEntity;
import com.example.bankcards.model.enums.RoleType;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for initializing data on application startup
 * Creates default roles (USER and ADMIN) and an admin user if they do not exist
 * Active only in non-test profiles
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer {

    private final RoleRepository roleRepository;

    private final UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * CommandLineRunner bean to initialize admin user and roles
     * Checks for existing roles and admin user, creates them if missing
     *
     * @return CommandLineRunner instance
     */
    @Bean
    CommandLineRunner initAdmin() {
        return args -> {
            log.info("Starting data init");
            RoleEntity userRole = roleRepository.findByRole(RoleType.USER)
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {
                        log.debug("create User role");
                        return roleRepository.save(RoleEntity.builder()
                            .role(RoleType.USER)
                            .build());
                    });

            RoleEntity adminRole = roleRepository.findByRole(RoleType.ADMIN)
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {
                        log.debug("create Admin role");
                        return roleRepository.save(RoleEntity.builder()
                            .role(RoleType.ADMIN)
                            .build());
                        });

                String loginAdmin = ("admin");
                if (!usersRepository.existsByLogin(loginAdmin)) {
                    log.info("Creating admin");
                    UsersEntity user = UsersEntity.builder()
                    .login(loginAdmin)
                    .password(passwordEncoder.encode("admin"))
                    .email("admin@gmail.com")
                    .roles(Set.of(userRole, adminRole))
                    .build();

                    usersRepository.save(user);
                    log.info("Create admin");
                } else {
                    log.info("User with role admin already exists");
                }
                log.info("Data initialization completed");
            };
    }
}
