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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Implementation of AdminController
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminControllerImpl implements AdminController {

    private final AdminUserServiceImpl adminUserService;

    private final AdminCardServiceImpl adminCardService;

    private final NotificationServiceImpl notificationService;

    /**
     * Grants admin role to a user
     *
     * @param userId The ID of the user to grant admin role
     * @return UserResponse with updated user details
     */
    @Operation(
            summary = "Grant admin role to a user",
            description = "Assigns the admin role to the specified user ID. Requires admin privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin role granted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/grant-admin/{userId}")
    public UserResponse grantAdmin(@PathVariable(name = "userId") UUID userId) {
        log.info("Entering grantAdmin for userId: {}", userId);
        UserResponse response = adminUserService.grantAdminRole(userId);
        log.info("Exiting grantAdmin with response: {}", response);
        return response;
    }

    /**
     * Retrieves all notifications with pagination
     *
     * @param page The page number (starting from 0)
     * @param size The number of items per page (1-100)
     * @return PageResponse containing NotificationResponse items
     */
    @Operation(
            summary = "Check all notifications",
            description = "Fetches a paginated list of all notifications. Requires admin privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notifications")
    public PageResponse<NotificationResponse> checkAllNotification(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        log.info("Entering checkAllNotification with page: {}, size: {}", page, size);
        PageResponse<NotificationResponse> response = notificationService.getUserActiveNotifications(page, size);
        log.info("Exiting checkAllNotification with response size: {}", response.getContent().size());
        return response;
    }

    /**
     * Creates a new card for a user
     *
     * @param userId The ID of the user to create card for
     * @return CreateCardResponse with new card details
     */
    @Operation(
            summary = "Create a new card for a user",
            description = "Generates a new bank card for the specified user ID. Requires admin privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/create/{userId}")
    public CreateCardResponse createCard(@PathVariable(name = "userId") UUID userId) {
        log.info("Entering createCard for userId: {}", userId);
        CreateCardResponse response = adminCardService.createCard(userId);
        log.info("Exiting createCard with response: {}", response);
        return response;
    }

    /**
     * Blocks a card
     *
     * @param cardNumberRequest Request containing card number
     * @return CardActiveStatusResponse with updated status
     */
    @Operation(
            summary = "Block a card",
            description = "Blocks the specified card. Requires admin privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card blocked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid card number"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/block")
    public CardActiveStatusResponse blockCard(@RequestBody CardNumberRequest cardNumberRequest) {
        log.info("Entering blockCard with request: {}", cardNumberRequest);
        CardActiveStatusResponse response = adminCardService.blockCard(cardNumberRequest);
        log.info("Exiting blockCard with response: {}", response);
        return response;
    }

    /**
     * Unblocks a card
     *
     * @param cardNumberRequest Request containing card number
     * @return CardActiveStatusResponse with updated status
     */
    @Operation(
            summary = "Unblock a card",
            description = "Unblocks the specified card. Requires admin privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card unblocked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid card number"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/card/unblock")
    public CardActiveStatusResponse unblockCard(@RequestBody CardNumberRequest cardNumberRequest) {
        log.info("Entering unblockCard with request: {}", cardNumberRequest);
        CardActiveStatusResponse response = adminCardService.unblockCard(cardNumberRequest);
        log.info("Exiting unblockCard with response: {}", response);
        return response;
    }

    /**
     * Blocks a user
     *
     * @param userId The ID of the user to block
     * @return UserActiveResponse with updated status
     */
    @Operation(
            summary = "Block a user",
            description = "Blocks the specified user. Requires admin privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User blocked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/{userId}/block")
    public UserActiveResponse blockUser(@PathVariable(name = "userId") UUID userId) {
        log.info("Entering blockUser for userId: {}", userId);
        UserActiveResponse response = adminUserService.blockUser(userId);
        log.info("Exiting blockUser with response: {}", response);
        return response;
    }

    /**
     * Unblocks a user
     *
     * @param userId The ID of the user to unblock
     * @return UserActiveResponse with updated status
     */
    @Operation(
            summary = "Unblock a user",
            description = "Unblocks the specified user. Requires admin privileges",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User unblocked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid user ID"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/{userId}/unblock")
    public UserActiveResponse unblockUser(@PathVariable(name = "userId") UUID userId) {
        log.info("Entering unblockUser for userId: {}", userId);
        UserActiveResponse response = adminUserService.unblockUser(userId);
        log.info("Exiting unblockUser with response: {}", response);
        return response;
    }

}
