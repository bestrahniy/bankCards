package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.AdminService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/grant-admin/{user_id}")
    public UserResponse postMethodName(@PathVariable(name = "user_id") UUID userId) {
        return adminService.grantAdminRole(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notifications")
    public PageResponse<NotificationResponse> checkAllNotification(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return adminService.getUserActiveNotifications(page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/card/create")
    public CreateCardResponse createCard(@PathVariable(name = "userId") UUID userId) {
        return adminService.createCard(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/block")
    public CardActiveStatusResponse blockCard(@RequestBody CardNumberRequest cardNumberRequest) {
        return adminService.blockCard(cardNumberRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/unblock")
    public CardActiveStatusResponse unblockCard(@RequestBody CardNumberRequest cardNumberRequest) {
        return adminService.unblockCard(cardNumberRequest);
    }

}
