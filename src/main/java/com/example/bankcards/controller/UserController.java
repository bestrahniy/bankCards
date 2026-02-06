package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.service.UserService;

import lombok.RequiredArgsConstructor;
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

}
