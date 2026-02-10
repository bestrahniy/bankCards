package com.example.bankcards.controller.interfaces;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.bankcards.dto.request.CardNumberRequest;
import com.example.bankcards.dto.response.CardActiveStatusResponse;
import com.example.bankcards.dto.response.CreateCardResponse;
import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserActiveResponse;
import com.example.bankcards.dto.response.UserResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public interface AdminController {

    UserResponse grantAdmin(@PathVariable(name = "userId") UUID userId);

    PageResponse<NotificationResponse> checkAllNotification(
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size);

    CreateCardResponse createCard(@PathVariable(name = "userId") UUID userId);

    CardActiveStatusResponse blockCard(@RequestBody CardNumberRequest cardNumberRequest);

    CardActiveStatusResponse unblockCard(@RequestBody CardNumberRequest cardNumberRequest);

    UserActiveResponse blockUser(@PathVariable(name = "userId") UUID userId);

    UserActiveResponse unblockUser(@PathVariable(name = "userId") UUID userId);
}
