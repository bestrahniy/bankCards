package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.AuthorizationRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.CreateCardNotificationResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
    public CreateCardNotificationResponse createCard(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.createCardRequest(userDetails);
    }

}
