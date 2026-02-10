package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.controller.interfaces.AdminController;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.AdminCardServiceImpl;
import com.example.bankcards.service.AdminUserServiceImpl;
import com.example.bankcards.service.NotificationServiceImpl;
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
public class AdminControllerImpl implements AdminController {

    private final AdminUserServiceImpl adminUserService;

    private final AdminCardServiceImpl adminCardService;

    private final NotificationServiceImpl notificationService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/grant-admin/{userId}")
    public UserResponse grantAdmin(@PathVariable(name = "userId") UUID userId) {
        return adminUserService.grantAdminRole(userId);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notifications")
    public PageResponse<NotificationResponse> checkAllNotification(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return notificationService.getUserActiveNotifications(page, size);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/create/{userId}")
    public CreateCardResponse createCard(@PathVariable(name = "userId") UUID userId) {
        return adminCardService.createCard(userId);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/block")
    public CardActiveStatusResponse blockCard(@RequestBody CardNumberRequest cardNumberRequest) {
        return adminCardService.blockCard(cardNumberRequest);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/unblock")
    public CardActiveStatusResponse unblockCard(@RequestBody CardNumberRequest cardNumberRequest) {
        return adminCardService.unblockCard(cardNumberRequest);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/{userId}/block")
    public UserActiveResponse blockUser(@PathVariable(name = "userId") UUID userId) {
        return adminUserService.blockUser(userId);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/{userId}/unblock")
    public UserActiveResponse unblockUser(@PathVariable(name = "userId") UUID userId) {
        return adminUserService.unblockUser(userId);
    }

}
