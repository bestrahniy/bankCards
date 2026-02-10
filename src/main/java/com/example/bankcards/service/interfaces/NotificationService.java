package com.example.bankcards.service.interfaces;

import com.example.bankcards.dto.response.NotificationResponse;
import com.example.bankcards.dto.response.PageResponse;

public interface NotificationService {

    PageResponse<NotificationResponse> getUserActiveNotifications(int page, int size);

}
