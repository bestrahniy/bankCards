package com.example.bankcards.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.bankcards.controller.interfaces.UserController;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardStatusResponse;
import com.example.bankcards.dto.response.CreateTransactionResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.service.CardServiceImpl;
import com.example.bankcards.service.TransferServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Implementation of UserController
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserControllerImpl implements UserController {

    private final CardServiceImpl cardService;

    private final TransferServiceImpl transferService;

    /**
     * Creates a card request for the authenticated user
     *
     * @param userDetails Authenticated user details
     * @return NotificationResponse with request details
     */
    @Operation(
            summary = "Create card request",
            description = "Submits a request to create a new card for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card request created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @PostMapping("/cards/request/create")
    public NotificationResponse createCard(@AuthenticationPrincipal @Valid UserDetails userDetails) {
        log.info("Entering createCard for user: {}", userDetails.getUsername());
        NotificationResponse response = cardService.createCardRequest(userDetails);
        log.info("Exiting createCard with response: {}", response);
        return response;
    }

    /**
     * Shows all cards for the authenticated user with pagination
     *
     * @param page The page number (starting from 0)
     * @param size The number of items per page (1-100)
     * @return PageResponse containing CardResponse items
     */
    @Operation(
            summary = "Show all cards",
            description = "Fetches a paginated list of the user's cards",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cards retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @GetMapping("/cards")
    public PageResponse<CardResponse> showAllCards(
            @RequestParam(defaultValue = "0") @Min(0) @Valid Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) @Valid Integer size) {
        log.info("Entering showAllCards with page: {}, size: {}", page, size);
        PageResponse<CardResponse> response = cardService.showAllCards(page, size);
        log.info("Exiting showAllCards with response size: {}", response.getContent().size());
        return response;
    }

    /**
     * Performs a money transfer
     *
     * @param transferRequest Request containing transfer details
     * @return CreateTransactionResponse with transaction details
     * @throws IllegalAccessException If access is denied
     */
    @Operation(
            summary = "Perform money transfer",
            description = "Transfers money between cards. Requires user privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid transfer data"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - access denied"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @PostMapping("/cards/transfer")
    public CreateTransactionResponse transfer(@RequestBody @Valid TransferRequest transferRequest) throws IllegalAccessException {
        log.info("Entering transfer with request: {}", transferRequest);
        CreateTransactionResponse response = transferService.transfer(transferRequest);
        log.info("Exiting transfer with response: {}", response);
        return response;
    }

    /**
     * Checks the balance of a card
     *
     * @param cardNumberRequest Request containing card number
     * @return CardStatusResponse with balance and transactions
     * @throws IllegalAccessException If access is denied
     */
    @Operation(
            summary = "Check card balance",
            description = "Retrieves the current balance and transaction history for the card",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid card number"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - access denied"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @GetMapping("/cards/balance")
    public CardStatusResponse checkBalance(@RequestBody @Valid CardNumberRequest cardNumberRequest) throws IllegalAccessException {
        log.info("Entering checkBalance with request: {}", cardNumberRequest);
        CardStatusResponse response = cardService.checkBalance(cardNumberRequest);
        log.info("Exiting checkBalance with response: {}", response);
        return response;
    }

    /**
     * Creates a block request for a card
     *
     * @param cardNumberRequest Request containing card number
     * @return NotificationResponse with request details
     */
    @Operation(
            summary = "Create card block request",
            description = "Submits a request to block the specified card",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Block request created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid card number"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN') and hasRole('USER')")
    @PostMapping("/cards/request/block")
    public NotificationResponse blockCard(@RequestBody @Valid CardNumberRequest cardNumberRequest) {
        log.info("Entering blockCard with request: {}", cardNumberRequest);
        NotificationResponse response = cardService.createBlockRequest(cardNumberRequest);
        log.info("Exiting blockCard with response: {}", response);
        return response;
    }

}
