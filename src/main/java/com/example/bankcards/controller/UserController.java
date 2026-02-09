package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.UserService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public UserResponse signup(@RequestBody RegistrationRequest registrationRequest) {
        return userService.registerUser(registrationRequest);
    }

    @PostMapping("/signin")
    public UserResponse signin(@RequestBody AuthorizationRequest authorizationRequest) {
        return userService.authorizationUser(authorizationRequest);
    }

    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @PostMapping("/cards/request/create")
    public NotificationResponse createCard(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.createCardRequest(userDetails);
    }

    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @GetMapping("/cards")
    public PageResponse<CardResponse> showAllCards(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return userService.showAllCards(page, size);
    }

    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @PostMapping("/cards/transfer")
    public CreateTransactionResponse transfer(@RequestBody TransferRequest transferRequest) throws IllegalAccessException {
        return userService.transfer(transferRequest);
    }

    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @GetMapping("/cards/balance")
    public CardStatusResponse checkBalance(@RequestBody CardNumberRequest cardNumberRequest) throws IllegalAccessException {
        return userService.checkBalance(cardNumberRequest);
    }

    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @PostMapping("/cards/request/block")
    public NotificationResponse blockCard(@RequestBody CardNumberRequest cardNumberRequest) {
        return userService.createBlockRequest(cardNumberRequest);
    }

}
