package com.example.bankcards.controller.interfaces;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public interface UserController {

    NotificationResponse createCard(@AuthenticationPrincipal UserDetails userDetails);

    PageResponse<CardResponse> showAllCards(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size);

    CreateTransactionResponse transfer(@RequestBody TransferRequest transferRequest) throws IllegalAccessException;

    CardStatusResponse checkBalance(@RequestBody CardNumberRequest cardNumberRequest) throws IllegalAccessException;

    NotificationResponse blockCard(@RequestBody CardNumberRequest cardNumberRequest);

}
