package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @PostMapping("/create-user")
    public String postMethodName(@RequestBody String entity) {
        return entity;
    }
    
}
