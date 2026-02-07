package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.AdminService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{user_id}/grant-admin")
    public UserResponse postMethodName(@PathVariable(name = "user_id") UUID userId) {
        return adminService.grantAdminRole(userId);
    }

}
